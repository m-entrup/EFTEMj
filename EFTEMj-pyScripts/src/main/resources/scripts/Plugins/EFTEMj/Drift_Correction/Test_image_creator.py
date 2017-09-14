"""
@String(value='Create N image that can be used to test a drift correction.', visibility='MESSAGE') message
@String(value='simple: a bright rectengle and dark background', visibility='MESSAGE') message_simple
@String(value='complex: a excerpt of the sample image Nile Bend', visibility='MESSAGE') message_complex
@Short(label='Number of images', value=3, min=2, max=100, stepSize=1, style="slider") COUNT
@String(label='Mode', choices={'simple', 'complex'}) MODE

file:       Test_image_creator.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script that creates images to test the drift correction.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

from __future__ import with_statement, division

# pylint: disable-msg=E0401
from EFTEMj_pyLib.TestImages import DriftTestImageCreator as Creator
# pylint: enable-msg=E0401

# pylint: disable-msg=E0602
MODE = MODE
COUNT = COUNT
# pylint: enable-msg=E0602


def run_script():
    """Function to be run when this file is used as a script
    """
    creator = Creator()
    creator.create_images(count=COUNT, mode=MODE)
    for imp in creator.get_images():
        imp.show()


if __name__ in ('__main__', '__builtin__'):
    run_script()
