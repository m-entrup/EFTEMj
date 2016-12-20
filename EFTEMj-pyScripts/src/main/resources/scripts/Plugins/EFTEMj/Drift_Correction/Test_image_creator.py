"""
@String(value='Create N image that can be used to test a drift correction.', visibility='MESSAGE') message
@String(value='simple: a bright rectengle and dark background', visibility='MESSAGE') message_simple
@String(value='complex: a excerpt of the sample image Nile Bend', visibility='MESSAGE') message_complex
@Short(label='Number of images', value=3, min=2, max=100, stepSize=1, style="slider") count
@String(label='Mode', choices={'simple', 'complex'}) mode

file:       Test_image_creator.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161017
info:       A script that creates images to test the drift correction.
"""

from __future__ import with_statement, division

# pylint: disable-msg=E0401
from EFTEMj_pyLib.TestImages import DriftTestImageCreator as Creator
# pylint: enable-msg=E0401


def run_script():
    creator = Creator()
    creator.create_images(count=count, mode=mode)
    for imp in creator.get_images():
        imp.show()


if __name__ == '__main__':
    run_script()
