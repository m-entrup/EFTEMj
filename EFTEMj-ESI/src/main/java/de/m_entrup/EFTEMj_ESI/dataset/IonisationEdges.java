
package de.m_entrup.EFTEMj_ESI.dataset;

import java.util.LinkedHashMap;

import de.m_entrup.EFTEMj_ESI.plugin.PluginAPI;

public class IonisationEdges {

	/**
	 * There is only one instance of {@link IonisationEdges}. That is why the
	 * Singleton pattern is used.
	 */
	private static final IonisationEdges INSTANCE = new IonisationEdges();

	/**
	 * Instead of using the constructor you can get an instance of
	 * {@link PluginAPI} by using this method.
	 *
	 * @return The only instance of {@link PluginAPI}
	 */
	public static IonisationEdges getInstance() {
		return INSTANCE;
	}

	private final LinkedHashMap<Integer, String> edges;

	/**
	 * A private constructor that creates the {@link LinkedHashMap} edges.
	 */
	private IonisationEdges() {
		edges = new LinkedHashMap<Integer, String>();
		edges.put(51, "Magnesium L<sub>2,3</sub>-edge");
		edges.put(73, "Aluminium L<sub>2,3</sub>-edge");
		edges.put(86, "Lead O<sub>2,3</sub>-edge");
		edges.put(96, "Uranium O<sub>4,5</sub>-edge");
		edges.put(99, "Silicon L<sub>2,3</sub>-edge");
		edges.put(132, "Phosphorus L<sub>2,3</sub>-edge");
		edges.put(165, "Sulfur L<sub>2,3</sub>-edge");
		edges.put(188, "Boron K-edge");
		edges.put(227, "Molybdenium M<sub>4,5</sub>-edge");
		edges.put(284, "Carbon K-edge");
		edges.put(346, "Calcium L<sub>3</sub>-edge");
		edges.put(350, "Calcium L<sub>2</sub>-edge");
		edges.put(367, "Silver M<sub>4,5</sub>-edge");
		edges.put(381, "Uranium N<sub>7</sub>-edge");
		edges.put(391, "Uranium N<sub>6</sub>-edge");
		edges.put(401, "Nitrogen K-edge");
		edges.put(456, "Titan L<sub>3</sub>-edge");
		edges.put(462, "Titan L<sub>2</sub>-edge");
		edges.put(512, "Vanadium L<sub>3</sub>-edge");
		edges.put(521, "Vanadium L<sub>2</sub>-edge");
		edges.put(532, "Oxygen K-edge");
		edges.put(575, "Cromium L<sub>3</sub>-edge");
		edges.put(584, "Cromium L<sub>2</sub>-edge");
		edges.put(640, "Manganese L<sub>3</sub>-edge");
		edges.put(651, "Manganese L<sub>2</sub>-edge");
		edges.put(685, "Fluorine K-edge");
		edges.put(708, "Iron L<sub>3</sub>-edge");
		edges.put(721, "Iron L<sub>2</sub>-edge");
		edges.put(779, "Cobalt L<sub>3</sub>-edge");
		edges.put(794, "Cobalt L<sub>2</sub>-edge");
		edges.put(855, "Nickel L<sub>3</sub>-edge");
		edges.put(872, "Nickel L<sub>2</sub>-edge");
		edges.put(931, "Copper L<sub>3</sub>-edge");
		edges.put(951, "Copper L<sub>2</sub>-edge");
		edges.put(1020, "Zinc L<sub>3</sub>-edge");
		edges.put(1043, "Zinc L<sub>2</sub>-edge");
		edges.put(1072, "Natrium K-egde");
		edges.put(1115, "Gallium L<sub>3</sub>-edge");
		edges.put(1142, "Gallium L<sub>2</sub>-edge");
		edges.put(1217, "Germanium L<sub>3</sub>-edge");
		edges.put(1248, "Germanium L<sub>2</sub>-edge");
		edges.put(1305, "Magnesium K-edge");
		edges.put(1560, "Aluminium K-edge");
		edges.put(1839, "Silicon K-edge");
		edges.put(2146, "Phosphorus K-edge");
		edges.put(2206, "Gold M<sub>5</sub>-edge");
		edges.put(2291, "Gold M<sub>4</sub>-edge");
		edges.put(2484, "Lead M<sub>5</sub>-edge");
		edges.put(2586, "Lead M<sub>4</sub>-edge");
		edges.put(2520, "Molybdenium L<sub>3</sub>-edge");
		edges.put(2625, "Molybdenium L<sub>2</sub>-edge");
		edges.put(3351, "Silver L<sub>3</sub>-edge");
		edges.put(3524, "Silver L<sub>2</sub>-edge");
		edges.put(3552, "Uranium M<sub>5</sub>-edge");
		edges.put(3728, "Uranium M<sub>4</sub>-edge");
	}

	public LinkedHashMap<Integer, String> getEdges() {
		return edges;
	}
}
