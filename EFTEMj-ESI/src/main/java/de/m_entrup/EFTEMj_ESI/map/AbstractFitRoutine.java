
package de.m_entrup.EFTEMj_ESI.map;

public abstract class AbstractFitRoutine {

	// /**
	// * A shortcut to access the instance of {@link DatasetAPI}.
	// */
	// private final DatasetAPI datasetAPI = PluginAPI.getInstance()
	// .getDatasetAPI();
	// /**
	// * The selected {@link ImageStack} as a sorted array of {@link
	// EFTEMImage}s.
	// */
	// private EFTEMImage[] array_EFTEMImages;
	// /**
	// * The index of the first post-edge image.
	// */
	// private int edgeIndex;
	// /**
	// * <code>x + y * width</code>
	// */
	// private int index;
	//
	// public AbstractFitRoutine() {
	// array_EFTEMImages = datasetAPI.getEFTEMImageArray();
	// }

	public abstract short calculateByPixel(int index);

	public abstract float getR(int index);

	public abstract float getA(int index);

}
