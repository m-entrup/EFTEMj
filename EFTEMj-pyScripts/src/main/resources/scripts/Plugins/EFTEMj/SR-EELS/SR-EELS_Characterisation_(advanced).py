"""
@Integer(label='Step size', value=32) STEP_SIZE
@Integer(label='Lower energy border', value=64) ENERGY_BORDER_LOW
@Integer(label='Upper energy border', value=64) ENERGY_BORDER_HIGH
@Float(label='Energy position', value=0.5, min=0, max=1) ENERGY_POSITION
@Integer(label='Polynominal order', value=3, min=1, max=8, step=1, style='slider') POLY_ORDER
@Boolean(label='Use thresholding', value=True) USE_THRESHOLDING
@String(label='Thresholding method', choices={'Li', 'Default', 'Huang', 'Intermodes', 'IsoData', 'Otsu'}, value='Li') THRESHOLDING_METHOD
@File(label='Select input directory', style='directory', description='An directory containing images for the SR-EELS characterisation.') DIR_INPUT
@Boolean(label='Use custom output directory', value=False) USE_CUSTOM_OUTPUT
@File(label='Select output directory', style='directory', description='An directory to safe the results of the SR-EELS characterisation.') DIR_OUTPUT
@String(label='Rotate images', choices = {'left', 'right', 'none'}, value='none', description='Rotation is needed if the energy dispersive axis is not the x axis.') DO_ROTATE
@Boolean(label='Create JPEG', value=True, description='The analysed images are saved as JPEGs with the detected borders marked.') CREATE_JPEG
@Boolean(label='Create plots', value=True, description='Plots are created that sumarize the characterisation results.') CREATE_PLOTS
@Boolean(label='Create TIFF', value=True, description='Save the results as a tiff dataset. These files are used by the SR-EEL correction.') CREATE_TIFF
@Boolean(label='Create csv', value=True, description='Save the results as a csv dataset.') CREATE_CSV

file:       SR-EELS_Characterisation_(advanced).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script that runs a characterisation on SR-EELS images.
            It is designed to extend the functionality of the SR_EELS_CharacterisationPlugin.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

import math

# pylint: disable-msg=E0401
from java.io import File

from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationSettings
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationResults
# pylint: enable-msg=E0401


def get_task():
    """Create a new instance of SR_EELS_CharacterisationPlugin and return it.
    """
    task = SR_EELS_CharacterisationPlugin()
    # pylint: disable-msg=E0602
    task.setRotation(DO_ROTATE)
    # pylint: enable-msg=E0602
    return task


def get_settings():
    """Create a new instance of SR_EELS_CharacterisationSettings and return it.
    """
    # pylint: disable-msg=E0602
    settings = SR_EELS_CharacterisationSettings()
    settings.stepSize = STEP_SIZE
    settings.filterRadius = math.sqrt(STEP_SIZE)
    settings.energyBorderLow = ENERGY_BORDER_LOW
    settings.energyBorderHigh = ENERGY_BORDER_HIGH
    settings.energyPosition = ENERGY_POSITION
    settings.polynominalOrder = POLY_ORDER
    settings.useThresholding = USE_THRESHOLDING
    settings.threshold = THRESHOLDING_METHOD
    settings.path = DIR_INPUT
    # pylint: enable-msg=E0602
    return settings


def run_script():
    """Function to be run when this file is used as a script
    """
    task = get_task()
    settings = get_settings()
    settings.images = task.getImages(settings)
    task.performCharacterisation(settings)
    flags = 0
    # pylint: disable-msg=E0602
    if CREATE_JPEG:
        flags += SR_EELS_CharacterisationResults.JPEG
    if CREATE_PLOTS:
        flags += SR_EELS_CharacterisationResults.PLOTS
    if CREATE_TIFF:
        flags += SR_EELS_CharacterisationResults.TIFF
    if CREATE_CSV:
        flags += SR_EELS_CharacterisationResults.CSV
    if USE_CUSTOM_OUTPUT:
        final_output_dir = File(DIR_OUTPUT, settings.toString())
    else:
        final_output_dir = File(settings.path, settings.toString())
    task.saveResults(final_output_dir, flags)
    # pylint: enable-msg=E0602


if __name__ == '__main__':
    run_script()
