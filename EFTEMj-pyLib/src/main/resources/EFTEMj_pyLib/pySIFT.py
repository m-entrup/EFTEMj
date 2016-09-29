'''
file:       pySIFT.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20160929
info:       This module calculates the drift using SIFT.
'''

from __future__ import with_statement, division

from ij import IJ, ImagePlus

from java.util import ArrayList, Vector
from java.lang import Float

from mpicbg.imagefeatures import FloatArray2DSIFT
from mpicbg.ij import SIFT
from mpicbg.models import TranslationModel2D

class Param:
    '''A dataset that holds parameters used by the SIFT algorithm.
    You can modify an instance of this object to customize the SIFT detection.
    '''
    # pylint: disable=too-few-public-methods
    sift = FloatArray2DSIFT.Param()
    # Closest/next closest neighbour distance ratio
    rod = 0.92
    #Maximal allowed alignment error in px
    maxEpsilon = 25.0
    # Inlier/candidates ratio
    minInlierRatio = 0.05
    # Implemeted transformation models for choice
    modelStrings = ['Translation,' 'Rigid', 'Similarity', 'Affine']
    modelIndex = 1
    interpolate = True
    showInfo = False

class pySIFT:
    '''An implementation of a drift correction using the SIFT algorithm.
    It is designed to measure the drift of all images with each other.
    '''

    def __init__(self, img_list, params=None):
        '''Create an instance of pySIFT.
        :param img_list: All SIFT calculations are done with the given list
                         of ImagePlus objects (Classes that extend ImagePlus are allowed).
        :param params: Pass a modified instance of Param
                       to customize the SIFT detection (default: None).
        '''
        self.images = []
        def check(img):
            '''Returns True if img is a an ImagePlus or a class that extends it.'''
            # Check if the given object extends ImagePlus:
            return isinstance(img, ImagePlus)
        for img in img_list:
            if check(img):
                self.images.append(img)
        if params and isinstance(params, Param):
            self.param = params
        else:
            self.param = Param()
        self.drift_vector = [None] * len(self.images)
        self.drift_matrix = [[]] * len(self.images)
        for i, _ in enumerate(self.drift_matrix):
            self.drift_matrix[i] = [None] * len(self.images)
        self._extract_features_()

    def _extract_features_(self):
        '''This method is used by the constructor to identify the features of each image.
        '''
        sift = FloatArray2DSIFT(self.param.sift)
        ijSIFT = SIFT(sift)
        self.all_features = [ArrayList() for _ in self.images]
        for img, features in zip(self.images, self.all_features):
            ijSIFT.extractFeatures(img.getProcessor(), features)

    def get_drift(self, index1, index2):
        '''Returns th drift between the images at the given indices.
        :param index1: The index of the first image (0-based).
        :param index2: The index of the second image (0-based).
        '''
        if not self.drift_matrix[index1][index2]:
            if index1 == index2:
                self.drift_matrix[index1][index2] = (0, 0)
            model = TranslationModel2D()
            candidates = FloatArray2DSIFT.createMatches(self.all_features[index1],
                                                        self.all_features[index2],
                                                        1.5,
                                                        None,
                                                        Float.MAX_VALUE,
                                                        self.param.rod
                                                       )
            # print '%i potentially corresponding features identified' % len(candidates)
            inliers = Vector()
            model.filterRansac(candidates,
                               inliers,
                               1000,
                               self.param.maxEpsilon,
                               self.param.minInlierRatio
                              )
            self.drift_matrix[index1][index2] = (model.createAffine().translateX,
                                                 model.createAffine().translateY)
        return self.drift_matrix[index1][index2]

    def get_drift_matrix(self):
        '''Returns a NxN matrix that represents the drift of all images with each other.
        '''
        full_progress = len(self.images)**2
        for i in range(len(self.images)):
            for j in range(len(self.images)):
                IJ.showProgress((i * len(self.images) + j) / full_progress)
                self.drift_matrix[i][j] = self.get_drift(i, j)
        IJ.showProgress(1.0)
        return self.drift_matrix

    def get_drift_vector(self):
        '''Returns a vector of length N that represents the drift of all images with the first one.
        '''
        full_progress = len(self.images) - 1
        self.drift_vector[0] = (0., 0.)
        for i in range(1, len(self.images)):
            IJ.showProgress(i / full_progress)
            self.drift_vector[i] = self.get_drift(i - 1, i)
            self.drift_vector[i] = tuple([a + b for a,b in zip(self.drift_vector[i - 1], self.drift_vector[i])])
        IJ.showProgress(1.0)
        return self.drift_vector

'''
Testing section:
'''
if __name__ == '__main__':
    imp1 = IJ.openImage("http://imagej.nih.gov/ij/images/TEM_filter_sample.jpg");
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
    sift = pySIFT([imp1, imp2, imp3])
    drift_vec = sift.get_drift_vector()
    drift2 = drift_vec[1]
    drift3 = drift_vec[2]
    assert(abs(drift2[0] - offset_x) < 1)
    assert(abs(drift2[1] - offset_y) < 1)
    assert(abs(drift3[0] + offset_x) < 1)
    assert(abs(drift3[1] + offset_y) < 1)
    print('Test completed.')