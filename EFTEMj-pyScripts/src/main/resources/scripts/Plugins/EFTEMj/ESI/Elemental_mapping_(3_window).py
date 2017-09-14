"""
file:       Elemental_mapping_(3-window).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script that calculates the elemental map of three images.
            The code is based on an article of A. Berger and H. Kohl.
            Optik 92 (1993), p. 175 - 193
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

from __future__ import with_statement, division

# pylint: disable-msg=E0401
from EFTEMj_pyLib import CorrectDrift as drift
from EFTEMj_pyLib import Tools as tools
from EFTEMj_pyLib import ElementalMapping

from ij import IJ, WindowManager
from ij.gui import GenericDialog
# pylint: enable-msg=E0401


def get_setup():
    """ Returns the drift correction mode and three image.
    The order of return values is drift detection mode, pre-edge 1, pre-edge 2, post-edge.
    """
    options = drift.get_options()
    modes = drift.get_modes()
    dialog = GenericDialog('3-window-method setup')
    dialog.addMessage('Select the mode  for drift correction\n' +
                      'and the images to process.')
    dialog.addChoice('Mode:', options, options[0])
    image_ids = WindowManager.getIDList()
    if not image_ids or len(image_ids) < 3:
        IJ.showMessage("To run the 3-window-method\n" +
                       "you need to open 3 images:\n" +
                       "   Pre-edge 1,\n   Pre-edge 2 and\n   Post-edge")
        return [None] * 4
    image_titles = [WindowManager.getImage(id).getTitle() for id in image_ids]
    dialog.addMessage('Post-edge is divided by the pre-edge.')
    dialog.addChoice('Pre-edge 1', image_titles, image_titles[0])
    dialog.addChoice('Pre-edge 2', image_titles, image_titles[1])
    dialog.addChoice('Post-edge', image_titles, image_titles[2])
    dialog.showDialog()
    if dialog.wasCanceled():
        return [None] * 4
    mode = modes[dialog.getNextChoiceIndex()]
    img1 = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    img2 = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    img3 = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    return mode, img1, img2, img3


def run_script():
    """Function to be run when this file is used as a script
    """
    selected_mode, pre1_in, pre2_in, post_in = get_setup()
    if not selected_mode:
        return
    corrected_stack = drift.get_corrected_stack(
        (pre1_in, pre2_in, post_in), mode=selected_mode)
    pre1_imp, pre2_imp, post_imp = tools.stack_to_list_of_imp(corrected_stack)

    mapping = ElementalMapping.ThreeWindow(pre1_imp, pre2_imp, post_imp)
    map_imp, snr_imp, r_imp, lna_imp = mapping.get_result()

    for imp in (map_imp, snr_imp, r_imp, lna_imp):
        imp.copyScale(post_in)
        show_imp(imp)


def show_imp(imp):
    """Displays the given ImagePlus.

    The image is marked as modified.
    The contrast gets enhanced.
    """
    imp.changes = True
    IJ.run(imp, 'Enhance Contrast', 'saturated=0.35')
    imp.show()

if __name__ in ('__main__', '__builtin__'):
    run_script()
