'''
file:       Tools.py
author:     Michael Entrup b. Epping (michael.entrup@wwu.de)
version:    20170224
info:       This module contains different usefull functions.
'''

from __future__ import with_statement, division

import os

# pylint: disable-msg=E0401
from EFTEMj_pyLib import HelperDialogs as dialogs

from java.io import File
from ij import IJ, ImagePlus, ImageStack, WindowManager
# pylint: enable-msg=E0401


def perform_func_on_list_of_tuples(func, list_of_tuples):
    ''' Returns a tuple that contains the results of the given function.
    :param func: A function that needs a list as argument.
    :param list_of_tuples: A list that contains tuples, each of the same size.
    '''
    # Actually it's a tuple of tuples,
    # but this name reflects how the structure of the construct is changed.
    tuple_of_lists = tuple(zip(*list_of_tuples))
    return tuple(func(item) for item in tuple_of_lists)


def mean_of_list_of_tuples(list_of_tuples):
    ''' Returns the mean vector of a list of vectors.
    :param list_of_tuples: The tuples represent vectors (e.g. Points in 2D space).
    '''
    def mean(vals):
        '''Returns the mean value of the lists content.'''
        return sum(vals) / len(vals)
    return perform_func_on_list_of_tuples(mean, list_of_tuples)


def center_of_list_of_tuples(list_of_tuples):
    ''' Returns the center vector of a list of vectors $c_i = (min_i + max_i) / 2$.
    :param list_of_tuples: The tuples represent vectors (e.g. Points in 2D space).
    '''
    def mid(vals):
        '''Returns the centre of the given list.'''
        return (max(vals) + min(vals)) / 2
    return perform_func_on_list_of_tuples(mid, list_of_tuples)


def stack_from_list_of_imp(list_of_imps):
    ''' Returns an ImageStack that contains the images of the given list.
    :param list_of_imp: A list of ImagePlus objects.
    '''
    stack = ImageStack(list_of_imps[0].getWidth(), list_of_imps[0].getHeight())
    for img in list_of_imps:
        stack.addSlice(img.getTitle(), img.getProcessor())
    return stack


def stack_to_list_of_imp(imp_stack):
    ''' Returns a list of newly created ImagePlus objects.
    :param stack: The ImagePlus containing astack that is converted to a list.
    '''
    size = imp_stack.getStackSize()
    labels = [imp_stack.getStack().getShortSliceLabel(i)
              for i in range(1, size + 1)]
    ips = [imp_stack.getStack().getProcessor(i) for i in range(1, size + 1)]
    image_list = [ImagePlus(label, ip) for label, ip in zip(labels, ips)]
    return image_list


def get_images(minimum=0, maximum=None, exact=None, no_stack=False):
    ''' Returns a list of ImagePlus objects or None if it failed.
    Passing None as parameter will trigger a dialog to show up to enter the exact number of images.
    :param minimum: The minimum number of images to select (default: 0).
    :param maximum: The maximum number of images to select (default: None).
    :param exact: Set this to get an exact number of images (default: None).
    :param no_stack: Set this boolean to ban stacks from selection (default: False).
    '''
    if not (minimum or maximum or exact or no_stack):
        if WindowManager.getCurrentImage().getStackSize() > 1:
            return stack_to_list_of_imp(WindowManager.getCurrentImage())
    if not (minimum or maximum or exact):
        exact = int(IJ.getNumber("How many images do you want to process?", 3))
    def check_count(count):
        '''Returns True if the count of images is as required.'''
        # print count, exact, minimum, maximum
        if exact:
            if not count == exact:
                return False
        else:
            if minimum:
                if count < minimum:
                    return False
            if maximum:
                if count > maximum:
                    return False
        return True

    # Option 1: The selected image is a stack and has the demanded size.
    if not no_stack:
        if check_count(WindowManager.getCurrentImage().getStackSize()):
            return stack_to_list_of_imp(WindowManager.getCurrentImage())

    # Option 2: The count of open images matches the demanded number.
    image_ids = WindowManager.getIDList()
    if exact:
        if len(image_ids) < exact:
            return None
        if len(image_ids) == exact:
            return [WindowManager.getImage(img_id) for img_id in image_ids]

    # Option 3: The user can select the images from a list.
    image_titles = [WindowManager.getImage(id).getTitle() for id in image_ids]
    img_count = len(image_ids)
    if exact:
        img_count = exact
    elif maximum:
        img_count = maximum
        image_titles.append('None')
    images_selected = dialogs.create_selection_dialog(image_titles,
                                                      range(img_count),
                                                      'Select images for drift correction'
                                                      )
    # dialogs.create_selection_dialog() returns None if canceled
    if not images_selected:
        return None
    # This is only true if 'None has been appended to image_titles and the
    # user selected it.
    if len(image_ids) in images_selected:
        images_selected.remove(len(image_ids))
    if not check_count(len(images_selected)):
        return None
    image_list = [WindowManager.getImage(image_ids[selection])
                  for selection in images_selected]
    return image_list


def copy_scale(list_of_imps_from, list_of_imps_to):
    '''Uses the ImagePlus method copyScale() to copy the calibration.'''
    for imp_from, imp_to in zip(list_of_imps_from, list_of_imps_to):
        imp_to.copyScale(imp_from)


def check_type(file_name, file_extension):
    '''Check if the given file is of the correct file type.
    :param file_name: Name of the file to check.
    :param file_extension: A string (or list/tuple of strings) that represents the file type.
    '''
    if file_extension:
        if isinstance(file_extension, list) or isinstance(file_extension, tuple):
            for file_type_ in file_extension:
                if file_name.endswith(file_type_):
                    return True
                else:
                    continue
        else:
            return bool(file_name.endswith(file_extension))
        return False
    else:
        return True


def check_filter(file_name, filter_string):
    '''Check if the given filename contains a certain string.
    :param file_name: Name of the file to check.
    :param filter_string: A string (or list/tuple of strings) that represents the filter.
    '''
    if filter_string:
        if isinstance(filter_string, list) or isinstance(filter_string, tuple):
            for name_filter_ in filter_string:
                if name_filter_ in file_name:
                    return True
                else:
                    continue
        else:
            return bool(filter_string in file_name)
        return False
    else:
        return True


def batch_open_images(directory, file_type=None, name_filter=None, recursive=False):
    '''Open all files in the given folder.
    :param path: The path from were to open the images. String and java.io.File are allowed.
    :param file_type: Only accept files with the given extension (default: None).
    :param name_filter: Only accept files that contain the given string (default: None).
    :param recursive: Process directories recursively (default: False).
    '''
    if isinstance(directory, File):
        directory = directory.getAbsolutePath()

    path_to_images = []
    directory = os.path.expanduser(directory)
    directory = os.path.expandvars(directory)
    if not recursive:
        for file_name in os.listdir(directory):
            full_path = os.path.join(directory, file_name)
            if os.path.isfile(full_path) \
                    and check_type(file_name, file_type) \
                    and check_filter(file_name, name_filter):
                path_to_images.append(full_path)
    else:
        for directory, _, file_names in os.walk(directory):
            for file_name in file_names:
                full_path = os.path.join(directory, file_name)
                if check_type(file_name, file_type) \
                        and check_filter(file_name, name_filter):
                    path_to_images.append(full_path)
    image_list = []
    for img_path in path_to_images:
        imp = IJ.openImage(img_path)
        if imp:
            image_list.append(imp)
    return image_list


def _test_batch_open():
    path = IJ.getDirectory('Select a directory that contains images...')
    if not path:
        return
    images = batch_open_images(
        path, file_type=None, name_filter=None, recursive=False)
    print('- - Result (recursive=False) - -')
    for image in images:
        print(image)
    images = batch_open_images(
        path, file_type=None, name_filter=None, recursive=True)
    print('- - Result (recursive=True) - -')
    for image in images:
        print(image)
    appendix_list = []
    while (True):
        appendix = IJ.getString('Please enter a file appendix', 'tif')
        if appendix:
            appendix_list.append(appendix)
        else:
            break
    contains = IJ.getString(
        'Please enter a sting the files should contain', 'eV')
    images = batch_open_images(
        path, file_type=appendix_list, name_filter=contains, recursive=True)
    print('- - Result - -')
    for image in images:
        print(image)
    appendix = IJ.getString('Please enter a file appendix', 'tif')
    contains_list = []
    while (True):
        contains = IJ.getString(
            'Please enter a sting the files should contain', 'eV')
        if contains:
            contains_list.append(contains)
        else:
            break
    images = batch_open_images(
        path, file_type=appendix, name_filter=tuple(contains_list), recursive=True)
    print('- - Result - -')
    for image in images:
        print(image)


def _test_module():
    # pylint: disable-msg=C0103
    _test_batch_open()


if __name__ in ('__main__', '__builtin__'):
    _test_module()
