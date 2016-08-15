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

	private double wavelength = calcWavelength(primaryEnergy);

	private double speed = calcSpeed(primaryEnergy);

	@Parameter(label = "Wavelength (pm)", visibility = ItemVisibility.MESSAGE)
	private double lambda = 1. * Math.round(wavelength * 100) / 100;

	@Parameter(label = "Energy loss (eV)", callback = "updateDelocalisation")
	private double energyLoss = 0;

	@Parameter(label = "Collection half angle (mrad)", callback = "updateDelocalisation")
	private double collectionHalfAngle = 10;

	@Parameter(label = "Delocalisation (pm)", visibility = ItemVisibility.MESSAGE)
	private double delocalisation = calcDelocalisation(primaryEnergy, energyLoss, collectionHalfAngle);

	@Parameter(label = "Focal length (mm)")
	private double focalLength = 1.72;

	@Parameter(label = "Aperture diameter (µm)")
	private double apertureDiameter = 40;

	@Parameter(label = "Spherical aberation (mm)")
	private double sphericalAberation = 1.2;

	@Parameter(label = "Chromatic aberation (mm)")
	private double chromaticAberation = 1.2;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private final String labelPlot = PLOT_SETTINGS;

	/**
	 * A callback to update the wavelength when the primary energy changes.
	 */
	protected void primaryEnergyChanged() {
		wavelength = calcWavelength(primaryEnergy);
		speed = calcSpeed(primaryEnergy);
		lambda = 1. * Math.round(wavelength * 100) / 100;
		updateDelocalisation();
	}

	/**
	 * A callback to update the delocalisation. You have to call it if the
	 * primary energy, the energy loss and the collection half-angle changes.
	 */
	protected void updateDelocalisation() {
		delocalisation = calcDelocalisation(primaryEnergy, energyLoss, collectionHalfAngle);
	}

	/**
	 * @param energy
	 *            of the beam electron in keV.
	 * @return the wavelength of to electron with the given energy in pm. A
	 *         relativistic correction of the mass is considered.
	 */
	protected double calcWavelength(double energy) {
		return 1240. / Math.sqrt(energy * (energy + 2 * 511));
	}

	/**
	 * @param energy
	 *            of the beam electron in keV.
	 * @return the relativistic speed of an electron with the given energy in
	 *         m/s.
	 */
	protected double calcSpeed(double energy) {
		return LIGHT_SPEED * Math.sqrt(1 - (1 / (1 + energy / 511)));
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
	protected double calcDelocalisation(double energy, double loss, double angle) {
		if (loss == 0)
			return 0;
		// Convert keV -> eV
		double _energy = energy * 1000;
		// Convert eV -> J
		double _loss = loss * ELECTRON_CHARGE;
		// Convert mrad -> rad
		double _angle = angle / 1000;
		double cAngle = loss / (2. * _energy);
		System.out.println(speed);
		return H_BAR * speed * _angle / (_loss * Math.sqrt(
				(Math.pow(_angle, 2) + Math.pow(cAngle, 2)) * Math.log(1 + Math.pow(_angle, 2) / Math.pow(cAngle, 2))))
				* 2 * Math.pow(10, 12);
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
