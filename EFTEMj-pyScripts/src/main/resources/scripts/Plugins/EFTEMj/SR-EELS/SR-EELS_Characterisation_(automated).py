"""
@Integer(label='Step size', value=32) stepSize
@Integer(label='Lower energy border', value=64) energyBorderLow
@Integer(label='Upper energy border', value=64) energyBorderHigh
@Float(label='Energy position', value=0.5, min=0, max=1) energyPosition
@Integer(label='Polynominal order', value=3, min=1, max=8, step=1, style='slider') polynomialOrder
@Boolean(label='Use thresholding', value=True) useThresholding
@String(label='Threshold method', choices={'Li', 'Default', 'Huang', 'Intermodes', 'IsoData', 'Otsu'}, value='Li') threshold

file:       SR-EELS_Characterisation_(automated).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161216
info:       A script that automaticaly runs a characterisation on SR-EELS images.
            It detects all data set were no result for the selected settings are available
            and runs the the characterisation to complete the set of results.
"""

import os
import re
import math

from java.util import ArrayList
from java.io import File
from java.lang import RuntimeException

from ij import IJ

from de.m_entrup.EFTEMj_SR_EELS.shared import SR_EELS_ConfigurationManager
from de.m_entrup.EFTEMj_SR_EELS.importer import SR_EELS_ImportPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationSettings
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationResults

config = SR_EELS_ConfigurationManager.getConfiguration()
types = SR_EELS_CharacterisationResults.TIFF
types += SR_EELS_CharacterisationResults.JPEG
types += SR_EELS_CharacterisationResults.CSV
types += SR_EELS_CharacterisationResults.PLOTS

class AutomatedCharacterisation:

    def __init__(self):
        self.path = config.getString(SR_EELS_ImportPlugin.databasePathKey)
        self.datasets = []

    def create_dataset_list(self):
        for root, dirs, files in os.walk(self.path):
            #print root
            if re.search('SM\d+(\.\d+)?[/\\\\]-?\d+(\.\d+)?$', root):
                for directory in dirs:
                    self.datasets.append(os.path.join(root, directory))


def main():
    automated_characterisation = AutomatedCharacterisation()
    automated_characterisation.create_dataset_list()
    settings = SR_EELS_CharacterisationSettings()
    settings.stepSize = stepSize
    settings.filterRadius = math.sqrt(stepSize)
    settings.energyBorderLow = energyBorderLow
    settings.energyBorderHigh = energyBorderHigh
    settings.energyPosition = energyPosition
    settings.polynomialOrder = polynomialOrder
    settings.useThresholding = useThresholding
    settings.threshold = threshold
    finished = skipped = failed = 0
    dataset_count = len(automated_characterisation.datasets)
    IJ.log('Processing %d datasets.' % (dataset_count,))
    count = 0
    for dataset in automated_characterisation.datasets:
        count += 1
        if not os.path.exists(os.path.join(dataset, settings.toString())):
            IJ.log('%d/%d Starting to process "%s".' % (count, dataset_count, dataset))
            images = ArrayList()
            for image in os.listdir(dataset):
                if re.search('Cal_\d+\.tif', image):
                    images.add(image)
            settings.images = images
            settings.path = File(dataset)
            try:
                characterisation = SR_EELS_CharacterisationPlugin()
                characterisation.performCharacterisation(settings)
                results_path = File(settings.path, settings.toString())
                characterisation.saveResults(results_path, types)
                finished += 1
            except RuntimeException:
                IJ.log('%d/%d There was an error when processing "%s"!' % (count, dataset_count, dataset))
                failed += 1
        else:
            IJ.log('%d/%d Skipping "%s" as results for "%s" exist.' % (count, dataset_count, dataset, settings.toString()))
            skipped += 1
    IJ.log('\nFinished automated SR-EELS characterisation:')
    IJ.log('Finished: %d\nSkipped: %d\nFailed: %d' % (finished, skipped, failed))

if __name__ == '__main__':
    main()
