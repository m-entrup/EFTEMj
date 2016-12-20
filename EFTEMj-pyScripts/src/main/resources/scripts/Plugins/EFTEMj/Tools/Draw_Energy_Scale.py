"""
@ImagePlus imp
@Integer(label='Minimal width', value=1024) width_min
@Integer(label='Font size', value=24) font_size
@Integer(label='Tick count', value=5) tick_count

file:       Draw_Energy_Scale.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20161216
info:       A script to add an horizontal energy scale to a given image.
            The image gets extended by the hight of the scale.
            The scale is an overlay.
"""

from __future__ import division

import math

from java.awt import Font, Color

from ij import IJ
from ij.gui import Overlay, Line, TextRoi
from ij.measure import Calibration

font = Font('SansSerif', Font.PLAIN, font_size)
font_offset = round(font_size / 5)
extend_full = 3 * font_size + 3 * font_offset
extend_label = 1 * font_size + 1 * font_offset
dispersion = 1
offset = 0

def get_scaled_image():
    global dispersion, offset
    dispersion = imp.getCalibration().pixelWidth
    offset = imp.getCalibration().xOrigin
    bin = 1
    while bin * width_min < imp.getWidth():
        bin *= 2
    if bin > 1:
        bin /= 2
    IJ.run(imp, 'Select None', '')
    imp_new = imp.crop()
    IJ.run(imp_new, 'Bin...', 'x=%d y=%d bin=Average' % (bin, bin))
    dispersion *= bin
    offset /= bin
    return imp_new

def extend_image(image):
    IJ.run(image, 'Select All', '')
    IJ.run(image, 'Copy', '')
    width = image.getWidth()
    height = int(image.getHeight() + extend_full)
    image_new = IJ.createImage(imp.getTitle(), '32-bit black', width, height, 1)
    image_new.setRoi(0, 0, image.getWidth(), image.getHeight())
    IJ.run(image_new, 'Paste', '')
    IJ.run(image_new, 'Enhance Contrast', 'saturated=0.35')
    IJ.run(image_new, 'Select None', '')
    cal = Calibration(image)
    cal.pixelWidth = dispersion
    cal.xOrigin = offset
    image_new.setCalibration(cal)
    return image_new

def draw_scale(image, ticks):
    cal = image.getCalibration()
    overlay = Overlay()
    image.setOverlay(overlay)
    TextRoi.setGlobalJustification(TextRoi.CENTER)
    offset = image.getHeight() - extend_full
    tick_offset = offset + font_size + font_offset
    for tick in ticks:
        tick_pos = cal.getRawX(tick)
        line = Line(tick_pos, offset, tick_pos, offset + font_size)
        line.setWidth(font_size // 8)
        line.setStrokeColor(Color(1.00, 1.00, 1.00))
        overlay.add(line)
        text = TextRoi(tick_pos, tick_offset, str(tick), font)
        text_width = text.getFloatWidth()
        text_y = text.getYBase()
        text.setLocation(tick_pos - text_width/2, text_y)
        text.setStrokeColor(Color(1.00, 1.00, 1.00))
        overlay.add(text)

def draw_label(image):
    overlay = image.getOverlay()
    TextRoi.setGlobalJustification(TextRoi.CENTER)
    offset = image.getHeight() - extend_label
    label_pos = image.getWidth() / 2
    text = TextRoi(label_pos, offset, 'Energy loss [eV]', font)
    text_width = text.getFloatWidth()
    text_y = text.getYBase()
    text.setLocation(label_pos - text_width/2, text_y)
    text.setStrokeColor(Color(1.00, 1.00, 1.00))
    overlay.add(text)

def create_ticks(image):
    cal = image.getCalibration()
    tick_max = cal.getX(image.getWidth() - 1)
    tick_min = cal.getX(0)
    x_range = tick_max - tick_min
    tick = x_range / (tick_count + 1)
    x = math.ceil(math.log10(tick)-1)
    pow10x = math.pow(10, x)
    tick_rounded = math.ceil(tick / pow10x) * pow10x
    tick_part = tick_rounded / 2
    centre = math.ceil(cal.getX(image.getWidth() / 2) / tick_part) * tick_part
    if tick_count % 2 == 0:
        centre += tick_part
    ticks = [centre]
    print 'round', tick_rounded
    tick = centre + tick_rounded
    print 'tick >', tick
    while tick < tick_max:
        ticks.append(tick)
        tick += tick_rounded
    tick = centre - tick_rounded
    print 'tick <', tick
    while tick > tick_min:
        ticks.append(tick)
        tick -= tick_rounded
    print len(ticks)
    assert len(ticks) <= tick_count + 1
    sorted(ticks)
    print ticks
    ticks_nice = []
    for tick in ticks:
        if tick % 1 == 0:
            ticks_nice.append(int(tick))
        else:
            ticks_nice.append(tick)    
    print ticks_nice
    return ticks_nice

def main():
    imp_scaled = get_scaled_image()
    imp_extended = extend_image(imp_scaled)
    ticks = create_ticks(imp_extended)
    draw_scale(imp_extended, ticks)
    draw_label(imp_extended)
    imp_extended.show()


if __name__ == '__main__':
    main()