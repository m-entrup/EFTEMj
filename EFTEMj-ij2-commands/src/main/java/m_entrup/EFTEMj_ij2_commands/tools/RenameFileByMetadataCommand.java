package m_entrup.EFTEMj_ij2_commands.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;

import net.imagej.ImageJ;

@Plugin(type = Command.class, menuPath = "Plugins>EFTEMj>Tools>Rename file by metadata...")
public class RenameFileByMetadataCommand implements Command {

	@Parameter
	private ImagePlus imp;

	@Override
	public void run() {
		String props = (String) imp.getProperty("Info");
		BufferedReader bufReader = new BufferedReader(new StringReader(props));
		Pattern pattern = Pattern.compile("(?<=Exposure = )(\\d+(?:\\.\\d+)?)");
		String line = null;
		try {
			while ((line = bufReader.readLine()) != null) {
				Matcher match = pattern.matcher(line);
				if (match.find()) {
					System.out.println(match.group());
				} else {
					System.out.println("Not found.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// Launch ImageJ as usual.
		final ImageJ ij = net.imagej.Main.launch(args);

		// TODO: Open an image.

		// Launch the "Widget Demo" command right away.
		ij.command().run(RenameFileByMetadataCommand.class, true);
	}

}
