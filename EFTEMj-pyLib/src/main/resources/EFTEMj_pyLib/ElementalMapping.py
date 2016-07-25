# coding=utf-8
'''
file:       ElementalMapping.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20160725
info:       This module contains different classes to calculate elemental mpas.
'''

from __future__ import with_statement, division

from EFTEMj_pyLib import Tools as tools

import math, re
from ij import IJ, ImagePlus
from ij.process import FloatProcessor

class ThreeWindow:

    def __init__(self, pre1, pre2, post):
        self.pre1_imp = pre1
        self.pre2_imp = pre2
        self.post_imp = post
        eloss_pattern = re.compile('(\d+(?:[\.,]\d+)?)eV')
        self.eloss_pre1 = float(eloss_pattern.search(pre1.getTitle()).group(1))
        self.eloss_pre2 = float(eloss_pattern.search(pre2.getTitle()).group(1))
        self.eloss_post = float(eloss_pattern.search(post.getTitle()).group(1))
        print self.eloss_pre1, self.eloss_pre2, self.eloss_post
        self.map_imp = False
        self.snr_imp = False

    def calc_map(self):
        E1 = self.eloss_pre1
        E2 = self.eloss_pre2
        self.map_imp = ImagePlus('Map', FloatProcessor(self.post_imp.getWidth(), self.post_imp.getHeight()))
        for y in range(self.post_imp.getHeight()):
            for x in range(self.post_imp.getWidth()):
                Nb1 = self.pre1_imp.getProcessor().getf(x, y)
                Nb2 = self.pre2_imp.getProcessor().getf(x, y)
                sig = self.post_imp.getProcessor().getf(x, y)
                if Nb1 > 0 and Nb2 > 0 and sig > 0:
	                lnA = 0.5 * math.log(Nb1) + math.log(Nb2) + math.log(E1) + math.log(E2)
	                r = math.log(Nb1 / Nb2) / math.log(E2 / E1)
	                self.map_imp.getProcessor().setf(x, y, sig - math.exp(lnA) * math.pow(self.eloss_post, -1 * r))

    def calc_snr(self):
        self.snr_imp = ImagePlus('SNR', self.post_imp.getProcessor())

    def get_result(self):
        if not self.map_imp:
            self.calc_map()
        if not self.snr_imp:
            self.calc_snr()
        return self.map_imp, self.snr_imp