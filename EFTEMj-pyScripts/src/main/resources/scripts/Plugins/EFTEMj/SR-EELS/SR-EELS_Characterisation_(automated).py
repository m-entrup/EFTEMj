'''
@Integer(label='Step size', value='64') stepSize
@Integer(label='Lower energy border', value='128') energyBorderLow
@Integer(label='Upper energy border', value='128') energyBorderHigh
@Float(label='Energy position', value='0.5') energyPosition
@Integer(label='Polynominal order', value='3') polynomialOrder
@Boolean(label='Use thresholding', value=True) useThresholding
@String(label='Threshold method', choices={'Default','Huang','Li','Mean','Otsu','Yen'}) threshold
'''

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

def todo():
    settings.path = File(self.config.path)
    runCharacterisation()
    settings.images = getImages(settings)
    this.results = SR_EELS_CharacterisationResults()
    results.settings = settings
    saveResults()

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
    IJ.log('Processing %d datasets.' % (len(automated_characterisation.datasets),))
    for dataset in automated_characterisation.datasets:
        if not os.path.exists(os.path.join(dataset, settings.toString())):
            IJ.log('Starting to process "%s".' % (dataset,))
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
                IJ.log('There was an error when processing "%s"!' % (dataset,))
                failed += 1
        else:
            IJ.log('Skipping "%s" as results for "%s" exist.' % (dataset, settings.toString()))
            skipped += 1
    IJ.log('\nFinished automated SR-EELS characterisation:')
    IJ.log('Finished: %d\nSkipped: %d\nFailed: %d' % (finished, skipped, failed))

if __name__ == '__main__':
    main()