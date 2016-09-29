'''
file:       CrossCorrelation.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20160720
info:       This module calculates the normalised Cross-correlation of two images.
            There are aditional functions to style the result or find the position of the maximum.
'''

from __future__ import with_statement, division

import math

from EFTEMj_pyLib import Tools as tools

from ij import IJ, WindowManager, ImagePlus
from ij.process import ImageStatistics as Stats, FHT
from ij.gui import Line, PointRoi
from ij.plugin import CanvasResizer

def _calc_correlation(img1, img2):
    '''
    Return the cross correlation between the given images.
    The same steps are used as in FFTMath.
    :param img1: The ImagePlus to be used as reference.
    :param img2: The ImagePlus its correlation to the first ImagePlus is calculated.
    '''
    fht1 = img1.getProperty('FHT')
    if not fht1 is None:
        h1 = FHT(fht1)
    else:
        h1 = FHT(img1.getProcessor())
    fht2 = img2.getProperty('FHT')
    if not fht2 is None:
        h2 = FHT(fht2)
    else:
        h2 = FHT(img2.getProcessor())
    if not h1.powerOf2Size():
        IJ.error('FFT Math', 'Images must be a power of 2 size (256x256, 512x512, etc.)')
        return
    if not img1.getWidth() == img2.getWidth():
        IJ.error('FFT Math', 'Images must be the same size')
        return
    if fht1 is None:
        IJ.showStatus('Transform image1')
        h1.transform()
    if fht2 is None:
        IJ.showStatus('Transform image2')
        h2.transform()
    IJ.showStatus('Complex conjugate multiply')
    result = h2.conjugateMultiply(h1)
    IJ.showStatus('Inverse transform')
    result.inverseTransform()
    result.swapQuadrants()
    result.resetMinAndMax()
    IJ.showProgress(1.0)
    return ImagePlus('Result', result)

def perform_correlation(img1, img2):
    '''
    Return an ImagePlus that represents the normalized CrossCorrelation of two images.
    :param img1: The ImagePlus to be used as reference.
    :param img2: The ImagePlus its correlation to the first ImagePlus is calculated.
    '''
    norm = 1
    for img in (img1, img2):
        copy = img.duplicate()
        IJ.run(copy, 'Square', '')
        stat = copy.getStatistics(Stats.MEAN)
        norm *= math.sqrt(stat.umean) * math.sqrt(img.getWidth()) * math.sqrt(img.getHeight())
    # Include image names in square brackets to handle file names with spaces.
    suffix = 1
    prefix = 'Result'
    title = prefix
    while WindowManager.getImage(title):
        title = prefix + '-' + str(suffix)
        suffix += 1
    result = _calc_correlation(img1, img2)
    '''
    Previous version:
    IJ.run(img1, 'FD Math...', 'image1=[' + img2.getTitle() +
           '] operation=Correlate image2=[' + img1.getTitle()
           + '] result=[' + title + ']  do');
    result = WindowManager.getImage(title)
    Alternative to IJ.run where no configuration is possible:
    cc = FFTMath()
    cc.doMath(img1, img2)
    '''
    IJ.run(result, 'Divide...', 'value=' + str(norm))
    IJ.run(result, 'Enhance Contrast', 'saturated=0.0')
    return result


def style_cc(cc_img):
    '''
    Styles an ImagePlus that shows a CrossCorrelation.
    The maximum is marked by a point selection.
    Scale bar and intensity calibration are added.
    :param cc_img: An ImagePlus that shows a CrossCorrelation.
    '''
    new = ''
    stat = cc_img.getStatistics(Stats.MIN_MAX)
    minimum = round(50 * stat.min) / 50
    maximum = round(50 * stat.max) / 50
    cc_img.getProcessor().setMinAndMax(minimum, maximum)
    if cc_img.getWidth() < 512:
        scale = 2
        while cc_img.getWidth() * scale < 512:
            scale *= 2
        title = cc_img.getTitle()
        new = IJ.run(cc_img, 'Scale...',
                     'x=' + scale + ' y=' + scale + ' z=1.0 interpolation=None create')
        cc_img.close()
        new.rename(title)
        cc_img = new
    width = cc_img.getWidth()
    height = cc_img.getHeight()
    IJ.run(cc_img, 'Remove Overlay', '')
    cc_img.setRoi(Line(0.5 * width, 0, 0.5 * width, height))
    IJ.run(cc_img, 'Add Selection...', '')
    cc_img.setRoi(Line(0, 0.5 * height, width, 0.5 * height))
    IJ.run(cc_img, 'Add Selection...', '')
    IJ.run(cc_img, 'Find Maxima...', 'noise=' + str(width / 4) + ' output=[Point Selection]')
    IJ.run(cc_img, 'Add Selection...', '')
    IJ.run(cc_img, 'Select None', '')
    __create_scalebar(cc_img)
    __create_calbar(cc_img)
    if not cc_img.isVisible():
        cc_img.show()

def get_max(cc_img):
    '''
    Finds the maximum of an image and returns it as a list of the length 2.
    This function is designed for use on CrossCorrelation images.
    :param cc_img: An ImagePlus showing a CrossCorrelation.
    '''
    width = cc_img.getWidth()
    IJ.run(cc_img, 'Find Maxima...', 'noise=' + str(width / 4) + ' output=[Point Selection]')
    roi = cc_img.getRoi()
    if roi.getClass() == PointRoi:
        return (roi.getBounds().x, roi.getBounds().y)
    else:
        return (None, None)

def get_shift(cc_img):
    '''
    Finds the maximum of an image and returns the offset to the centre.
    This function is designed for use on CrossCorrelation images.
    :param cc_img: An ImagePlus showing a CrossCorrelation.
    '''
    return get_drift(cc_img)

def get_drift(cc_img):
    '''
    Finds the maximum of an image and returns the offset to the centre.
    This function is designed for use on CrossCorrelation images.
    :param cc_img: An ImagePlus showing a CrossCorrelation.
    '''
    max_x, max_y = get_max(cc_img)
    x_off = max_x - cc_img.getWidth() / 2
    y_off = max_y - cc_img.getHeight() / 2
    return x_off, y_off

def __create_scalebar(imp):
    '''
    Creates a scale bar and adds it to an ImagePlus. Nothing is returned.
    :param imp: The imagePlus the scale bar is added to.
    '''
    width = imp.getWidth()
    font_size = width / 4096 * 150
    scale_bar_color = 'White'
    cal = imp.getCalibration()
    pixel_width = cal.getX(1.)
    if width * pixel_width > 10:
        bar_width = 10 * round(width * pixel_width / 80)
    elif width * pixel_width > 1:
        bar_width = math.floor((width * pixel_width / 8) + 1)
    else:
        bar_width = 0.01 * math.floor(100 * width * pixel_width / 8)

    bar_height = font_size / 3
    #print(bar_width, bar_height, font_size, scale_bar_color)
    scale_bar_settings = 'width=%d \
                          height=%d \
                          font=%d \
                          color=%s \
                          background=None \
                          location=[Lower Right] \
                          bold \
                          overlay'
    IJ.run(imp, 'Scale Bar...',
           scale_bar_settings % (bar_width, bar_height, font_size, scale_bar_color))


def __create_calbar(imp):
    '''
    Creates a calibration bar and adds it to an ImagePlus. Nothing is returned.
    :param imp: The imagePlus the calibration bar is added to.
    '''
    font_size = 10
    zoom = imp.getWidth() / 4096 * 10
    cal_bar_settings = 'location=[Upper Right] \
                        fill=White \
                        label=Black \
                        number=3 \
                        decimal=2 \
                        font=%d \
                        zoom=%d \
                        overlay'
    IJ.run(imp, 'Calibration Bar...', cal_bar_settings % (font_size, zoom))

def scale_to_power_of_two(images):
    ''' Renturns a list of images with width and height as power of two.
    The original image is centered.
    :param images: A list of images to process.
    '''
    dim = [(imp.getWidth(), imp.getHeight()) for imp in images]
    min_dim = min(tools.perform_func_on_list_of_tuples(min, dim))
    new_size = 2
    while new_size < min_dim / 2:
        new_size *= 2
    def resize(imp):
        '''Returns a rezized version of imp.
        Width and height will be a power of 2.
        '''
        x_off = int(math.floor((imp.getWidth() - new_size) / 2))
        y_off = int(math.floor((imp.getHeight() - new_size) / 2))
        # print x_off, y_off
        imp.setRoi(x_off, y_off, new_size, new_size)
        return imp.crop()
    return [resize(imp) for imp in images]

'''
Testing section:
'''
if __name__ == '__main__':
	imp1 = IJ.openImage("http://imagej.nih.gov/ij/images/TEM_filter_sample.jpg");
	imp2 = scale_to_power_of_two((imp1,))[0]
	assert(imp2.getWidth() == 256 and imp2.getHeight() == 256)
	print('Tests completed.')