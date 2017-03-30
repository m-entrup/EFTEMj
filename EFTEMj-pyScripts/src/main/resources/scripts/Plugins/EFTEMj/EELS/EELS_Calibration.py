"""
@File(label='Input directory', style='directory') SRC_DIR
@boolean(label='Use fast mode', description='calc in fourier space', value=True) FAST_MODE
@boolean(label='Debug mode', value=False) DEBUG

file:       EELS_Calibration.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script to get the energy dispersion from a series of spectra.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103


from __future__ import with_statement, division

import csv
import os
from collections import deque

# pylint: disable-msg=E0401
from ij import IJ
from ij.gui import Plot
from ij.gui import ProfilePlot
from ij.measure import CurveFitter
from org.apache.commons.math3.stat.correlation import PearsonsCorrelation
from org.apache.commons.math3.transform import FastFourierTransformer
from org.apache.commons.math3.transform import DftNormalization as norm
from org.apache.commons.math3.transform import TransformType as trans_type
# pylint: enable-msg=E0401

# pylint: disable-msg=E0602
SRC_DIR = SRC_DIR
FAST_MODE = FAST_MODE
DEBUG = DEBUG
# pylint: enable-msg=E0602

# Save errors to this list and desplay them at the end of the script.
ERRORS = []


class Spectrum:
    """A class that represents an EEL Spectrum.
    There are methods to create a Spectrum from a file.
    And methods to correlate two spectra with each other.
    Finally the spectrum can be plotted using IJs Plot class.
    """

    @classmethod
    def get_spectrum_csv(cls, csv_file):
        """ Load from csv files created with ImageJ.
        These files use commas as delimiter.
        Comments are ignored by catching ValueErrors.
        """
        spectrum = []
        with open(csv_file, 'rb') as csvfile:
            delim = ','
            if csv_file.endswith('.xls'):
                delim = '\t'
            reader = csv.reader(csvfile, delimiter=delim)
            index = 0
            for row in reader:
                try:
                    spectrum.append({
                        'x': index,
                        'dE': float(row[0]),
                        'y': float(row[1])
                    })
                    index += 1
                except ValueError:
                    ERRORS.append('%s: Skipping this row: %s' %
                                  (os.path.basename(csv_file), row))
        return spectrum

    @classmethod
    def get_spectrum_msa(cls, msa_file):
        """ Load from msa files created with Gatan DM.
        These files use commas as delimiter.
        Comments are ignored by catching ValueErrors.
        """
        spectrum = []
        with open(msa_file, 'rb') as csvfile:
            reader = csv.reader(csvfile, delimiter=',')
            index = 0
            for row in reader:
                try:
                    spectrum.append({
                        'x': index,
                        'dE': float(row[0]),
                        'y': float(row[1])
                    })
                    index += 1
                except ValueError:
                    ERRORS.append('%s: Skipping this row: %s' %
                                  (os.path.basename(msa_file), row))
        return spectrum

    @classmethod
    def get_spectrum_dm3(cls, dm3_file):
        """ Load from dm3 files created with Gatan DM.
        These files are images that are converted to line profiles.
        """
        spectrum = []
        imp = IJ.openImage(dm3_file)
        IJ.run(imp, "Select All", "")
        profiler = ProfilePlot(imp)
        plot = profiler.getPlot()
        y_values = plot.getYValues()
        spectrum = []
        index = 0
        for y_value in y_values:
            spectrum.append({
                'x': index,
                'y': y_value
            })
            index += 1
        return spectrum

    @classmethod
    def from_file(cls, file_path):
        """ Load a spectrum from file and return it as an instance of Spectrum.
        """
        if file_path.endswith('.dm3'):
            spectrum = Spectrum.get_spectrum_dm3(file_path)
        elif file_path.endswith('.csv') or file_path.endswith('.xls'):
            spectrum = Spectrum.get_spectrum_csv(file_path)
        elif file_path.endswith('.msa'):
            spectrum = Spectrum.get_spectrum_msa(file_path)
        else:
            return
        ''' Find an energy loss with optional decimal places.
        The rest of the file name is ignored.
        '''
        import re
        pattern = re.compile('.*[^\\d\\.]((?:\\d+[\\.,])?\\d+)eV.*')
        match = pattern.match(file_path)
        loss = float(match.group(1))
        return cls(spectrum, loss)

    def __init__(self, spectrum, loss):
        self.spec = spectrum

        self.loss = loss
        self.x_values = [item['x'] for item in spectrum]
        self.y_values = [item['y'] for item in spectrum]

    def plot(self):
        """Plot this spectrum by using IJs Plot class.
        """
        plot = Plot('Spectrum %deV' % self.loss,
                    'Position [px]',
                    'Intensity [a.u.]',
                    self.x_values, self.y_values
                   )
        plot.show()

    def crosscorrelation(self, spectrum):
        """ Calculate the crosscorrelation of the instance with a given Spectrum.
        The result is again an instance of Spectrum.
        There are to modes to use:
        1. 'FAST_MODE' runs in fourier space.
        2. The second mode runs without fouriertransform.
        """
        if FAST_MODE:
            '''old version
            from org.apache.commons.math3.util import MathArray_values
            reverse = spectrum.y_values[::-1]
            corr = MathArray_values.convolve(self.y_values, reverse)
            '''
            transformer = FastFourierTransformer(norm.STANDARD)
            fft1 = transformer.transform(self.y_values, trans_type.FORWARD)
            fft2 = transformer.transform(spectrum.y_values, trans_type.FORWARD)
            fft2c = [val.conjugate() for val in fft2]
            corr_fft = [val1.multiply(val2) for val1, val2 in zip(fft1, fft2c)]
            corr_c = transformer.transform(corr_fft, trans_type.INVERSE)
            corr = [val.getReal() for val in corr_c]
        else:
            correlator = PearsonsCorrelation()
            corr = []
            shifted = deque(spectrum.y_values)
            for _ in range(len(self.y_values)):
                corr.append(correlator.correlation(
                    self.y_values, list(shifted)))
                shifted.rotate(1)
        new_spec = [{'x': x, 'y': y} for x, y in zip(self.x_values, corr)]
        return Spectrum(new_spec, self.loss - spectrum.loss)


def pos_of(values, method):
    """Find the position of a value that is calculated by the given method.
    """
    result = method(values)
    pos = [i for i, j in enumerate(values) if j == result]
    if len(pos) > 1:
        print('Warning: The %s of the list is not distinct.' % (method,))
    return pos[0]


def get_shift(spectra):
    """ Use crosscorrelation do determine the shoft between two spectra.
    The results are two array_values that contain the shift in eV (from title)
    and in px (from crosscorrelation).
    """
    ref_pos = int(round(len(spectra) / 2)) - 1
    ref = spectra[ref_pos]
    if DEBUG:
        IJ.log('Energy loss of the reference spectrum is %deV' % ref.loss)
    steps = len(spectra)
    correlations = []
    for i, spec in enumerate(spectra):
        IJ.showProgress(i / steps)
        correlations.append(ref.crosscorrelation(spec))
    IJ.showProgress(1)
    if DEBUG:
        for corr in correlations:
            corr.plot()
    x_values = [item.loss for item in correlations]
    y_values = [pos_of(item.y_values, max)
                if item.loss <= 0
                else -(len(item.y_values) - pos_of(item.y_values, max))
                for item in correlations
               ]
    return x_values, y_values


def get_lin_fit(x_values, y_values):
    """Calculate a linear fit for the given values and return it.
    """
    fitter = CurveFitter(x_values, y_values)
    fitter.doFit(CurveFitter.STRAIGHT_LINE)
    return fitter


def error_of_dispersion(fit, x_values):
    """ Calculate the statistical error of the dispersion.
    See "Fehleranalyse" by John R. Taylor at p. 38f.
    """
    r_squares = [val**2 for val in fit.getResiduals()]
    s_y = sum(r_squares) / (len(r_squares) - 2)
    x_squares = [val**2 for val in x_values]
    delta_m = s_y**2 * len(r_squares) / (len(r_squares)
                                         * sum(x_squares) - sum(x_values)**2)
    return delta_m / fit.getParams()[1]**2

def error_of_gatandispersion(dispersion, uncertainty):
    """ Calculate the statistical error of the gatandispersion.
    """
    rel_uncertainty = uncertainty / dispersion
    gatan_dispersion = 15 / dispersion
    return rel_uncertainty * gatan_dispersion


def run_script():
    """Function to be run when this file is used as a script
    """
    files = []
    for item in os.listdir(SRC_DIR.getAbsolutePath()):
        files.append(os.path.join(SRC_DIR.getAbsolutePath(), str(item)))
    if len(files) <= 1:
        return
    spectra = [Spectrum.from_file(file_path) for file_path in files]
    spectra = [spec for spec in spectra if spec is not None]
    if len(spectra) <= 1:
        return
    spectra = sorted(spectra, key=lambda item: item.loss)
    if DEBUG:
        for spec in spectra:
            spec.plot()
    plot = Plot('EELS-Cal of %s' % os.path.basename(SRC_DIR.getAbsolutePath()),
                'Energy loss difference [eV]',
                'Shift [px]'
               )
    x_values, y_values = get_shift(spectra)
    plot.addPoints(x_values, y_values, Plot.CROSS)
    fit = get_lin_fit(x_values, y_values)
    x_fit = range(int(min(x_values)), int(max(x_values)) + 1)
    y_fit = [fit.f(x) for x in x_fit]
    plot.addPoints(x_fit, y_fit, Plot.LINE)
    # This is necessary to get a new row at the legend
    plot.addPoints([0], [0], Plot.LINE)
    plot.addLegend('Measured shift\ng(x) = %f x + %f\nr^2 = %f' %
                   (fit.getParams()[1], fit.getParams()[0], fit.getRSquared()))
    plot.addLabel(0.6,
                  0.4,
                  'dispersion = %feV/px\n uncertainty: %feV/px\nGatan dispersion: %f\n uncertainty: %f' %
                  (-1 / fit.getParams()[1],
                   error_of_dispersion(fit, x_values),
                   -15 * fit.getParams()[1],
                   error_of_gatandispersion(-1 / fit.getParams()[1], error_of_dispersion(fit, x_values))
                  )
                 )
    plot.show()
    if DEBUG:
        if len(ERRORS) >= 1:
            IJ.log('Errors durring script execution:')
            for error in ERRORS:
                IJ.log(error)

if __name__ == '__main__':
    run_script()
