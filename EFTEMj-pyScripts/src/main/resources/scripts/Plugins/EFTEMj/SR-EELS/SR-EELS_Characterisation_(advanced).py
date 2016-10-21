'''
@File(label='Select input directory', style='directory', description='An directory containing images for the SR-EELS characterisation.') in_dir
@File(label='Select output directory', style='directory', description='An directory to safe the results of the SR-EELS characterisation.') out_dir
@String(label='Rotate images', choices = {'left', 'right', 'none'}, description='Rotation is needed if the energy dispersive axis is not the x axis.') do_rotate
@Boolean(label='Create subdir', description='Create a directory for the results. The name includes the characterisation parameters.') create_subdir
@Boolean(label='Create JPEG', description='The analysed images are saved as JPEGs with the detected borders marked.') create_jpeg
@Boolean(label='Create plots', description='Plots are created that sumarize the characterisation results.') create_plots
@Boolean(label='Create TIFF', description='Save the results as a tiff dataset. These files are used by the SR-EEL correction.') create_tiff
@Boolean(label='Create csv', description='Save the results as a csv dataset.') create_csv

file:       SR-EELS_Characterisation_(advanced).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161021
info:       A script that runs a characterisation on SR-EELS images.
            It is designed to extend the functionality of the SR_EELS_CharacterisationPlugin.
'''
from java.io import File

from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationPlugin
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationSettings
from de.m_entrup.EFTEMj_SR_EELS.characterisation import SR_EELS_CharacterisationResults

from os import listdir

def main():
    task =  SR_EELS_CharacterisationPlugin()
    task.setRotation(do_rotate)
    settings = SR_EELS_CharacterisationSettings()
    settings.path = in_dir
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
    if create_subdir:
        final_out_dir = File(out_dir, settings.toString())
    else:
        final_out_dir = out_dir
    task.saveResults(final_out_dir, flags)


if __name__ == '__main__':
    main()
