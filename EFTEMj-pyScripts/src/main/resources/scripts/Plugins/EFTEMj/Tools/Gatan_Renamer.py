# @ImagePlus imp

'''
file:       Gatan_Renamer.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161116
info:       A script to rename dm3 files using metadata.
'''

from __future__ import division
from java.lang import Double
from java.text import SimpleDateFormat
from ij.gui import GenericDialog
from de.m_entrup.EFTEMj_lib.tools import GatanMetadataExtractor

# Some general settings:
default_format_str = '%(date)s_%(mag)s_%(dE)s_%(exp)s_%(name)s'
field_width = 10
field_width_long = 40
# End of settings.


class ImageProperties:
    '''A class that uses de.m_entrup.EFTEMj_lib.tools.GatanMetadataExtractor.
    Metadata from a dm3 file is gathered to be used for renaming.
    '''

    def __init__(self, imp):
        '''Get the metadata from the given dm3 image.
        '''
        extractor = GatanMetadataExtractor(imp)
        self.exposure = extractor.getExposure()
        self.magnification = extractor.getMagnification()
        self.mag_factor = extractor.getActualMagnification() / self.magnification
        self.mag_unit = 'x'
        if not Double.isNaN(extractor.getEnergyloss()):
            self.energyloss = extractor.getEnergyloss()
        else:
            self.energyloss = 0
        self.date = extractor.getDateAndTime()
        date_formater = SimpleDateFormat('yyyyMMdd')
        self.date_string = date_formater.format(self.date)
        self.name = extractor.getName()
        self.prop_dict = {}

    def calc_mag(self, mag):
        '''Use the magnification factor to calculate the actual magnification.
        '''
        self.magnification = self.mag_factor * mag

    def to_dict(self):
        '''Create a dictionary from the metadata to be used for string formating.
        '''
        self.prop_dict['exp'] = '%gs' % (self.exposure,)
        self.prop_dict['dE'] = '%geV' % (self.energyloss,)
        self.prop_dict['date'] = self.date_string
        mag = self.magnification
        if self.mag_unit.lower() == 'kx':
            mag /= 1000
            self.prop_dict['mag'] = '%.3g%s' % (mag, self.mag_unit)
        else:
        	self.prop_dict['mag'] = '%.0f%s' % (mag, self.mag_unit)
        self.prop_dict['name'] = self.name
        return self.prop_dict


def main():
    properties = ImageProperties(imp)
    # Create a GenericDialog to configure renaming:
    gd = GenericDialog('Gatan Reamer')
    gd.addMessage('Modifying: %s' % (imp.getTitle(),))
    gd.addMessage('Recorded: %s' % (properties.date.toString(),))
    gd.addNumericField('Exposure time', properties.exposure, 4, field_width, 's')
    gd.addNumericField('Magnification:', properties.magnification, 0, field_width, 'x')
    mag_units = ('kx', 'x')
    gd.addChoice('Magnification unit:', mag_units, mag_units[0])
    gd.addMessage('The actual magnification is %.2f times larger.' % (properties.mag_factor,))
    gd.addCheckbox('Use actual magnification:', False)
    gd.addMessage('')
    gd.addNumericField('Energy loss:', properties.energyloss, 1, field_width, 'eV')
    gd.addStringField('Date:', properties.date_string, field_width)
    gd.addStringField('original name:', properties.name, field_width_long)
    gd.addStringField('Filename format', default_format_str, field_width_long)
    gd.showDialog()

    if not gd.wasCanceled():
        # Edit the properties to consiter user choices:
        properties.exposure = gd.getNextNumber()
        mag = gd.getNextNumber()
        properties.mag_unit = gd.getNextChoice()
        if gd.getNextBoolean():
            properties.calc_mag(mag)
        properties.energyloss = gd.getNextNumber()
        properties.date_string = gd.getNextString()
        properties.name = gd.getNextString()
        format_str = gd.getNextString()
        # Chenge the title:
        imp.setTitle(format_str % properties.to_dict())

if __name__ == "__main__":
    main()
