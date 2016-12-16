'''
@Integer(label='Step size', value='32') stepSize
@Integer(label='Lower energy border', value='64') energyBorderLow
@Integer(label='Upper energy border', value='64') energyBorderHigh
@Float(label='Energy position', value='0.5') energyPosition
@Integer(label='Polynominal order', value='3') polynomialOrder
@Boolean(label='Use thresholding', value=True) useThresholding
@String(label='Threshold method', choices={'Li', 'Default', 'Huang', 'Intermodes', 'IsoData', 'Otsu'}, value='Li') threshold
@File(label='Select input directory', style='directory', description='An directory containing images for the SR-EELS characterisation.') in_dir
@Boolean(label='Use custom output directory', value='False') use_custom_output
@File(label='Select output directory', style='directory', description='An directory to safe the results of the SR-EELS characterisation.') out_dir
@String(label='Rotate images', choices = {'left', 'right', 'none'}, value='none', description='Rotation is needed if the energy dispersive axis is not the x axis.') do_rotate
@Boolean(label='Create JPEG', value=True, description='The analysed images are saved as JPEGs with the detected borders marked.') create_jpeg
@Boolean(label='Create plots, value=True', description='Plots are created that sumarize the characterisation results.') create_plots
@Boolean(label='Create TIFF', value=True, description='Save the results as a tiff dataset. These files are used by the SR-EEL correction.') create_tiff
@Boolean(label='Create csv', value=True, description='Save the results as a csv dataset.') create_csv

file:       SR-EELS_Characterisation_(advanced).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161216
info:       A script that runs a characterisation on SR-EELS images.
            It is designed to extend the functionality of the SR_EELS_CharacterisationPlugin.
'''
import math

from java.io import File

from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationSettings
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationResults

from os import listdir

def get_task():
    task =  SR_EELS_CharacterisationPlugin()
    task.setRotation(do_rotate)
    return task

def get_settings():
    settings = SR_EELS_CharacterisationSettings()
    settings.stepSize = stepSize
    settings.filterRadius = math.sqrt(stepSize)
    settings.energyBorderLow = energyBorderLow
    settings.energyBorderHigh = energyBorderHigh
    settings.energyPosition = energyPosition
    settings.polynomialOrder = polynomialOrder
    settings.useThresholding = useThresholding
    settings.threshold = threshold
    settings.path = in_dir
    return settings

def main():
    task =  get_task()
    settings = get_settings()
    settings.images = task.getImages(settings)
    task.performCharacterisation(settings)
    flags = 0
    if create_jpeg:
        flags += SR_EELS_CharacterisationResults.JPEG
    if create_plots:
        flags += SR_EELS_CharacterisationResults.PLOTS
    if create_tiff:
        flags += SR_EELS_CharacterisationResults.TIFF
    if create_csv:
        flags += SR_EELS_CharacterisationResults.CSV
    if use_custom_output:
        final_out_dir = File(out_dir, settings.toString())
    else:
        final_out_dir = File(settings.path, settings.toString())
    task.saveResults(final_out_dir, flags)


if __name__ == '__main__':
    main()
