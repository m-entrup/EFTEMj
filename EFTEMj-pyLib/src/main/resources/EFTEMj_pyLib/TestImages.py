'''
file:       TestImages.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20160930
info:       This module creates test images.
'''

from __future__ import division

from ij import IJ

class DriftTestImageCreator:
    '''
    This class class creates drift test images of differnt kind.
    The test images are intended to check if a drift correction algorithm works fine.

    The simple images are intended to test cross correlation based algorithms.
    The complex images are intended to test all kinds of algorithms.
    '''


    def __init__(self):
        self.images = []
        self.noise = 32
        self.signal_to_noise = 5
        self.dim_simple = (512, 512)
        self.roi_simple = (192, 192, 128, 128)
        # The image NilBend.jpg has a size of 4200x4200. This is the center.
        self.roi_complex = (600, 600, 3000, 3000)


    def create_images(self, count=3, mode='simple'):
        '''Creates N images to test the drift correction.
        :param count: the number of images to create (default: 3)
        :param mode: the type of images to create (options: 'simple', 'complex'; default: 'simple')
        '''
        IJ.showProgress(0)
        if not len(self.images) == 0:
            self.images = []
        if mode == 'simple':
            self._create_simple_images(count)
        if mode == 'complex':
            self._create_complex_images(count)


    def _create_simple_images(self, count):
        w, h = self.dim_simple
        ref = IJ.createImage('Ref', '32-bit black', w, h, 1)
        self._create_rect_and_noise(ref, self.roi_simple)
        self.images.append(ref)
        IJ.showProgress(1 / count)
        from random import randint
        for i in range(2, count + 1):
            roi_off = list(self.roi_simple[:])
            roi_off[0] = randint(0.5 * self.roi_simple[0], 1.5 * self.roi_simple[0])
            roi_off[1] = randint(0.5 * self.roi_simple[0], 1.5 * self.roi_simple[0])
            imp = IJ.createImage('Img', '32-bit black', w, h, 1)
            self._create_rect_and_noise(imp, roi_off)
            imp.setTitle('%d,%d' % (roi_off[0] - self.roi_simple[0], roi_off[1] - self.roi_simple[1]))
            self.images.append(imp)
            IJ.showProgress(i / count)


    def _create_complex_images(self, count):
        source = IJ.openImage('http://imagej.nih.gov/ij/images/NileBend.jpg')
        IJ.run(source, '32-bit', '')
        ref = self._crop_and_noise(source, self.roi_complex)
        ref.setTitle('Ref')
        self.images.append(ref)
        IJ.showProgress(1 / count)
        from random import randint
        for i in range(2, count + 1):
            roi_off = list(self.roi_complex[:])
            roi_off[0] = randint(0.5 * self.roi_complex[0], 1.5 * self.roi_complex[0])
            roi_off[1] = randint(0.5 * self.roi_complex[1], 1.5 * self.roi_complex[1])
            imp = self._crop_and_noise(source, roi_off)
            imp.setTitle('%d,%d' % (self.roi_complex[0] - roi_off[0], self.roi_complex[1] - roi_off[1]))
            self.images.append(imp)
            IJ.showProgress(i / count)
        source.close()


    def get_images(self):
        '''Returns the test images as a list of ImagePlus objects.
        '''
        return self.images


    def _crop_and_noise(self, imp, roi):
        imp.setRoi(*roi)
        cropped = imp.crop()
        IJ.run(cropped, 'Add Specified Noise...', 'standard=%d' % (self.noise,))
        IJ.run(cropped, 'Enhance Contrast', 'saturated=0.35')
        cropped.changes = False
        return cropped


    def _create_rect_and_noise(self, imp, roi):
        imp.setRoi(*roi)
        IJ.run(imp, 'Add...', 'value=%d' % (self.noise * self.signal_to_noise,))
        IJ.run(imp, 'Select None', '')
        IJ.run(imp, 'Add Specified Noise...', 'standard=%d' % (self.noise,))
        IJ.run(imp, 'Enhance Contrast', 'saturated=0.35')
        imp.changes = False


'''
Testing section:
'''
if __name__ == '__main__':
    creator = DriftTestImageCreator()
    from random import randrange
    count_simple = randrange(1, 100)
    print('Creating %d simple test images...' % (count_simple,))
    creator.create_images(count=count_simple, mode='simple')
    imgs_simple = creator.get_images()
    assert(len(imgs_simple) == count_simple)
    for img in imgs_simple:
        assert(img.getWidth() == 512 and img.getHeight() == 512)
    count_complex = randrange(1, 10)
    print('Creating %d complex test images...' % (count_complex,))
    creator.create_images(count=count_complex, mode='complex')
    imgs_complex = creator.get_images()
    assert(len(imgs_complex) == count_complex)
    for img in imgs_complex:
        assert(img.getWidth() == 3000 and img.getHeight() == 3000)
    print('Tests completed.')