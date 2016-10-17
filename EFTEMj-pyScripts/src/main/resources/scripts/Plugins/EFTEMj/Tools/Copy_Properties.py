'''
file:       Copy Properties.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161017
info:       A script that copies different properties from one ImagePlus to another.
'''

from __future__ import with_statement, division

# pylint: disable-msg=E0401
from ij import IJ, WindowManager
from ij.gui import GenericDialog
# pylint: enable-msg=E0401


def get_setup():
    '''Returns two ImagePlus objects and a dictionary of properties to copy.'''
    dialog = GenericDialog('Copy Properties setup')
    dialog.addMessage('Select the source and target image and the properties to copy.')
    image_ids = WindowManager.getIDList()
    if not image_ids or len(image_ids) < 2:
        IJ.showMessage('Two or more images are necessary to use this plugin.')
        return [None]*3
    image_titles = [WindowManager.getImage(id).getTitle() for id in image_ids]
    dialog.addChoice('Source', image_titles, image_titles[0])
    dialog.addChoice('Target', image_titles, image_titles[1])
    dialog.addCheckbox('Copy Calibration', True)
    dialog.addCheckbox('Copy Slice Labels', False)
    dialog.showDialog()
    if dialog.wasCanceled():
        return [None]*3
    source = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    target = WindowManager.getImage(image_ids[dialog.getNextChoiceIndex()])
    choices = {'cal': dialog.getNextBoolean(),
               'labels': dialog.getNextBoolean()
              }
    return source, target, choices


def run_script():
    '''Function to be run when this file is used as a script'''
    source, target, options = get_setup()
    if not options:
        return
    print(options)
    if options['cal']:
        #IJ.showMessage('Copying calibration')
        calibration = source.getCalibration()
        target.setCalibration(calibration)
        target.updateAndRepaintWindow()
    if options['labels']:
        if source.getStackSize() > 1:
            #IJ.showMessage('Copying labels')
            labels = [source.getImageStack().getSliceLabel(n)
                      for n in range(1, source.getStackSize() + 1)]
            for index, label in enumerate(labels):
                if not label is None:
                    target.getImageStack().setSliceLabel(label, index + 1)

if __name__ == '__main__':
    run_script()
