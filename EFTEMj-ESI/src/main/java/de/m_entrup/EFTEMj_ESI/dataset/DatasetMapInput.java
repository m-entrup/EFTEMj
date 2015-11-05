
package de.m_entrup.EFTEMj_ESI.dataset;

import de.m_entrup.EFTEMj_ESI.map.PowerLawFitCalculation;
import de.m_entrup.EFTEMj_ESI.resources.PluginMessages;

/**
 * The {@link DatasetMapInput} class collects all data that is used to start the
 * elemental-map calculation.
 */
public class DatasetMapInput {

	/**
	 * The images that are used for the elemental-map calculation. The array has
	 * to be sorted by energy loss.
	 */
	protected EFTEMImage[] array_InputImages;
	/**
	 * The energy loss that separates pre-edge and post-edge images. An image with
	 * the same energy loss value as edgeELoss will be handled as a post-edge
	 * image.
	 */
	protected float edgeELoss;
	/**
	 * The index of the first post-edge image.
	 */
	protected int edgeIndex;

	/**
	 * The constructor creates an object of {@link DatasetMapInput} that is
	 * necessary to start the MLE calculation.
	 *
	 * @param inputImages A sorted array of {@link EFTEMImage}s.
	 * @param edgeELoss The energy loss where the elemental signal starts.
	 * @param epsilon The exit condition for the MLE calculation.
	 * @throws Exception
	 */
	public DatasetMapInput(final EFTEMImage[] inputImages, final float edgeELoss,
		final float epsilon) throws Exception
	{
		this.array_InputImages = inputImages;
		this.edgeELoss = edgeELoss;
		PowerLawFitCalculation.setEpsilon(epsilon);
		int index = 0;
		while (index < inputImages.length) {
			if (inputImages[index].eLoss >= edgeELoss) {
				break;
			}
			index++;
		}
		edgeIndex = index;
		if (edgeIndex < 2) {
			throw new Exception(PluginMessages.getString("Error.PreEdgeImages"));
		}
		if (inputImages.length - edgeIndex == 0) {
			throw new Exception(PluginMessages.getString("Error.PostEdgeImages"));
		}
	}
}
