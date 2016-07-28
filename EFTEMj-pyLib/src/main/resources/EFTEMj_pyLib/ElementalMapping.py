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

from java.lang import Float

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
        Es = self.eloss_post
        print len(self.pre1_imp.getProcessor().getPixels())
        lnAs = [calc_lnA(Nb1, Nb2, E1, E2) for Nb1, Nb2 in zip(self.pre1_imp.getProcessor().getPixels(), self.pre2_imp.getProcessor().getPixels())]
        rs = [calc_r(Nb1, Nb2, E1, E2) for Nb1, Nb2 in zip(self.pre1_imp.getProcessor().getPixels(), self.pre2_imp.getProcessor().getPixels())]
        sigs = [calc_sig(sig, lnA, r, Es) for sig, lnA, r in zip(self.post_imp.getProcessor().getPixels(), lnAs, rs)]
        self.map_imp = ImagePlus('Map', FloatProcessor(self.post_imp.getWidth(), self.post_imp.getHeight(), sigs))
        from jarray import array
        ImagePlus('a-Map', FloatProcessor(self.post_imp.getWidth(), self.post_imp.getHeight(), array(lnAs, 'f'))).show()
        ImagePlus('r-Map', FloatProcessor(self.post_imp.getWidth(), self.post_imp.getHeight(), array(rs, 'f'))).show()


    def calc_snr(self):
        self.snr_imp = ImagePlus('SNR', self.post_imp.getProcessor())

    def get_result(self):
        if not self.map_imp:
            self.calc_map()
        if not self.snr_imp:
            self.calc_snr()
        return self.map_imp, self.snr_imp

def calc_lnA(Nb1, Nb2, E1, E2):
    if Nb1 > 0 and Nb2 > 0:
        #return math.log(Nb1 / Nb2) / math.log(E2 / E1) * math.log(Nb1) + math.log(E1)
        return math.log((Nb1 / Nb2)**math.log(Nb1)) / math.log(E2 / E1) + math.log(E1)
    else:
        return Float.NaN

def calc_r(Nb1, Nb2, E1, E2):
    if Nb1 >0 and Nb2 > 0:
        # Three versions that lead to the same results:
        return math.log(Nb1) / math.log(E2 / E1) - math.log(Nb2) / math.log(E2 / E1)
        #return math.log(Nb1) / (math.log(E2) - math.log(E1)) - math.log(Nb2) / (math.log(E2) - math.log(E1))
        #return math.log(Nb1 / Nb2) / math.log(E2 / E1)
    else:
        return Float.NaN

def calc_sig(sig, lnA, r, Es):
    if not Float.isNaN(lnA) and not Float.isNaN(r) and sig > 0:
        return float(sig) - (math.exp(lnA) * math.pow(Es, -r))
    else:
        return Float.NaN