
package de.m_entrup.EFTEMj_ESI.dataset;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;

import de.m_entrup.EFTEMj_ESI.map.MapCalculation;
import de.m_entrup.EFTEMj_ESI.map.PowerLawFitCalculation;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.FloatProcessor;

/**
 * This class will handle all access to the used data. For example the
 * ImagePlus, the energy loss, or the results of calculations. Some objects can
 * be passed to the requester, others will only be accessible by the use of
 * methods this class will provide.
 */
public class DatasetAPI {

	/**
	 * All data that is needed to calculate the drift between the images.
	 */
	private DatasetDriftInput datasetDriftInput;
	/**
	 * The result of the crosscorrelation. Only this part of the drift correction
	 * has parallel threads that save their results using the {@link DatasetAPI}.
	 */
	private DatasetDriftResult datasetDriftResult;
	/**
	 * {@link DatasetMapInput} gives access to all data that you need to start the
	 * elemental-map calculation.
	 */
	private DatasetMapInput datasetMapInput;
	/**
	 * {@link DatasetMapResult} stores all results from the elemental-map
	 * calculation.
	 */
	private DatasetMapResult datasetMapResult;
	/**
	 * If the {@link ImagePlus} is a stack, this {@link ImageStack} is saved at
	 * {@link DatasetStack}.
	 */
	private DatasetStack datasetStack;
	/**
	 * The energy loss that is used to split the images into pre-edge and
	 * post-edge images.
	 */
	private float edgeELoss;
	/**
	 * The {@link ImagePlus} that is selected for further calculations.
	 */
	private final ImagePlus imagePlus;

	/**
	 * Creates a new {@link DatasetAPI} object that will handle the access to all
	 * data of the given {@link ImagePlus} object. Additionally it will be used to
	 * store results of calculations and give access to the stored values.
	 *
	 * @param imagePlus The {@link ImagePlus} that is used for calculations.
	 */
	public DatasetAPI(final ImagePlus imagePlus) {
		super();
		this.imagePlus = imagePlus;
		if (imagePlus.getStackSize() > 1) {
			datasetStack = new DatasetStack(imagePlus.getImageStack());
		}
	}

	/**
	 * This method creates an instance of {@link DatasetDriftInput} and
	 * {@link DatasetDriftResult}. This is only done if the distance between the
	 * ROI and the image bounds is larger than the parameter delta.
	 *
	 * @param delta The largest investigated drift.
	 * @param referenceIndex The index (starting @ 1) of the image (slice) that is
	 *          used as reference for the drift correction.
	 * @return <code>true</code> if delta is ok and the drift correction can be
	 *         done, <code>false</code> if delta is to large.
	 */
	public boolean createDatasetDriftInput(final int delta,
		final int referenceIndex)
	{
		// The Rectangle is cloned to prevent changes by the user. It may be
		// possible to change the ROI of the original image during the
		// calculation.
		final Rectangle roi = (Rectangle) imagePlus.getRoi().getBounds().clone();
		if (roi.getX() - delta < 0 | roi.getY() - delta < 0) {
			return false;
		}
		if (roi.getX() + roi.getWidth() + delta > this.getWidth() | roi.getY() + roi
			.getHeight() + delta > this.getHeight())
		{
			return false;
		}
		final FloatProcessor[] array_croppedImages = new FloatProcessor[this
			.getStackSize()];
		for (int i = 0; i < this.getStackSize(); i++) {
			if (i != referenceIndex - 1) {
				final FloatProcessor fp = (FloatProcessor) datasetStack.imageStack
					.getProcessor(i + 1);
				fp.setRoi(roi);
				array_croppedImages[i] = (FloatProcessor) fp.crop();
			}
		}
		// Another clone of the rectangle is used to crop the images that will
		// be corrected. The size of the new rectangle is increased by delta in
		// all directions.
		final Rectangle roiMod = (Rectangle) roi.clone();
		roiMod.setBounds(roiMod.x - delta, roiMod.y - delta, roiMod.width + 2 *
			delta, roiMod.height + 2 * delta);
		final FloatProcessor fp = (FloatProcessor) datasetStack.imageStack
			.getProcessor(referenceIndex);
		fp.setRoi(roiMod);
		array_croppedImages[referenceIndex - 1] = (FloatProcessor) fp.crop();
		datasetDriftInput = new DatasetDriftInput(array_croppedImages, roi,
			referenceIndex, delta);
		datasetDriftResult = new DatasetDriftResult();
		return true;
	}

	/**
	 * Creates an instance of {@link DatasetMapInput} that contains an sorted
	 * array of {@link EFTEMImage}s. Additionally edgeELoss, epsilon and rLimit
	 * are passed to {@link DatasetMapInput}.
	 *
	 * @param edgeELoss The energy loss where the element signal starts.
	 * @param epsilon The exit condition for the MLE calculation.
	 * @param rLimit A high-pass filter for the calculated parameters
	 *          <code>r</code> .
	 * @throws Exception
	 */
	public void createDatasetMapInput(final float edgeELoss, final float epsilon,
		final float rLimit) throws Exception
	{
		final EFTEMImage[] array_EFTEMImages = new EFTEMImage[getStackSize()];
		for (int i = 0; i < getStackSize(); i++) {
			array_EFTEMImages[i] = new EFTEMImage(datasetStack.eLossArray[i],
				datasetStack.imageStack.getShortSliceLabel(i + 1),
				(float[]) datasetStack.imageStack.getPixels(i + 1), getWidth());
		}
		Arrays.sort(array_EFTEMImages);
		try {
			datasetMapInput = new DatasetMapInput(array_EFTEMImages, edgeELoss,
				epsilon, rLimit);
			datasetMapResult = new DatasetMapResult();
		}
		catch (final Exception e) {
			datasetMapInput = null;
			datasetMapResult = null;
			throw e;
		}
	}

	/**
	 * This method deletes the instances of {@link DatasetDriftInput} and
	 * {@link DatasetDriftResult}.
	 */
	public void deleteDriftDataset() {
		datasetDriftInput = null;
		datasetDriftResult = null;
	}

	/**
	 * This method deletes the instances of {@link DatasetMapInput} and
	 * {@link DatasetMapResult}.
	 */
	public void deleteMapDataset() {
		datasetMapInput = null;
		datasetMapResult = null;
	}

	/**
	 * Tries to find an edge in the given energy loss interval.
	 *
	 * @param eLossLow The lower limit of the interval.
	 * @param eLossHigh The upper limit of the interval.
	 * @return <code>true</code> if an edge was found.
	 */
	private boolean findEdge(final float eLossLow, final float eLossHigh) {
		final LinkedHashMap<Integer, String> edges = IonisationEdges.getInstance()
			.getEdges();
		final int[] possibleEdges = new int[edges.size()];
		int edgeCount = 0;
		for (int i = (int) Math.ceil(eLossLow); i < eLossHigh; i++) {
			if (edges.get(i) != null) {
				possibleEdges[edgeCount] = i;
				edgeCount++;
			}
		}
		if (edgeCount == 0) {
			return false;
		}
		if (edgeCount == 1) {
			edgeELoss = possibleEdges[0];
			return true;
		}
		final float mean = (eLossHigh + eLossLow) / 2;
		int selected = 0;
		float diff = Math.abs(mean - possibleEdges[0]);
		for (int i = 1; i < edgeCount; i++) {
			if (Math.abs(mean - possibleEdges[i]) < diff) {
				diff = Math.abs(mean - possibleEdges[i]);
				selected = i;
			}
		}
		edgeELoss = possibleEdges[selected];
		return true;
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return An array of all calculated power law parameters <code>a</code>.
	 */
	public float[] getAMap() {
		return datasetMapResult.aMap;
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return An array of {@link EFTEMImage}s which show the calculated
	 *         background.
	 */
	public EFTEMImage[] getBackgroundImages() {
		return datasetMapResult.array_BackgroundImage;
	}

	/**
	 * {@link DatasetMapInput}
	 *
	 * @param imageIndex The position of the image at the array of all background
	 *          images (starting at 0).
	 * @return The pixel values of the selected image.
	 */
	public float[] getBackgroundPixels(final int imageIndex) {
		return datasetMapResult.array_BackgroundImage[imageIndex].pixels;
	}

	/**
	 * {@link DatasetAPI}
	 *
	 * @return The Calibration of the selected {@link ImagePlus} object.
	 */
	public Calibration getCalibration() {
		return imagePlus.getCalibration();
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return An array of all Chi�-values.
	 */
	public float[] getChi2() {
		return datasetMapResult.chi2Map;
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return An array of all calculated coefficients of determination.
	 */
	public float[] getCoeffOFDet() {
		return datasetMapResult.coefficientOfDeterminationMap;
	}

	/**
	 * {@link DatasetDriftResult}
	 *
	 * @return A 2D-array that contains the crosscorrelation coefficients. The
	 *         array at the position of the template image is empty.
	 */
	public float[][] getCorrelationCoefficients() {
		return datasetDriftResult.array_correlationCoefficients;
	}

	/**
	 * {@link DatasetDriftResult}
	 *
	 * @return An array of {@link FloatProcessor}s, each of them containing a map
	 *         of crosscorrelation coefficients. The {@link FloatProcessor} at the
	 *         position of the template image is empty.
	 */
	public FloatProcessor[] getCorrelationCoefficientsAsFP() {
		if (datasetDriftResult.array_correlationCoefficientsAsFP == null) {
			datasetDriftResult.createFloatProcessorFromArray();
		}
		return datasetDriftResult.array_correlationCoefficientsAsFP;
	}

	/**
	 * {@link DatasetDriftInput}
	 *
	 * @param index The position of the {@link FloatProcessor} at the array
	 *          (starting at 0).
	 * @return A cropped image that correspondents to the ROI set at the GUI. The
	 *         {@link FloatProcessor} at the position of the template index is
	 *         larger than the others.
	 */
	public FloatProcessor getCroppedImage(final int index) {
		return datasetDriftInput.array_CroppedImages[index];
	}

	/**
	 * {@link DatasetDriftInput}
	 *
	 * @return The cropped reference image. This is the same as<br />
	 *         <code>getCroppedImage(referenceImageIndex)</code>.
	 */
	public FloatProcessor getCroppedReferenceImage() {
		return datasetDriftInput.array_CroppedImages[datasetDriftInput.referenceImageIndex];
	}

	/**
	 * {@link DatasetDriftInput}
	 *
	 * @return The largest considered drift.
	 */
	public int getDelta() {
		return datasetDriftInput.delta;
	}

	/**
	 * {@link DatasetMapInput}
	 *
	 * @return The index of the first image, at a sorted array of
	 *         {@link EFTEMImage}s, that has an energy loss equal to or larger
	 *         than the edge energy loss (the index starts at 0).
	 */
	public int getEdgeIndex() {
		return datasetMapInput.edgeIndex;
	}

	/**
	 * {@link DatasetMapInput}
	 *
	 * @param imageIndex The position (starting at 0) of the {@link EFTEMImage} at
	 *          the sorted array of {@link EFTEMImage}s.
	 * @return The {@link EFTEMImage} at the selected array position.
	 */
	public EFTEMImage getEFTEMImage(final int imageIndex) {
		return datasetMapInput.array_InputImages[imageIndex];
	}

	/**
	 * {@link DatasetMapInput}
	 *
	 * @return All images of the selected stack as a sorted array of
	 *         {@link EFTEMImage}s.
	 */
	public EFTEMImage[] getEFTEMImageArray() {
		return datasetMapInput.array_InputImages;
	}

	/**
	 * {@link DatasetStack}
	 *
	 * @return An unsorted array containing the energy losses of all images at the
	 *         {@link ImageStack}. It returns <code>null</code> if only a single
	 *         image is selected.
	 */
	public float[] getELossArray() {
		if (datasetStack == null) return null;
		return datasetStack.eLossArray;
	}

	/**
	 * {@link DatasetStack}
	 *
	 * @param n Index of the eLossArray, starting at 0.
	 * @return The energy loss as a String with 0, 1, or 2 decimal places using
	 *         the US locale to format the String.
	 */
	public String getELossAsString(final int n) {
		if (datasetStack == null) {
			return null;
		}
		else {
			final float value = datasetStack.eLossArray[n];
			String valueFormat;
			if (value % 1 == 0) {
				valueFormat = "%.0f";
			}
			else if (10 * value % 1 == 0) {
				valueFormat = "%.1f";
			}
			else {
				valueFormat = "%.2f";
			}
			final String eLossStr = String.format(Locale.ENGLISH, valueFormat, value);
			return eLossStr;
		}
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return All error values set by the {@link PowerLawFitCalculation}.
	 */
	public float[] getErrorMap() {
		return datasetMapResult.errorMap;
	}

	/**
	 * {@link DatasetAPI}
	 *
	 * @return The height of the selected {@link ImagePlus} object.
	 */
	public int getHeight() {
		return imagePlus.getHeight();
	}

	/**
	 * {@link DatasetAPI}
	 *
	 * @return The {@link ImagePlus} object that is selected by the plugin.
	 */
	public ImagePlus getImagePlus() {
		return imagePlus;
	}

	/**
	 * {@link DatasetAPI}
	 *
	 * @return The short title of the selected {@link ImagePlus}.
	 */
	public String getImagePlusShortTitle() {
		return imagePlus.getShortTitle();
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return The elemental-maps calculated by {@link MapCalculation}.
	 */
	public EFTEMImage[] getMap() {
		return datasetMapResult.array_Map;
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param index The position of the {@link EFTEMImage} at the array of
	 *          elemental-maps (starting at 0).
	 * @return The elemental-map at the selected array position.
	 */
	public EFTEMImage getMap(final int index) {
		return datasetMapResult.array_Map[index];
	}

	/**
	 * {@link DatasetDriftInput}
	 *
	 * @param index The position of the value at the array of cropped images
	 *          (starting at 0).
	 * @return The mean value of the image at the selected position.
	 */
	public float getMean(final int index) {
		return datasetDriftInput.mean[index];
	}

	/**
	 * {@link DatasetStack} This method takes the two highest energy loss values
	 * of the energy loss array and tries to find an edge at the given interval.
	 * If no edge is listed for the given interval the median of the selected
	 * energy loss values is used.
	 *
	 * @return A prediction of the edge energy loss.
	 */
	public float getPredictedEdgeELoss() {
		final float[] eLossArray = getSortedELossArray();
		final float eLossHigh = eLossArray[eLossArray.length - 1];
		final float eLossLow = eLossArray[eLossArray.length - 2];
		// TODO enhance the code to detect edges if there are 2 or more
		// post-edge images.
		if (findEdge(eLossLow, eLossHigh) == false) {
			edgeELoss = (eLossHigh + eLossLow) / 2;
		}
		return edgeELoss;
	}

	/**
	 * @param edgeELoss The energy loss of the ionization edge.
	 * @return If the given energy loss is listed at the database the element and
	 *         the name of the edge are written to this string.
	 */
	public String getPredictedEdgeLabel(final int edgeELoss) {
		String label;
		final LinkedHashMap<Integer, String> edges = IonisationEdges.getInstance()
			.getEdges();
		label = edges.get(edgeELoss);
		if (label == null) {
			label = PluginMessages.getString("Label.NoEdgeFound");
		}
		return label;
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return An array of {@link EFTEMImage}s which show the calculated relative
	 *         background.
	 */
	public EFTEMImage[] getRelBackgroundImages() {
		return datasetMapResult.array_RelativeBackgroundImage;
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return n array of all calculated power law parameters <code>r</code>.
	 */
	public float[] getRMap() {
		return datasetMapResult.rMap;
	}

	/**
	 * {@link DatasetDriftInput}
	 *
	 * @return The ROI used for drift detection.
	 */
	public Rectangle getRoi() {
		return datasetDriftInput.roi;
	}

	/**
	 * {@link DatasetStack}
	 *
	 * @param n The index of the image at the input stack (starting at 0, the
	 *          stack is not sorted by the energy loss).
	 * @return The short title of the selected slice/image.
	 */
	public String getShortSliceLabel(final int n) {
		return datasetStack.imageStack.getShortSliceLabel(n + 1);
	}

	/**
	 * {@link DatasetMapInput}
	 *
	 * @param index The position of the value at the array of cropped images
	 *          (starting at 0).
	 * @return The variance of the selected image.
	 */
	public double getSigma(final int index) {
		return datasetDriftInput.sigma[index];
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return Sigma^2 is part of the SNR. It is calculated for each elemental-map
	 *         and each pixel of it.
	 */
	public EFTEMImage[] getSigma2() {
		return datasetMapResult.array_Sigma2;
	}

	/**
	 * {@link DatasetStack}
	 *
	 * @param n The index of the image at the input stack (starting at 0, the
	 *          stack is not sorted by the energy loss).
	 * @return The complete title of the selected slice/image.
	 */
	public String getSliceLabel(final int n) {
		return datasetStack.imageStack.getSliceLabel(n + 1);
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @return The SNR of each elemental-map.
	 */
	public EFTEMImage[] getSNR() {
		return datasetMapResult.array_SNR;
	}

	/**
	 * {@link DatasetStack}
	 *
	 * @return An sorted array containing the energy losses of all images at the
	 *         {@link ImageStack}. It returns <code>null</code> if only a single
	 *         image is selected.
	 */
	public float[] getSortedELossArray() {
		if (datasetStack == null) return null;
		final float[] sortedELossArray = datasetStack.eLossArray.clone();
		Arrays.sort(sortedELossArray);
		return sortedELossArray;
	}

	/**
	 * {@link DatasetAPI}
	 *
	 * @return The size of the {@link ImageStack}. Returns 1 if an single image is
	 *         selected.
	 */
	public int getStackSize() {
		return imagePlus.getStackSize();
	}

	/**
	 * {@link DatasetDriftInput}
	 *
	 * @return The index of the template image (starting at 0).
	 */
	public int getTemplateIndex() {
		return datasetDriftInput.referenceImageIndex;
	}

	/**
	 * {@link DatasetAPI}
	 *
	 * @return The width of the selected {@link ImagePlus} object.
	 */
	public int getWidth() {
		return imagePlus.getWidth();
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param index <code>y * width + x</code>
	 * @param array_A The parameters <code>a</code> of an image row.
	 */
	public synchronized void saveA(final int index, final float[] array_A) {
		for (int i = 0; i < array_A.length; i++) {
			datasetMapResult.aMap[index + i] = array_A[i];
		}
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param background All pixel values of a background image.
	 * @param imageIndex The position of the image at the array of all background
	 *          images (starting at 0).
	 */
	public synchronized void saveBackground(final float[] background,
		final int imageIndex)
	{
		final EFTEMImage eftemImage = datasetMapInput.array_InputImages[imageIndex];
		final String label = PluginMessages.getString("Label.BgImage") + "[" +
			eftemImage.getELoss() + "eV]";
		datasetMapResult.array_BackgroundImage[imageIndex] = new EFTEMImage(
			eftemImage.getELoss(), label, background, eftemImage.width);
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param index <code>y * width + x</code>
	 * @param array_Chi2 The Chi�-values of an image row.
	 */
	public synchronized void saveChi2(final int index, final float[] array_Chi2) {
		for (int i = 0; i < array_Chi2.length; i++) {
			datasetMapResult.chi2Map[index + i] = array_Chi2[i];
		}
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param index <code>y * width + x</code>
	 * @param array_COD The coefficients of determination of an image row.
	 */
	public synchronized void saveCOD(final int index, final float[] array_COD) {
		for (int i = 0; i < array_COD.length; i++) {
			datasetMapResult.coefficientOfDeterminationMap[index + i] = array_COD[i];
		}
	}

	/**
	 * {@link DatasetDriftResult}
	 *
	 * @param values The crosscorrelation coefficients of an image row.
	 * @param imageIndex The position of the image at the array of all coefficient
	 *          images (starting at 0).
	 * @param index <code>y * width + x</code>
	 */
	public synchronized void saveCross(final float[] values, final int imageIndex,
		final int index)
	{
		for (int i = 0; i < values.length; i++) {
			datasetDriftResult.array_correlationCoefficients[imageIndex][index + i] =
				values[i];
		}
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param index <code>y * width + x</code>
	 * @param array_Error The error values of an image row.
	 */
	public synchronized void saveError(final int index,
		final short[] array_Error)
	{
		for (int i = 0; i < array_Error.length; i++) {
			datasetMapResult.errorMap[index + i] = array_Error[i];
		}
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param map All pixel values of a elemental-map.
	 * @param imageIndex he position of the image at the array of all
	 *          elemental-maps (starting at 0).
	 */
	public synchronized void saveMap(final float[] map, final int imageIndex) {
		final EFTEMImage eftemImage = datasetMapInput.array_InputImages[imageIndex];
		final String label = PluginMessages.getString("Label.MapImage") + "[" +
			eftemImage.getELoss() + "eV]";
		datasetMapResult.array_Map[imageIndex - datasetMapInput.edgeIndex] =
			new EFTEMImage(eftemImage.getELoss(), label, map, eftemImage.width);
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param index <code>y * width + x</code>
	 * @param array_R The parameters <code>r</code> of an image row.
	 */
	public synchronized void saveR(final int index, final float[] array_R) {
		for (int i = 0; i < array_R.length; i++) {
			datasetMapResult.rMap[index + i] = array_R[i];
		}
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param relBackground All pixel values of a relative background image.
	 * @param imageIndex The position of the image at the array of all relative
	 *          background images (starting at 0).
	 */
	public synchronized void saveRelBackground(final float[] relBackground,
		final int imageIndex)
	{
		final EFTEMImage eftemImage = datasetMapInput.array_InputImages[imageIndex];
		final String label = PluginMessages.getString("Label.RelBgImage") + "[" +
			eftemImage.getELoss() + "eV]";
		datasetMapResult.array_RelativeBackgroundImage[imageIndex] = new EFTEMImage(
			eftemImage.getELoss(), label, relBackground, eftemImage.width);
	}

	/**
	 * {@link DatasetMapResult}
	 *
	 * @param imageIndex The position of the image at the array of all sigma^2
	 *          images (starting at 0).
	 * @param index <code>y * width + x</code>
	 * @param sigma2 The sigma^2 values of an image row.
	 */
	public synchronized void saveSigma2(final int imageIndex, final int index,
		final float[] sigma2)
	{
		final EFTEMImage eftemImage = datasetMapInput.array_InputImages[imageIndex];
		final String label = PluginMessages.getString("Label.Sigma2Image") + "[" +
			eftemImage.getELoss() + "eV]";
		if (datasetMapResult.array_Sigma2[imageIndex -
			datasetMapInput.edgeIndex] == null)
		{
			datasetMapResult.array_Sigma2[imageIndex - datasetMapInput.edgeIndex] =
				new EFTEMImage(eftemImage.getELoss(), label, new float[getWidth() *
					getHeight()], eftemImage.width);
		}
		final float[] pixels = datasetMapResult.array_Sigma2[imageIndex -
			datasetMapInput.edgeIndex].getPixels();
		for (int i = 0; i < sigma2.length; i++) {
			pixels[index + i] = sigma2[i];
		}
	}

	/**
	 * @link DatasetMapResult}
	 * @param imageIndex The position of the image at the array of all SNR images
	 *          (starting at 0).
	 * @param index <code>y * width + x</code>
	 * @param snr The SNR values of an image row.
	 */
	public synchronized void saveSNR(final int imageIndex, final int index,
		final float[] snr)
	{
		final EFTEMImage eftemImage = datasetMapInput.array_InputImages[imageIndex];
		final String label = PluginMessages.getString("Label.SnrImage") + "[" +
			eftemImage.getELoss() + "eV]";
		if (datasetMapResult.array_SNR[imageIndex -
			datasetMapInput.edgeIndex] == null)
		{
			datasetMapResult.array_SNR[imageIndex - datasetMapInput.edgeIndex] =
				new EFTEMImage(eftemImage.getELoss(), label, new float[getWidth() *
					getHeight()], eftemImage.width);
		}
		final float[] pixels = datasetMapResult.array_SNR[imageIndex -
			datasetMapInput.edgeIndex].getPixels();
		for (int i = 0; i < snr.length; i++) {
			pixels[index + i] = snr[i];
		}
	}

	/**
	 * {@link DatasetStack}
	 *
	 * @param imageIndex The index of the image at the input stack (starting at 0,
	 *          the stack is not sorted by the energy loss).
	 * @param eLoss The new energy loss of the image.
	 */
	public void setELoss(final int imageIndex, final float eLoss) {
		datasetStack.eLossArray[imageIndex] = eLoss;
	}

	/**
	 * {@link DatasetStack}<br />
	 * Set a new label for a specific slice. This is not a short label. It can
	 * contain additional information about the image.
	 *
	 * @param n The index of the slice starting at 0.
	 * @param newLabel The new Label.
	 */
	public void setSliceLabel(final int n, final String newLabel) {
		datasetStack.imageStack.setSliceLabel(newLabel, n + 1);
		imagePlus.changes = true;
	}
}
