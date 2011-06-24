package jpeg;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;

/**
 * Processes all JPEG photos of a specific folder (constant ROOT_FOLDER_PATH) and for each of them tries to extract EXIF date
 * information and, if successful, rename the photo using a year-month-date-hour-minute-second date pattern (constant df).
 * 
 * DEPENDS ON: metadata-extractor-2.3.1.jar
 * 
 * I created this program because sometimes my wife and I take pictures using different cameras in the same trip and I'd like to
 * have these pictures ordered by the date they were taken in the folder that contains the pictures of the trip.
 * 
 * TODO: thinking of adding to the file name after the timestamp something that identifies the camera that was used (will look in
 * the EXIF if I can find the brand of the machine). Also I'm thinking of making this program recurse through subdirectories, so I
 * can apply to my whole photo collection.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class PhotoRenamer {
	private static final String ROOT_FOLDER_PATH = "/home/vitor/Pictures/2011/04/22";

	private static final String FINAL_EXTENSION = "jpg";

	private static final DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");

	private static final FileFilter jpegFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String fileName = file.getName();
			int idx = fileName.lastIndexOf('.');
			if (idx == -1)
				return false;
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
				String newName = df.format(photoDate);
				renamePhoto(folder, photoFile, newName);
			}
		}
	}

	private static Date extractDate(File photoFile) throws Exception {
		// Extracts the meta-data from the JPEG, checking if there is any.
		Metadata metadata = JpegMetadataReader.readMetadata(photoFile);
		if (metadata == null) {
			System.out.println("[WARNING] " + photoFile.getName() + " doesn't contain meta-data.");
			return null;
		}

		// Obtains the EXIF directory in the meta-data, checking if it exists.
		Directory directory = metadata.getDirectory(ExifDirectory.class);
		if (directory == null) {
			System.out.println("[WARNING] " + photoFile.getName() + "'s meta-data doesn't contain an EXIF directory.");
			return null;
		}

		// Checks for different date tags, returning the value of the first one that exists.
		int[] dateTags = new int[] { ExifDirectory.TAG_DATETIME, ExifDirectory.TAG_DATETIME_ORIGINAL, ExifDirectory.TAG_DATETIME_DIGITIZED };
		for (int i = 0; i < dateTags.length; i++)
			if (directory.containsTag(dateTags[i]))
				return directory.getDate(dateTags[i]);

		// If nothing was found, return null.
		return null;
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
