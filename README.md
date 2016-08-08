# EFTEMj
Processing of Energy Filtering TEM images with ImageJ

## Description
EFTEMj is a set of plugins for the open source image processing software ImageJ (and it's distribution Fiji) with a focus on processing data of an Energy Filtering Transmission Electron Microscope (EFTEM). There are modules for different tasks. This modules are described below.

## How to install EFTEMj
The newest version is provided by an [Update Site]. You don't have to download EFTEMj manually as the ImageJ [Updater] performs this task for you. EFTEMj is hosted at the URL
```
http://sites.imagej.net/M-entrup/
```
that you need to [add to your list of Update Sites][addUpdateSite] (the ``M`` must be a capital letter).

## The modules of EFTEMj
### EFTEMj-ESI
This module is used to calculate elemental maps from EFTEM images. The method to fit the power law background signal is the [maximum-likelihood estimation (MLE)][MLE-wiki]. The implementation is based on [Unser1987]. Another part of the module is a drift correction based on [normalised cross-correlation coefficients][NCCC].

The main part of EFTEMj-ESI was started as part of my diploma thesis. I needed the flexibility to calculate elemental maps with any number of pre- and post-edge windows. As the the common implementations use only two pre- and one post-edge windows, this implementation is not limited.

[Unser1987] M. Unser et al., Journal of Microscopy **145** (1987), 245-256

### EFTEMj-EELS
This module contains a plugin to load msa-files (a file format for EEL spectra, that uses simple text files) and display them as a [PlotWindow]. Another plugin grabs the data points from an [PlotWindow] and performs a background fit with a live preview.

### EFTEMj-SR-EELS
This module is part of my PhD thesis. Spatially Resolved EELS (SR-EELS) is a technique to preserve spatial information when recording EEL spectra. Image processing is necessary to remove distortions from the recorded data sets. The plugins of this module organise the calibration data sets, analyse the distortion and perform the correction.

### EFTEMj-lib
This module includes code that is used by more than one other module.

### EFTEMj-pyScripts
This module contains a set of python scripts. On the one hand they extend other EFTEMj modules. On the other hand they demonstrate how to use self written python modules to use in ImageJ scripts. Therefore the module	EFTEMj-pyLib is required to run this scripts.

### EFTEMj-pyLib
This module contains a python module to be used in python scripts for ImageJ. Python modules make it easy to reuse code in python scripts.

## Developing EFTEMj
EFTEMj uses maven to manage dependencies and to control the build process. An introduction to maven is given by the [ImageJ wiki][ImageJMaven]. The source code published in this repository is sufficient to build EFTEMj.

If you want to build EFTEMj from source, three commands are necessary:
```
git clone git@github.com:m-entrup/EFTEMj.git
cd EFTEMj
mvn build
```
This will create a folder ``target`` in each of the modules. There you can find the jar-file of the corresponding module.

To copy the jar files and all necessary dependencies to ImageJ a maven plugin is used ([imagej-maven-plugin]).
```
mvn -Dimagej.app.directory=/path/to/Fiji.app/
```
The parameter ``-D`` will set the system property (in ths case ``imagej.app.directory``) to the given value. Just replace ``/path/to/Fiji.app/`` by the path to your installed ImageJ/Fiji.

The EFTEMj-pyLib jar will not be copied correct, when using the imagej-maven-plugin. You have to move it by hand from the ``target`` directory to ``jars/Lib``, where ImageJ will search for python modules.

[Update Site]: http://imagej.net/Update_Sites
[Updater]: http://imagej.net/Updater
[addUpdateSite]: http://imagej.net/How_to_follow_a_3rd_party_update_site
[MLE-wiki]: https://en.wikipedia.org/wiki/Maximum_likelihood
[NCCc]: https://en.wikipedia.org/wiki/Cross-correlation#Normalized_cross-correlation
[PlotWindow]: http://rsb.info.nih.gov/ij/developer/api/ij/gui/PlotWindow.html
[ImageJMaven]: http://imagej.net/Maven
[imagej-maven-plugin]: https://github.com/imagej/imagej-maven-plugin
