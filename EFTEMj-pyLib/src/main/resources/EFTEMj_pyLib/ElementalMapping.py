'''
file:       ElementalMapping.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161017
info:       This module contains different classes to calculate elemental mpas.
'''

from __future__ import with_statement, division

import math
import re

# pylint: disable-msg=E0401
from ij import IJ, ImagePlus
from ij.process import FloatProcessor

from java.lang import Float
# jarray.array() is used to convert from a list to a java array.
from jarray import array
# pylint: enable-msg=E0401


class ThreeWindow:
    '''This class is used to calculate the elemental map and the SNR using the three window method.

    The background signal at the post-edge image is estimated
    by fitting a power law function to the pre-edge images.
    The linear representation of the power law is used.
    The slope and y-intercept of the linear function are calculated using the two-point-form.
    The algorithm is based on an article by A. Berger and H.Kohl [1]

    [1]: A. Berger and H. Kohl in Optik 92 (1993), p.175-193
    '''

    # pylint: disable=too-many-instance-attributes
    # pylint: disable-msg=C0103
    sigs = None
    snrs = None
    rs = None
    lnAs = None
    hs = None
    variances = None

    def __init__(self, pre1, pre2, post):
        '''
        Preparing the algorithem by passing thre images.

        pre1 and pre2 are the pre-edge images used to estimate the power law background.
        post is the image, the estimated background signal is subtracted from.
        '''
        self.pre1_imp = pre1
        self.pre2_imp = pre2
        self.post_imp = post
        eloss_pattern = re.compile(r'(\d+(?:[\.,]\d+)?)eV')
        self.eloss_pre1 = float(eloss_pattern.search(pre1.getTitle()).group(1))
        self.eloss_pre2 = float(eloss_pattern.search(pre2.getTitle()).group(1))
        self.eloss_post = float(eloss_pattern.search(post.getTitle()).group(1))
        print('Energy-losses extracted form image titles: %deV, %deV and %deV'
              % (self.eloss_pre1, self.eloss_pre2, self.eloss_post))
        # There are 5 repetitive called functions where _update_progress() is
        # called.
        self.full_progress = 5 * post.getWidth() * post.getHeight() - 1
        self.progress = 0
        IJ.showProgress(0, self.full_progress)

    def _update_progress(self):
        '''Raises the progress by one and updates the progress bar.
        '''
        self.progress += 1
        IJ.showProgress(self.progress, self.full_progress)

    def calc_map(self):
        '''Calcs the elemental map and stores the result as property of the object.

        This method generates results for the objects properties sigs, rs and lnAs.'''
        E1 = self.eloss_pre1
        E2 = self.eloss_pre2
        IJ.showStatus('Calculating the parameter ln(A)...')
        self.lnAs = [self.calc_lnA(Nb1, Nb2, E1, E2)
                     for Nb1, Nb2 in zip(self.pre1_imp.getProcessor().getPixels(),
                                         self.pre2_imp.getProcessor().getPixels())
                     ]
        IJ.showStatus('Calculating the parameter r...')
        self.rs = [self.calc_r(Nb1, Nb2, E1, E2)
                   for Nb1, Nb2 in zip(self.pre1_imp.getProcessor().getPixels(),
                                       self.pre2_imp.getProcessor().getPixels())
                   ]
        IJ.showStatus('Calculating the elemental map...')
        self.sigs = [self.calc_sig(sig, lnA, r)
                     for sig, lnA, r in zip(self.post_imp.getProcessor().getPixels(),
                                            self.lnAs, self.rs)
                     ]

    def calc_sig(self, sig, lnA, r):
        '''A helper function used by calc_map().'''
        if not Float.isNaN(lnA) and not Float.isNaN(r) and sig > 0:
            return float(sig) - calc_pow(self.eloss_post, lnA, r)
        return Float.NaN

    def calc_lnA(self, Nb1, Nb2, E1, E2):
        '''A function to calculate the parameter ln(a).'''
        self._update_progress()
        if Nb1 > 0 and Nb2 > 0:
            return (math.log(Nb1) - math.log(Nb2)) \
                / (math.log(E2) - math.log(E1)) \
                * math.log(E1) + math.log(Nb1)
        return Float.NaN

    def calc_r(self, Nb1, Nb2, E1, E2):
        '''A function to calculate the parameter r.'''
        self._update_progress()
        if Nb1 > 0 and Nb2 > 0:
            return (math.log(Nb1) - math.log(Nb2)) / (math.log(E2) - math.log(E1))
        return Float.NaN

    def calc_snr(self):
        '''Calcs the SNR (signal to noise ratio) and stores the result as property of the object.

        If not calc_map() was used before, this function does nothing.
        This method generates results for the objects properties snrs, hs ans variances.
        '''
        if self.sigs and self.rs and self.lnAs:
            self.calc_vars()
            self.calc_hs()
            IJ.showStatus('Calculating the variance...')
            vars_sig = [self._calc_var_sig(sig, h, lnA, r)
                        for sig, h, r, lnA in zip(self.sigs, self.hs, self.rs, self.lnAs)
                        ]
            IJ.showStatus('Calculating the signal to noise ratio...')
            self.snrs = [self._calc_snr(sig, var_sig)
                         for sig, var_sig in zip(self.sigs, vars_sig)
                         ]

    def calc_vars(self):
        '''A helper function used by calc_snr().'''
        self.variances = [self._calc_var(lnA, r)
                          for lnA, r in zip(self.lnAs, self.rs)]

    def _calc_var(self, lnA, r):
        '''A helper function used by calc_vars().'''
        self._update_progress()
        if not Float.isNaN(lnA) and not Float.isNaN(r):
            E1 = self.eloss_pre1
            E2 = self.eloss_pre2
            Es = self.eloss_post
            q = (math.log(Es) - 0.5 * (math.log(E1) + math.log(E2))) / \
                (math.log(E2) - math.log(E1))
            a01 = math.pow(Es / E1, -1 * r) * (q - 0.5)**2
            a02 = math.pow(Es / E2, -1 * r) * (q + 0.5)**2
            a11 = math.pow(Es / E1, -2 * r) * (q - 0.5)**2 * (q + 0.5)
            a12 = math.pow(Es / E2, -2 * r) * (q - 0.5) * (q + 0.5)**2
            return calc_pow(Es, lnA, r) * (a01 + a02 + (a11 + a12) / calc_pow(Es, lnA, r))
        return Float.NaN

    def calc_hs(self):
        '''A helper function used by calc_snr().'''
        self.hs = [self._calc_h(r, lnA, var)
                   for r, lnA, var in zip(self.rs, self.lnAs, self.variances)
                   ]

    def _calc_h(self, r, lnA, var):
        '''A helper function used by calc_hs().'''
        self._update_progress()
        Es = self.eloss_post
        if not Float.isNaN(lnA) and not Float.isNaN(r):
            return 1 + (var / calc_pow(Es, lnA, r))
        return Float.NaN

    def _calc_var_sig(self, sig, h, lnA, r):
        '''A helper function used by calc_snr().'''
        self._update_progress()
        if not Float.isNaN(r) and not Float.isNaN(lnA):
            Es = self.eloss_post
            return sig + h * calc_pow(Es, lnA, r)
        return Float.NaN

    def _calc_snr(self, sig, var_sig):
        '''A helper function used by calc_snr().'''
        # pylint: disable-msg=R0201
        if not Float.isNaN(sig) and not Float.isNaN(var_sig):
            return sig / math.sqrt(var_sig)
        return Float.NaN

    def get_result(self):
        '''Returns the ImagePlus objects that represent the elemental map,
        the SNR and the maps of the parameters r and ln(a).

        If the calculation of results was not done before, ths method invokes the calculation.
        '''
        if not self.sigs:
            self.calc_map()
        lnA_imp = ImagePlus('Map of parameter ln(a)',
                            FloatProcessor(self.post_imp.getWidth(),
                                           self.post_imp.getHeight(),
                                           array(self.lnAs, 'f')
                                           )
                            )
        r_imp = ImagePlus('Map of parameter r',
                          FloatProcessor(self.post_imp.getWidth(),
                                         self.post_imp.getHeight(),
                                         array(self.rs, 'f')
                                         )
                          )
        sig_imp = ImagePlus('Elemental map %deV - %s' % (self.eloss_post,
                                                         self.post_imp.getShortTitle()),
                            FloatProcessor(self.post_imp.getWidth(),
                                           self.post_imp.getHeight(),
                                           array(self.sigs, 'f')
                                           )
                            )
        if not self.snrs:
            self.calc_snr()
        snr_imp = ImagePlus('SNR %deV - %s' % (self.eloss_post,
                                               self.post_imp.getShortTitle()),
                            FloatProcessor(self.post_imp.getWidth(),
                                           self.post_imp.getHeight(),
                                           array(self.snrs, 'f')
                                           )
                            )
        IJ.showProgress(1.0)
        IJ.showStatus('Calculation finished.')
        return sig_imp, snr_imp, r_imp, lnA_imp


def calc_pow(x, lnA, r):
    '''Returns the result of the power law function.'''
    # pylint: disable-msg=C0103
    return math.exp(lnA) * math.pow(x, -r)
