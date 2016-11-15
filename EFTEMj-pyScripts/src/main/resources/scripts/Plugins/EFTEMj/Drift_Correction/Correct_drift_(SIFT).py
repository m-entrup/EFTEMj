'''
file:       Correct_drift_(SIFT).py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161017
info:       A script that corrects the drift between any number of images.
            Scale-invariant feature transform (SIFT) is used for drift detection.
            The images are not changed. A stack is created that holds the corrected images.
'''

from __future__ import with_statement, division

# pylint: disable-msg=E0401
from EFTEMj_pyLib import CorrectDrift as drift
from EFTEMj_pyLib import Tools as tools
# pylint: enable-msg=E0401


def run_script():
    '''Function to be run when this file is used as a script'''
    images = tools.get_images()
    if not images:
        return
    elif len(images) >= 2:
        corrected_stack = drift.get_corrected_stack(images, 'SIFT')
        corrected_stack.copyScale(images[0])
        corrected_stack.show()


if __name__ == '__main__':
    run_script()
