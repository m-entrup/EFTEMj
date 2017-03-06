"""
@Integer(label='Step size', value=32) STEP_SIZE
@Integer(label='Lower energy border', value=64) ENERGY_BORDER_LOW
@Integer(label='Upper energy border', value=64) ENERGY_BORDER_HIGH
@Float(label='Energy position', value=0.5, min=0, max=1) ENERGY_POSITION
@Integer(label='Polynominal order', value=3, min=1, max=8, step=1, style='slider') POLY_ORDER
@Boolean(label='Use thresholding', value=True) USE_THRESHOLDING
@String(label='Threshold method', choices={'Li', 'Default', 'Huang', 'Intermodes', 'IsoData', 'Otsu'}, value='Li') THRESHOLDING_METHOD

file:       SR-EELS_Characterisation_(automated).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script that automaticaly runs a characterisation on SR-EELS images.
            It detects all data set were no result for the selected settings are available
            and runs the the characterisation to complete the set of results.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

import os
import re
import math

# pylint: disable-msg=E0401
from java.util import ArrayList
from java.io import File
from java.lang import RuntimeException

from ij import IJ

from de.m_entrup.EFTEMj_SR_EELS.shared import SR_EELS_ConfigurationManager
from de.m_entrup.EFTEMj_SR_EELS.importer import SR_EELS_ImportPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationSettings
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationResults
# pylint: enable-msg=E0401

CONFIG = SR_EELS_ConfigurationManager.getConfiguration()
TYPES = SR_EELS_CharacterisationResults.TIFF
TYPES += SR_EELS_CharacterisationResults.JPEG
TYPES += SR_EELS_CharacterisationResults.CSV
TYPES += SR_EELS_CharacterisationResults.PLOTS


class AutomatedCharacterisation:
    """A class that represents datasets to be characterized.
    """

    def __init__(self):
        """Initialize the class by loading the path to the datasets.
        ``SR_EELS_ConfigurationManager`` defines the path.
        """
        self.path = CONFIG.getString(SR_EELS_ImportPlugin.databasePathKey)
        self.datasets = []

    def create_dataset_list(self, search_dir=None):
        """Find all datasets at the given directory.
        By default ``SR_EELS_ConfigurationManager`` defines the path.
        :param search_dir: The path to the directory to search for datasets (default: None).
        """
        if not search_dir:
            directory = self.path
        for root, dirs, _ in os.walk(search_dir):
            # print root
            if re.search('SM\\d+(\\.\\d+)?[/\\\\]-?\\d+(\\.\\d+)?$', root):
                for directory in dirs:
                    self.datasets.append(os.path.join(root, directory))

    def run_characterisations(self, settings=None):
        """Run the characterisation for all datasets.
        By default a new configuration is created.
        :param settings: An instance of SR_EELS_CharacterisationSettings (default: None).
        """
        if not settings:
            settings = SR_EELS_CharacterisationSettings()
        finished = skipped = failed = 0
        dataset_count = len(self.datasets)
        IJ.log('Processing %d datasets.' % (dataset_count,))
        count = 0
        for dataset in self.datasets:
            count += 1
            if not os.path.exists(os.path.join(dataset, settings.toString())):
                IJ.log('%d/%d Starting to process "%s".' %
                       (count, dataset_count, dataset))
                images = ArrayList()
                for image in os.listdir(dataset):
                    if re.search('Cal_\\d+\\.tif', image):
                        images.add(image)
                settings.images = images
                settings.path = File(dataset)
                try:
                    characterisation = SR_EELS_CharacterisationPlugin()
                    characterisation.performCharacterisation(settings)
                    results_path = File(settings.path, settings.toString())
                    characterisation.saveResults(results_path, TYPES)
                    finished += 1
                except RuntimeException:
                    IJ.log('%d/%d There was an error when processing "%s"!' %
                           (count, dataset_count, dataset))
                    failed += 1
            else:
                IJ.log('%d/%d Skipping "%s" as results for "%s" exist.' %
                       (count, dataset_count, dataset, settings.toString()))
                skipped += 1
        IJ.log('\nFinished automated SR-EELS characterisation:')
        IJ.log('Finished: %d\nSkipped: %d\nFailed: %d' %
               (finished, skipped, failed))


def run_script():
    """Function to be run when this file is used as a script
    """
    automated_characterisation = AutomatedCharacterisation()
    automated_characterisation.create_dataset_list()
    settings = SR_EELS_CharacterisationSettings()
    # pylint: disable-msg=E0602
    settings.stepSize = STEP_SIZE
    settings.filterRadius = math.sqrt(STEP_SIZE)
    settings.energyBorderLow = ENERGY_BORDER_LOW
    settings.energyBorderHigh = ENERGY_BORDER_HIGH
    settings.energyPosition = ENERGY_POSITION
    settings.polynomialOrder = POLY_ORDER
    settings.useThresholding = USE_THRESHOLDING
    settings.threshold = THRESHOLDING_METHOD
    # pylint: enable-msg=E0602
    automated_characterisation.run_characterisations(settings)


if __name__ == '__main__':
    run_script()
