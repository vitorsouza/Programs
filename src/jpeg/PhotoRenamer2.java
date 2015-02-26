package jpeg;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO: document this type.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class PhotoRenamer2 {
	private static final String ROOT_FOLDER_PATH = "/home/vitor/Temp/Tattoo";

	private static final String FINAL_EXTENSION = "jpg";

	private static final DateFormat dfIn = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private static final DateFormat dfOut = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");

	private static final FileFilter jpegFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String fileName = file.getName();
			int idx = fileName.lastIndexOf('.');
			if (idx == -1) return false;
			String ext = fileName.substring(idx + 1).toLowerCase();
			return "jpg".equals(ext) || "jpeg".equals(ext);
		}
	};

	public static void main(String[] args) throws Exception {
		File rootFolder = new File(ROOT_FOLDER_PATH);
		processFolder(rootFolder);
	}

	private static void processFolder(File folder) throws Exception {
		// Goes through all the JPEG files in the folder.
		for (File photoFile : folder.listFiles(jpegFilter)) {
			// Extracts the date out of the photo file.
			Date photoDate = extractDate(photoFile);

			// Checks if there's no date tag.
			if (photoDate == null) {
				System.out.println("[WARNING] " + photoFile.getName() + ": date tag not found.");
			}

			// If it was found, rename the photo.
			else {
				String newName = dfOut.format(photoDate);
				renamePhoto(folder, photoFile, newName);
			}
		}
	}

	private static Date extractDate(File photoFile) throws Exception {
		Date date = null;

		// Extracts the date from name similar to "IMG_20110303_144735.jpg":
		String name = photoFile.getName();
		int idx = name.indexOf("IMG_");
		if (idx != -1) {
			name = name.substring(idx + 4);
			name = name.substring(0, name.length() - 4);
			date = dfIn.parse(name);
		}

		return date;
	}

	private static void renamePhoto(File folder, File photoFile, String newName) throws Exception {
		// Creates a file descriptor for the new name.
		String newNameFull = newName + "." + FINAL_EXTENSION;
		File newPhotoFile = new File(folder, newNameFull);

		// First check if there's no need to rename anything: the photo already has the standard name.
		if (photoFile.getName().equals(newNameFull)) {
			System.out.println("[INFO] " + photoFile.getName() + " already has a name following the standard. Nothing to be done!");
			return;
		}

		// Checks if a file with the new name already exists. If it does, try alternate names until one is available.
		char alt = 'b';
		while (newPhotoFile.exists()) {
			System.out.println("[INFO] " + photoFile.getName() + ": a photo with name " + newName + " already exists. Will try appending '" + alt + "' to the name.");
			newNameFull = newName + alt + "." + FINAL_EXTENSION;
			newPhotoFile = new File(folder, newNameFull);
			alt++;
		}

		// Rename the file.
		photoFile.renameTo(newPhotoFile);
		System.out.println("[INFO] " + photoFile.getName() + " -> " + newPhotoFile.getName());
	}
}
