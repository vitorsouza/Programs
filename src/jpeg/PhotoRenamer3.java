package jpeg;

import java.io.File;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class PhotoRenamer3 {
	private static final String PATH = "/Users/vitor/Temp/Transfer/Cellphotos/";

	public static void main(String[] args) {
		File folder = new File(PATH);
		for (File file : folder.listFiles()) {
			String name = file.getName();
			if (name.endsWith(".jpg")) {
				StringBuilder newName = new StringBuilder();
				newName.append(name.substring(4, 8)).append('-').append(name.substring(8, 10)).append('-').append(name.substring(10, 12)).append(' ').append(name.substring(13, 15)).append('.').append(name.substring(15, 17)).append('.').append(name.substring(17, 19)).append(".jpg");
				System.out.println(name + " -> " + newName);
				file.renameTo(new File(folder, newName.toString()));
			}
		}
	}
}
