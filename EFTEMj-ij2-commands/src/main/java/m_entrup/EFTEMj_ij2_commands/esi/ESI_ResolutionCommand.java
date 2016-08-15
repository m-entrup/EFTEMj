package m_entrup.EFTEMj_ij2_commands.esi;

import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;

/**
 * A plugin (IJ2-Command) to calculate different properties of an TEM operating
 * in ESI mode.
 * 
 * @author Michael Entrup
 *
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>EFTEMj>ESI>Calculate resolution...")
public class ESI_ResolutionCommand implements Command {

	private static final String MICROSCOPE_LABEL = "-- Microscope properties --";
	private static final String PLOT_SETTINGS = "-- Plot settings --";

	private static final double ELECTRON_CHARGE = 1.6021766208e-19;
	private static final double H_BAR = 1.054571800e-34;
	private static final double LIGHT_SPEED = 299792458;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String labelMic = MICROSCOPE_LABEL;

	@Parameter(label = "Electron beam energy (keV)", callback = "primaryEnergyChanged")
	private double primaryEnergy = 200;

	private double wavelength = calcWavelength();

	private double speed = calcSpeed();

	@Parameter(label = "Wavelength (pm)", visibility = ItemVisibility.MESSAGE)
	private double lambda = round(wavelength, 2);

	@Parameter(label = "Energy loss (eV)", callback = "updateDelocalisation")
	private double energyLoss = 0;

	@Parameter(label = "Slit width (eV)", callback = "updateChromaticAberration")
	private double slitWidth = 20;

	@Parameter(label = "Collection half angle (mrad)", callback = "collectionHalfAngleChanged")
	private double collectionHalfAngle = 10;

	@Parameter(label = "Focal length (mm)", callback = "focalLengthChanged")
	private double focalLength = 1.72;

	@Parameter(label = "Aperture diameter (µm)", callback = "updateCollectionHalfAngle")
	private double apertureDiameter = 40;

	@Parameter(label = "Chromatic aberation (mm)", callback = "updateChromaticAberration")
	private double chromaticAberration = 1.2;

	@Parameter(label = "Spherical aberation (mm)", callback = "updateSphericalAberration")
	private double sphericalAberation = 1.2;

	private double chromatic = calcChromaticAberration();

	@Parameter(label = "Error disc by chromatic aberration (nm)", visibility = ItemVisibility.MESSAGE)
	private double chromaticRounded = round(chromatic, 3);

	private double spherical = calcSphericalAberration();

	@Parameter(label = "Error disc by spherical aberration (nm)", visibility = ItemVisibility.MESSAGE)
	private double sphericalRounded = round(spherical, 3);

	private double delocalisation = calcDelocalisation();

	@Parameter(label = "Delocalisation (nm)", visibility = ItemVisibility.MESSAGE)
	private double delocalisationRounded = round(delocalisation, 3);

	private double diffractionLimit = calcDiffrationLimit();

	@Parameter(label = "Diffration limit (nm)", visibility = ItemVisibility.MESSAGE)
	private double diffractionLimitRounded = round(diffractionLimit, 3);

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String labelPlot = PLOT_SETTINGS;
	private boolean apertureWasChanged = false;

	/**
	 * A callback to update the wavelength when the primary energy changes.
	 */
	protected void primaryEnergyChanged() {
		wavelength = calcWavelength();
		speed = calcSpeed();
		lambda = 1. * Math.round(wavelength * 100) / 100;
		updateDelocalisation();
		updateChromaticAberration();
		updateDiffrationLimit();
	}

	/**
	 * A callback to update different values when the collection half angle
	 * changes.
	 */
	protected void collectionHalfAngleChanged() {
		updateApertureDiameter();
		updateDelocalisation();
		updateChromaticAberration();
		updateSphericalAberration();
		updateDiffrationLimit();
		apertureWasChanged = false;
	}

	/**
	 * A callback to update the collection half angle or the diameter of the
	 * objective aperture when the focal length changes.
	 */
	protected void focalLengthChanged() {
		if (apertureWasChanged == true) {
			updateCollectionHalfAngle();
		} else {
			updateApertureDiameter();
		}
	}

	/**
	 * @param value
	 *            to be rounded.
	 * @param digits
	 *            after the decimal point.
	 * @return a value rounded to the given precision.
	 */
	private double round(double value, int digits) {
		double factor = Math.pow(10, digits);
		return 1. * Math.round(value * factor) / factor;
	}

	/**
	 * A callback to update the delocalisation. You have to call it if the
	 * primary energy, the energy loss and the collection half-angle changes.
	 */
	protected void updateDelocalisation() {
		delocalisation = calcDelocalisation();
		delocalisationRounded = round(delocalisation, 3);
	}

	/**
	 * A callback to update the chromatic error. You have to call it if the
	 * chromatic aberration, the slit width, the primary energy and the
	 * collection half-angle changes.
	 */
	protected void updateChromaticAberration() {
		chromatic = calcChromaticAberration();
		chromaticRounded = round(chromatic, 3);
	}

	/**
	 * A callback to update the spherical error. You have to call it if the
	 * spherical aberration and the collection half-angle changes.
	 */
	protected void updateSphericalAberration() {
		spherical = calcSphericalAberration();
		sphericalRounded = round(spherical, 3);
	}

	/**
	 * A callback to update the diffraction limit. You have to call it if the
	 * primary energy and the collection half-angle changes.
	 */
	protected void updateDiffrationLimit() {
		diffractionLimit = calcDiffrationLimit();
		diffractionLimitRounded = round(diffractionLimit, 3);
	}

	/**
	 * A callback to update the diameter of the objective aperture. You have to
	 * call it if the collection half angle and the focal length changes.
	 */
	protected void updateApertureDiameter() {
		double _angle = collectionHalfAngle / 1000;
		double _focalLength = focalLength / 1000;
		apertureDiameter = 2 * Math.sin(_angle) * _focalLength * Math.pow(10, 6);
	}

	/**
	 * A callback to update the collection half angle. You have to call it if
	 * the diameter of the objective aperture and the focal length changes.
	 */
	protected void updateCollectionHalfAngle() {
		double _radius = 0.5 * apertureDiameter / Math.pow(10, 6);
		double _focalLength = focalLength / 1000;
		collectionHalfAngle = Math.asin(_radius / _focalLength) * 1000;
		collectionHalfAngleChanged();
		apertureWasChanged = true;
	}

	/**
	 * @param energy
	 *            of the beam electron in keV.
	 * @return the wavelength of to electron with the given energy in pm. A
	 *         relativistic correction of the mass is considered.
	 */
	protected double calcWavelength() {
		return 1240. / Math.sqrt(primaryEnergy * (primaryEnergy + 2 * 511));
	}

	/**
	 * @param energy
	 *            of the beam electron in keV.
	 * @return the relativistic speed of an electron with the given energy in
	 *         m/s.
	 */
	protected double calcSpeed() {
		return LIGHT_SPEED * Math.sqrt(1 - (1 / (1 + primaryEnergy / 511)));
	}

	/**
	 * @param energy
	 *            of the beam electron in keV.
	 * @param loss
	 *            of the electron after specimen interaction in eV.
	 * @param angle
	 *            of collection in mrad. This is the half angle.
	 * @return the delocalisation by inelastic scattering in pm.
	 */
	protected double calcDelocalisation() {
		if (energyLoss == 0)
			return 0;
		// Convert keV -> eV
		double _energy = primaryEnergy * 1000;
		// Convert eV -> J
		double _loss = energyLoss * ELECTRON_CHARGE;
		// Convert mrad -> rad
		double _angle = collectionHalfAngle / 1000;
		double cAngle = energyLoss / (2. * _energy);
		System.out.println(speed);
		return H_BAR * speed * _angle / (_loss * Math.sqrt(
				(Math.pow(_angle, 2) + Math.pow(cAngle, 2)) * Math.log(1 + Math.pow(_angle, 2) / Math.pow(cAngle, 2))))
				* 2 * Math.pow(10, 9);
	}

	/**
	 * @return the diameter of error disk created by the chromatic aberration in
	 *         pm.
	 */
	protected double calcChromaticAberration() {
		double _aberration = chromaticAberration / 1000;
		double _energy = primaryEnergy * 1000;
		double _angle = collectionHalfAngle / 1000;
		return _aberration * slitWidth / _energy * _angle * Math.pow(10, 9);
	}

	/**
	 * @return the diameter of error disk created by the spherical aberration in
	 *         pm.
	 */
	protected double calcSphericalAberration() {
		double _aberration = sphericalAberation / 1000;
		double _angle = collectionHalfAngle / 1000;
		return _aberration * Math.pow(_angle, 3) * Math.pow(10, 9);
	}

	protected double calcDiffrationLimit() {
		double _wavelength = wavelength / Math.pow(10, 12);
		double _angle = collectionHalfAngle / 1000;
		return 0.6 * _wavelength / _angle * Math.pow(10, 9);
	}

	@Override
	public void run() {
		// TODO Create a dialog to choose 2 images/stacks and the properties to
		// copy.
	}

	public static void main(String[] args) throws Exception {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);

		// Launch the "Widget Demo" command right away.
		ij.command().run(ESI_ResolutionCommand.class, true);
	}

}
