package m_entrup.EFTEMj_ij2_commands.tools;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>EFTEMj>Tools>Copy properties...")
public class CopyPropertiesCommand implements Command {

	@Override
	public void run() {
		// TODO Create a dialog to choose 2 images/stacks and the properties to copy.
	}

	public static void main(String[] args) {
		// TODO Write code to test this Command
	}

}
