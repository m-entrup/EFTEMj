"""
@ImagePlus IMP

file:       Gatan_Renamer.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script to rename dm3 files using metadata.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

from __future__ import division

# pylint: disable-msg=E0401
from java.lang import Double
from java.text import SimpleDateFormat
from java.text import ParsePosition

from ij.gui import GenericDialog
from de.m_entrup.EFTEMj_lib.tools import GatanMetadataExtractor
# pylint: enable-msg=E0401

# pylint: disable-msg=E0602
IMP = IMP
# pylint: enable-msg=E0602

# Some general settings:
DEFAULT_FORMAT_STR = '%(date)s_%(mag)s_%(dE)s_%(exp)s_%(name)s'
FIELD_WIDTH = 10
FIELD_WIDTH_LONG = 40
# End of settings.


class ImageProperties:
    """A class that uses de.m_entrup.EFTEMj_lib.tools.GatanMetadataExtractor.
    Metadata from a dm3 file is gathered to be used for renaming.
    """

    def __init__(self, imp):
        """Get the metadata from the given dm3 image.
        """
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
        self.name = extractor.getName()

    def calc_mag(self, mag):
        """Use the magnification factor to calculate the actual magnification.
        """
        self.magnification = self.mag_factor * mag

    def to_dict(self):
        """Create a dictionary from the metadata to be used for string formating.
        """
        prop_dict = {}
        prop_dict['exp'] = '%gs' % (self.exposure,)
        prop_dict['dE'] = '%geV' % (self.energyloss,)
        prop_dict['date'] = self.date_to_string()
        mag = self.magnification
        if self.mag_unit.lower() == 'kx':
            mag /= 1000
            prop_dict['mag'] = '%.3g%s' % (mag, self.mag_unit)
        else:
            prop_dict['mag'] = '%.0f%s' % (mag, self.mag_unit)
        prop_dict['name'] = self.name
        return prop_dict

    def date_to_string(self):
        """Returns the date as a formated string.
        """
        date_formater = SimpleDateFormat('yyyyMMdd')
        return date_formater.format(self.date)

    def parse_date(self, date_string):
        """Reads a date from the given string.
        :param date_string: String to parse.
        """
        date_formater = SimpleDateFormat('yyyyMMdd')
        self.date = date_formater.parse(date_string, ParsePosition(0))


def run_script(imp):
    """Function to be run when this file is used as a script
    """
    properties = ImageProperties(imp)
    # Create a GenericDialog to configure renaming:
    dialog = GenericDialog('Gatan Reamer')
    dialog.addMessage('Modifying: %s' % (imp.getTitle(),))
    dialog.addMessage('Recorded: %s' % (properties.date.toString(),))
    dialog.addNumericField(
        'Exposure time', properties.exposure, 4, FIELD_WIDTH, 's')
    dialog.addNumericField(
        'Magnification:', properties.magnification, 0, FIELD_WIDTH, 'x')
    mag_units = ('kx', 'x')
    dialog.addChoice('Magnification unit:', mag_units, mag_units[0])
    dialog.addMessage(
        'The actual magnification is %.2f times larger.' % (properties.mag_factor,))
    dialog.addCheckbox('Use actual magnification:', False)
    dialog.addMessage('')
    dialog.addNumericField(
        'Energy loss:', properties.energyloss, 1, FIELD_WIDTH, 'eV')
    dialog.addStringField('Date:', properties.date_to_string(), FIELD_WIDTH)
    dialog.addStringField('original name:', properties.name, FIELD_WIDTH_LONG)
    dialog.addStringField(
        'Filename format', DEFAULT_FORMAT_STR, FIELD_WIDTH_LONG)
    dialog.showDialog()

    if not dialog.wasCanceled():
        # Edit the properties to consiter user choices:
        properties.exposure = dialog.getNextNumber()
        mag = dialog.getNextNumber()
        properties.mag_unit = dialog.getNextChoice()
        if dialog.getNextBoolean():
            properties.calc_mag(mag)
        properties.energyloss = dialog.getNextNumber()
        properties.parse_date(dialog.getNextString())
        properties.name = dialog.getNextString()
        format_str = dialog.getNextString()
        # Chenge the title:
        imp.setTitle(format_str % properties.to_dict())

if __name__ == '__main__':
    run_script(IMP)
