"""
@ImagePlus IMP
@Integer(label='Minimal width', value=1024) WIDTH_MIN
@Integer(label='Font size', value=24) FONT_SIZE
@Integer(label='Tick count', value=5) TICK_COUNT

file:       Draw_Energy_Scale.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170306
info:       A script to add an horizontal energy scale to a given image.
            The image gets extended by the hight of the scale.
            The scale is an overlay.
"""
# pylint: disable-msg=C0103
# pylint: enable-msg=C0103

from __future__ import division

import math

# pylint: disable-msg=E0401
from java.awt import Font, Color

from ij import IJ
from ij.gui import Overlay, Line, TextRoi
from ij.measure import Calibration
# pylint: enable-msg=E0401


# pylint: disable-msg=E0602
IMP = IMP
WIDTH_MIN = WIDTH_MIN
FONT_SIZE = FONT_SIZE
TICK_COUNT = TICK_COUNT
# pylint: enable-msg=E0602


def create_ticks(image, tick_count=5):
    """Return a list with nice tick values.
    :param image: The image the calibration is taken from.
    :param tick_count: The preferd number of tick values to create (default: 5).
    It is not guaranteed that the returned list contains the given number of tick values.
    """
    cal = image.getCalibration()
    tick_max = cal.getX(image.getWidth() - 1)
    tick_min = cal.getX(0)
    x_range = tick_max - tick_min
    tick = x_range / (tick_count + 1)
    x_value = math.ceil(math.log10(tick) - 1)
    pow10x = math.pow(10, x_value)
    tick_rounded = math.ceil(tick / pow10x) * pow10x
    tick_part = tick_rounded / 2
    centre = math.ceil(cal.getX(image.getWidth() / 2) /
                       tick_part) * tick_part
    if tick_count % 2 == 0:
        centre += tick_part
    ticks = [centre]
    print('round', tick_rounded)
    tick = centre + tick_rounded
    print('tick >', tick)
    while tick < tick_max:
        ticks.append(tick)
        tick += tick_rounded
    tick = centre - tick_rounded
    print('tick <', tick)
    while tick > tick_min:
        ticks.append(tick)
        tick -= tick_rounded
    print(len(ticks))
    assert len(ticks) <= tick_count + 1
    sorted(ticks)
    print(ticks)
    ticks_nice = []
    for tick in ticks:
        if tick % 1 == 0:
            ticks_nice.append(int(tick))
        else:
            ticks_nice.append(tick)
    print(ticks_nice)
    return ticks_nice


class EnergyScaleCreator():
    """A class that offers methods to draw an energy scale.
    """

    def __init__(self, image, font_size=24):
        """Initialize the class by setting some sizes.
        :param font_size: Value calculations are based on (default: 24)
        """
        self.image = image
        self.font = Font('SansSerif', Font.PLAIN, font_size)
        self.font_offset = round(FONT_SIZE / 5)
        self.extend_full = 3 * FONT_SIZE + 3 * self.font_offset
        self.extend_label = 1 * FONT_SIZE + 1 * self.font_offset
        self.dispersion = 1
        self.offset = 0

    def get_scaled_image(self, min_width=1024):
        """Returns a scaled version of the given image.
        :param min_width: The minimal width of the scaled image (default: 1024).
        """
        self.dispersion = self.image.getCalibration().pixelWidth
        self.offset = self.image.getCalibration().xOrigin
        binning = 1
        while binning * min_width < self.image.getWidth():
            binning *= 2
        if binning > 1:
            binning /= 2
        IJ.run(self.image, 'Select None', '')
        new_image = self.image.crop()
        IJ.run(new_image, 'Bin...', 'x=%d y=%d binning=Average' %
               (binning, binning))
        self.dispersion *= binning
        self.offset /= binning
        self.image = new_image
        return new_image

    def extend_image(self):
        """Returns a new image with increased height.
        """
        IJ.run(self.image, 'Select All', '')
        IJ.run(self.image, 'Copy', '')
        width = self.image.getWidth()
        height = int(self.image.getHeight() + self.extend_full)
        new_image = IJ.createImage(
            IMP.getTitle(), '32-bit black', width, height, 1)
        new_image.setRoi(0, 0, self.image.getWidth(), self.image.getHeight())
        IJ.run(new_image, 'Paste', '')
        IJ.run(new_image, 'Enhance Contrast', 'saturated=0.35')
        IJ.run(new_image, 'Select None', '')
        cal = Calibration(self.image)
        cal.pixelWidth = self.dispersion
        cal.xOrigin = self.offset
        new_image.setCalibration(cal)
        self.image = new_image
        return new_image

    def draw_scale(self, ticks):
        """Draw a scale to the given image.
        :param ticks: A list of energy losses to use as ticks.
        """
        cal = self.image.getCalibration()
        overlay = Overlay()
        self.image.setOverlay(overlay)
        TextRoi.setGlobalJustification(TextRoi.CENTER)
        offset = self.image.getHeight() - self.extend_full
        tick_offset = offset + FONT_SIZE + self.font_offset
        for tick in ticks:
            tick_pos = cal.getRawX(tick)
            line = Line(tick_pos, offset, tick_pos, offset + FONT_SIZE)
            line.setWidth(FONT_SIZE // 8)
            line.setStrokeColor(Color(1.00, 1.00, 1.00))
            overlay.add(line)
            text = TextRoi(tick_pos, tick_offset, str(tick), self.font)
            text_width = text.getFloatWidth()
            text_y = text.getYBase()
            text.setLocation(tick_pos - text_width / 2, text_y)
            text.setStrokeColor(Color(1.00, 1.00, 1.00))
            overlay.add(text)
        self._draw_x_label()

    def _draw_x_label(self):
        """Draws the label of the x axis.
        """
        overlay = self.image.getOverlay()
        TextRoi.setGlobalJustification(TextRoi.CENTER)
        offset = self.image.getHeight() - self.extend_label
        label_pos = self.image.getWidth() / 2
        text = TextRoi(label_pos, offset, 'Energy loss [eV]', self.font)
        text_width = text.getFloatWidth()
        text_y = text.getYBase()
        text.setLocation(label_pos - text_width / 2, text_y)
        text.setStrokeColor(Color(1.00, 1.00, 1.00))
        overlay.add(text)


def run_script():
    """Function to be run when this file is used as a script
    """
    creator = EnergyScaleCreator(IMP, FONT_SIZE)
    creator.get_scaled_image(WIDTH_MIN)
    creator.extend_image()
    ticks = create_ticks(IMP, TICK_COUNT)
    creator.draw_scale(ticks)
    imp_extended = creator.image
    imp_extended.show()


if __name__ in ('__main__', '__builtin__'):
    run_script()
