"""
file:       Calculate_Jump-ratio.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script that calculates the Jump-Ratio of two images.
            The second image is devided by the first one.
            A drift correction is performed. The first image is shiftet towards to the second one.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

from __future__ import with_statement, division

# pylint: disable-msg=E0401
from EFTEMj_pyLib import CorrectDrift as drift
from EFTEMj_pyLib import Tools as tools

from ij import IJ, WindowManager
from ij.gui import GenericDialog
from ij.plugin import ImageCalculator
from ij.process import ImageStatistics as Stats
# pylint: enable-msg=E0401


def get_setup():
    """ Returns the drift correction mode and two image."""
    options = drift.get_options()
    modes = drift.get_modes()
    dialog = GenericDialog('Jump-ratio setup')
    dialog.addMessage('Select the mode  for drift correction\n' +
                      'and the images to process.')
    dialog.addChoice('Mode:', options, options[0])
    image_ids = WindowManager.getIDList()
    if not image_ids or len(image_ids) < 2:
        return [None] * 3
    image_titles = [WindowManager.getImage(id).getTitle() for id in image_ids]
    dialog.addMessage('Post-edge is divided by the pre-edge.')
    dialog.addChoice('Pre-edge', image_titles, image_titles[0])
    dialog.addChoice('Post-edge', image_titles, image_titles[1])
    dialog.showDialog()
    if dialog.wasCanceled():
        return [None] * 3
    mode = modes[dialog.getNextChoiceIndex()]
    img1 = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    img2 = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    return mode, img1, img2


def run_script():
    """Function to be run when this file is used as a script"""
    selected_mode, img1_in, img2_in = get_setup()
    if not selected_mode:
        return
    corrected_stack = drift.get_corrected_stack(
        (img1_in, img2_in), mode=selected_mode)
    img1, img2 = tools.stack_to_list_of_imp(corrected_stack)
    img_ratio = ImageCalculator().run('Divide create', img2, img1)
    img_ratio.setTitle('Jump-ratio [%s divided by %s]' % (img2.getShortTitle(),
                                                          img1.getShortTitle()
                                                         )
                      )
    img_ratio.changes = True
    img_ratio.copyScale(img1_in)
    img_ratio.show()
    IJ.run(img_ratio, 'Enhance Contrast', 'saturated=0.35')
    # We want to optimise the lower displaylimit:
    minimum = img_ratio.getProcessor().getMin()
    maximum = img_ratio.getProcessor().getMax()
    stat = img_ratio.getStatistics(Stats.MEAN + Stats.STD_DEV)
    mean = stat.mean
    stdv = stat.stdDev
    if minimum < mean - stdv:
        if mean - stdv >= 0:
            img_ratio.getProcessor().setMinAndMax(mean - stdv, maximum)
        else:
            img_ratio.getProcessor().setMinAndMax(0, maximum)
        img_ratio.updateAndDraw()

if __name__ == '__builtin__':
    run_script()
