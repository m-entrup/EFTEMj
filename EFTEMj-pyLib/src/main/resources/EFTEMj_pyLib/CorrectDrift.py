'''
file:       CorrectDrift.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20160929
info:       This module will correct the drift between two images.
'''

from __future__ import with_statement, division

import operator, pprint, math

from EFTEMj_pyLib import CrossCorrelation as cc
from EFTEMj_pyLib import pySIFT
from EFTEMj_pyLib import Tools as tools

from ij import IJ, ImagePlus

def get_options():
    '''Returns a list of drift correction mode titles.'''
    return ['Scale-invariant feature transform', 'Normalized cross-correlation', 'None']

def get_modes():
    '''Return a list of modes that are available for drift correction.'''
    return ['SIFT', 'CC', 'None']

def correct_drift(img1, img2, display_cc=False):
    ''' Returns two ImagePlus objects that are drift corrected to each other.
    The first image is not changed.
    The second image is a new image that is shifted using ImageJ's Translate.
    :param img1: The reference image.
    :param img2: The image to be shifted.
    :param display_cc: Activate displaying the CrossCorrelation image (default False).
    '''
    img1_cc, img2_cc = cc.scale_to_power_of_two([img1, img2])
    result = cc.perform_correlation(img1_cc, img2_cc)
    x_off, y_off = cc.get_shift(result)
    # style after maximum detection
    if not display_cc:
        result.hide()
    else:
        result.copyScale(img1)
        cc.style_cc(result)
    if x_off == 0 and y_off == 0:
        print('No drift has been detected.')
        return img1, img2
    title = img2.getTitle()
    img2_dk = Duplicator().run(img2)
    img2_dk.setTitle('DK-' + title)
    IJ.run(img2_dk, 'Translate...', 'x=%d y=%d interpolation=None' % (-x_off, -y_off))
    img2_dk.copyScale(img2)
    img2_dk.show()
    return img1, img2_dk


def get_corrected_stack(images, mode='CC'):
    ''' Returns a drift corrected stack using the given method for drift detection.
    :param images: A list containing ImagePlus objects.
    :param mode: The method used for drift detection (e.g. CC, SIFT).
    '''
    if mode == 'None':
        return get_corrected_stack_using_vector(images, [(0, 0)] * len(images))
    drift_matrix = get_drift_matrix(images, mode)
    corrected_stack = get_corrected_stack_using_matrix(images, drift_matrix)
    return corrected_stack


def get_corrected_stack_linear(images, mode='CC'):
    ''' Returns a drift corrected stack using the given method for drift detection.
    This version uses a linear drift detection (image by image).
    :param images: A list containing ImagePlus objects.
    :param mode: The method used for drift detection (e.g. CC, SIFT).
    '''
    drift_vertor = [(0, 0)] * len(images)
    if mode == 'CC' or mode == 'SIFT':
        drift_vector = get_drift_vector(images, mode)
    corrected_stack = get_corrected_stack_using_vector(images, drift_vector)
    return corrected_stack


def get_drift_matrix(images, mode='CC'):
    ''' Returns the drift matrix of the given Images.
    For N images a NxN matrix is created.
    :param images: A list containing ImagePlus objects.
    :param mode: The method used for drift detection (e.g. CC, SIFT).
    '''
    if mode == 'CC':
        return get_drift_matrix_cc(images)
    elif mode == 'SIFT':
        return get_drift_matrix_sift(images)
    return None


def get_drift_matrix_cc(images):
    ''' Returns the drift matrix of the given images.
    Cross correlation is used for drift detection.
    :param images: A list containing ImagePlus objects.
    '''
    def get_drift(i, j, _images):
        '''Returns the drift of imagej compared to image i'''
        cc_img = cc.perform_correlation(_images[i], _images[j])
        offset = cc.get_drift(cc_img)
        ''' DEBUG
        print 'Reference: %s at index %i' % (images[i],i)
        print 'Drifted image: %s at index %i' % (images[j],j)
        print 'Offset: %d,%d\n' % offset
        '''
        return offset
    images_cc = cc.scale_to_power_of_two(images)
    drift_matrix = [[]] * len(images)
    for i, _ in enumerate(drift_matrix):
        drift_matrix[i] = [(0, 0)] * len(images)
    # print 'Initial matrix: ', drift_matrix
    for i in range(len(images)):
        # print  'i=%i: ' % i, range(i + 1, len(images))
        for j in range(i + 1, len(images)):
            img_drift = get_drift(i, j, images_cc)
            ''' DEBUG
            print 'Appending to %i/%i:' % (i, j)
            print shift
            '''
            # print 'Matrix element: ', i, j
            # print 'Adding value: ', img_drift
            drift_matrix[i][j] = img_drift
            drift_matrix[j][i] = tuple([-val for val in img_drift])
    # print 'Drift matrix:'
    # pprint(drift_matrix)
    return drift_matrix


def get_drift_matrix_sift(images):
    ''' Returns the drift matrix of the given images.
    Scale-invariant feature transform is used for drift detection.
    :param images: A list containing ImagePlus objects.
    '''
    sift = pySIFT.pySIFT(images)
    ''' DEBUG
    for x in sift.all_features:
        print(x.size())
    '''
    # pprint(sift.get_drift_matrix())
    return sift.get_drift_matrix()


def get_drift_vector(images, mode='CC'):
    ''' Returns the drift vector of the given Images.
    For N images a vector of length N is created.
    :param images: A list containing ImagePlus objects.
    :param mode: The method used for drift detection (e.g. CC, SIFT).
    '''
    if mode == 'CC':
        return get_drift_vector_cc(images)
    elif mode == 'SIFT':
        return get_drift_vector_sift(images)
    return None


def get_drift_vector_cc(images):
    ''' Returns the drift vector of the given images.
    Cross correlation is used for drift detection.
    :param images: A list containing ImagePlus objects.
    '''
    def get_drift(i, j, _images):
        '''Returns the drift of imagej compared to image i'''
        cc_img = cc.perform_correlation(_images[i], _images[j])
        offset = cc.get_drift(cc_img)
        ''' DEBUG
        print 'Reference: %s at index %i' % (images[i],i)
        print 'Drifted image: %s at index %i' % (images[j],j)
        print 'Offset: %d,%d\n' % offset
        '''
        return offset
    images_cc = cc.scale_to_power_of_two(images)
    drift_vector = [None] * len(images)
    drift_vector[0] = (0., 0.)
    # print 'Initial vector: ', drift_vector
    for i in range(1, len(images)):
        img_drift = get_drift(i - 1, i, images_cc)
        # print 'Vector element: ', i
        # print 'Adding value: ', img_drift
        drift_vector[i] = img_drift
        drift_vector[i] = tuple([a + b for a, b in zip(drift_vector[i - 1], drift_vector[i])])
    # print 'Drift vector:'
    # pprint(drift_vector)
    return drift_vector


def get_drift_vector_sift(images):
    ''' Returns the drift vector of the given images.
    Scale-invariant feature transform is used for drift detection.
    :param images: A list containing ImagePlus objects.
    '''
    sift = pySIFT.pySIFT(images)
    ''' DEBUG
    for x in sift.all_features:
        print(x.size())
    '''
    # pprint(sift.get_drift_matrix())
    return sift.get_drift_vector()


def get_corrected_stack_using_vector(images, drift_vector, suffix=''):
    ''' Returns a drift corrected stack using the given drift vector.
    :param images: A list of length N containing ImagePlus objects.
    :param drift_matrix: A list of length N that contains the measured drift.
    '''
    shift_vector = shift_vector_from_drift_vector(drift_vector)
    stack = tools.stack_from_list_of_imp(shift_images(images, shift_vector))
    title = 'Drift corrected stack'
    if suffix:
        title += ' (%s)' % (suffix)
    corrected_stack = ImagePlus(title, stack)
    return corrected_stack


def get_corrected_stack_using_matrix(images, drift_matrix, suffix=''):
    ''' Returns a drift corrected stack using the given drift matrix.
    :param images: A list of length N containing ImagePlus objects.
    :param drift_matrix: A NxN matrix that contains the measured drift between N images.
    '''
    drift_vector = drift_vector_from_drift_matrix(drift_matrix)
    return get_corrected_stack_using_vector(images, drift_vector, suffix)


def drift_vector_from_drift_matrix(drift_matrix):
    ''' Returns a list of tuples that represents the optimized drift vector.
    :param drift_matrix: A NxN matrix that contains the measured drift between N images.
    '''
    barycenters = [tools.mean_of_list_of_tuples(row) for row in drift_matrix]
    # print 'List of centers: ', centers
    mod_matrix = [[tuple(map(operator.sub, cell, barycenters[i]))
                   for cell in row]
                  for i, row in enumerate(drift_matrix)]
    # print 'Modified drift matrix:'
    # pprint.pprint(mod_matrix)
    rot_matrix = [list(x) for x in zip(*mod_matrix)]
    # print 'Rotated matrix:'
    # pprint.pprint(rot_matrix)
    drift_vector = [tup
                    for tup in [tools.mean_of_list_of_tuples(row)
                                for row in rot_matrix
                               ]
                   ]
    return drift_vector


def shift_vector_from_drift_matrix(drift_matrix):
    ''' Returns a list of tuples that represents the optimized shift vector.
    :param drift_matrix: A NxN matrix that contains the measured drift between N images.
    '''
    drift_vector = drift_vector_from_drift_matrix(drift_matrix)
    shift_vector = [tuple(map(operator.neg, tup)) for tup in drift_vector]
    return shift_vector


def shift_vector_from_drift_vector(drift_vector):
    ''' Returns a list of tuples that represents the optimized shift vector.
    :param drift_vector: A vector of length N that contains the measured drift between N images.
    '''
    shift_vector = [tuple(map(operator.neg, tup)) for tup in drift_vector]
    return shift_vector


def shift_images(img_list, shift_vector):
    ''' Returns a list of new images that are shifted by the given values.
    It is necessary to round down the shift values as ImageJ can only translate by integer values.
    :param img_list: A list of images to be shifted.
    :param shift_vector: A List of x-, y-coordinates to define the shift per image.
    '''
    shift_vector = [(math.floor(shift[0]), math.floor(shift[1])) for shift in shift_vector]
    shifted_list = [img.crop() for img in img_list]
    def make_title(i):
        '''Returns a new image title that contains the performed shift.'''
        old_title = img_list[i].getTitle()
        if shift_vector[i][0] != 0 and shift_vector[i][1] != 0:
            new_title = 'DK#%d&%d#%s' % (shift_vector[i][0], shift_vector[i][1], old_title)
            return new_title
        else:
            return old_title
    for i, img in enumerate(shifted_list):
        img.setTitle(make_title(i))
    for i, img in enumerate(shifted_list):
        IJ.run(img,
               'Translate...',
               'x=%d y=%d interpolation=None' % (shift_vector[i][0], shift_vector[i][1])
              )
    return shifted_list


'''
Testing section:
'''
if __name__ == '__main__':
    print('Testing the function get_drift_vector() for CC and SIFT...')
    imp1 = IJ.openImage("http://imagej.nih.gov/ij/images/TEM_filter_sample.jpg")
    width = imp1.getWidth()
    height = imp1.getHeight()
    IJ.run(imp1, "32-bit", "");
    imp2 = imp1.crop();
    imp3 = imp1.crop();
    offset_x = 15
    offset_y = 15
    IJ.run(imp2,
           "Translate...",
           "x=%d y=%d interpolation=None" % (offset_x, offset_y)
          )
    IJ.run(imp3,
           "Translate...",
           "x=%d y=%d interpolation=None" % (-offset_x, -offset_y)
          )
    for mode in ('CC', 'SIFT'):
        drift_vec = get_drift_vector([imp1, imp2, imp3], mode)
        print('Drift vector: ' + str(drift_vec))
        drift2 = drift_vec[1]
        drift3 = drift_vec[2]
        assert(abs(drift2[0] - offset_x) < 1)
        assert(abs(drift2[1] - offset_y) < 1)
        assert(abs(drift3[0] + offset_x) < 1)
        assert(abs(drift3[1] + offset_y) < 1)

    print('Testing the functions get_drift_matrix(), drift_vector_from_drift_matrix() ' + \
          'and shift_vector_from_drift_matrix() for CC and SIFT...'
         )
    for mode in ('CC', 'SIFT'):
        drift_matrix = get_drift_matrix([imp1, imp2, imp3], mode)
        drift_vec = drift_vector_from_drift_matrix(drift_matrix)
        shift_vec = shift_vector_from_drift_matrix(drift_matrix)
        print('Drift matrix: ' + str(drift_matrix))
        print('Drift vector: ' + str(drift_vec))
        print('Shift vector: ' + str(shift_vec))
        _, drift2, drift3 = drift_vec
        _, shift2, shift3 = shift_vec
        assert(abs(drift2[0] - offset_x) < 1)
        assert(abs(drift2[1] - offset_y) < 1)
        assert(abs(drift3[0] + offset_x) < 1)
        assert(abs(drift3[1] + offset_y) < 1)
        assert(abs(shift2[0] + offset_x) < 1)
        assert(abs(shift2[1] + offset_y) < 1)
        assert(abs(shift3[0] - offset_x) < 1)
        assert(abs(shift3[1] - offset_y) < 1)
    print('Tests completed.')
