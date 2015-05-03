package jpeg;

import java.io.File;
import java.io.FileFilter;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;

/**
 * Processes all JPEG photos of a specific folder (constant ROOT_FOLDER_PATH) and for each of them tries to extract EXIF
 * device make and model and, if successful, moves the file to a folder with that name.
 * 
 * DEPENDS ON: metadata-extractor-2.3.1.jar
 * 
 * I created this program because sometimes my wife and I take pictures using different cameras in the same trip and I'd
 * like to have these pictures ordered by the date they were taken in the folder that contains the pictures of the trip.
 * Separating the photos by camera helps in the process.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class PhotoOrganizer {
	private static final String ROOT_FOLDER_PATH = "/Users/vitor/Temp/Pics/";

	private static File rootFolder;

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
		rootFolder = new File(ROOT_FOLDER_PATH);
		processFolder(rootFolder);
	}

	private static void processFolder(File folder) throws Exception {
		// Goes through all the JPEG files in the folder.
		for (File photoFile : folder.listFiles(jpegFilter)) {
			String make = "";
			String model = "";

			// Extracts the meta-data.
			Metadata metadata = JpegMetadataReader.readMetadata(photoFile);

			// Obtains the EXIF directory in the meta-data, checking if it exists.
			Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			if (directory == null) {
				System.out.println("[WARNING] " + photoFile.getName() + "'s meta-data doesn't contain an EXIF directory.");
			}
			else {
				// Checks for different date tags.
				if (directory.containsTag(ExifIFD0Directory.TAG_MAKE)) make = directory.getString(ExifIFD0Directory.TAG_MAKE);
				if (directory.containsTag(ExifIFD0Directory.TAG_MODEL)) model = directory.getString(ExifIFD0Directory.TAG_MODEL);
			}

			// Creates the name of the sub-directory.
			String sub = (make.trim() + " - " + model.trim()).trim();

			// Checks if it exists. If not, create it.
			File subDir = new File(rootFolder, sub);
			if (subDir.exists() && !subDir.isDirectory()) {
				System.out.println("[ERROR] The file system entry \"" + sub + "\" exists and it's not a directory.");
				System.exit(1);
			}
			else if (!subDir.exists()) subDir.mkdir();

			// Move the photo file to the subDir.
			File newFile = new File(subDir, photoFile.getName());
			photoFile.renameTo(newFile);
			System.out.println("[INFO] Photo file \"" + newFile.getName() + "\" moved to \"" + subDir.getName() + "\"");
		}
	}
}
