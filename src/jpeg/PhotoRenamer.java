package jpeg;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;

/**
 * Processes all JPEG photos of a specific folder (constant ROOT_FOLDER_PATH) and for each of them tries to extract EXIF
 * date information and, if successful, rename the photo using a year-month-date-hour-minute-second date pattern
 * (constant df).
 * 
 * DEPENDS ON: metadata-extractor-2.3.1.jar
 * 
 * I created this program because sometimes my wife and I take pictures using different cameras in the same trip and I'd
 * like to have these pictures ordered by the date they were taken in the folder that contains the pictures of the trip.
 * 
 * TODO: thinking of adding to the file name after the timestamp something that identifies the camera that was used
 * (will look in the EXIF if I can find the brand of the machine). Also I'm thinking of making this program recurse
 * through subdirectories, so I can apply to my whole photo collection.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class PhotoRenamer {
	private static final String ROOT_FOLDER_PATH = "/Users/vitor/Temp/Pics/";

	private static final String FINAL_EXTENSION = "jpg";

	private static File rootFolder;

	private static final DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private static final List<File> photoFiles = new ArrayList<File>();

	private static final List<Date> photoDatesOriginal = new ArrayList<Date>();

	private static final List<Date> photoDatesDigitized = new ArrayList<Date>();

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
		promptUser();
	}

	private static void processFolder(File folder) throws Exception {
		// Goes through all the JPEG files in the folder.
		for (File photoFile : folder.listFiles(jpegFilter)) {
			// Places the files and the dates in the lists for future processing.
			photoFiles.add(photoFile);
			extractDates(photoFile);
		}
	}

	private static void extractDates(File photoFile) throws Exception {
		Date originalDate = null, digitizedDate = null;

		// Extracts the meta-data from the JPEG, checking if there is any.
		Metadata metadata = JpegMetadataReader.readMetadata(photoFile);
		if (metadata == null) {
			System.out.println("[WARNING] " + photoFile.getName() + " doesn't contain meta-data.");
		}
		else {
			// Obtains the EXIF directory in the meta-data, checking if it exists.
			Directory directory = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);
			if (directory == null) {
				System.out.println("[WARNING] " + photoFile.getName() + "'s meta-data doesn't contain an EXIF directory.");
			}
			else {
				// Checks for different date tags.
				if (directory.containsTag(ExifDirectoryBase.TAG_DATETIME_DIGITIZED)) originalDate = directory.getDate(ExifDirectoryBase.TAG_DATETIME_DIGITIZED);
				if (directory.containsTag(ExifDirectoryBase.TAG_DATETIME_ORIGINAL)) digitizedDate = directory.getDate(ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
			}
		}

		photoDatesOriginal.add(originalDate);
		photoDatesDigitized.add(digitizedDate);
	}

	private static void promptUser() throws Exception {
		String command = "";

		Calendar calendar = Calendar.getInstance();
		Date firstDate = photoDatesOriginal.get(0);
		if (firstDate == null) firstDate = photoDatesDigitized.get(0);
		if (firstDate != null) calendar.setTime(firstDate);
		int referenceYear = (firstDate == null) ? 0 : calendar.get(Calendar.YEAR);

		// Loops until the user quits the program.
		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				// Prints the list of file and their proposed new names.
				System.out.println("\nReference year: " + referenceYear + "\n");
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < photoFiles.size(); i++) {
					File photoFile = photoFiles.get(i);
					Date photoDateOriginal = photoDatesOriginal.get(i);
					Date photoDateDigitized = photoDatesDigitized.get(i);

					if (photoDateOriginal != null) calendar.setTime(photoDateOriginal);
					int photoYear = (photoDateOriginal == null) ? 0 : calendar.get(Calendar.YEAR);
					builder.append(i).append('\t').append(photoFile.getName());
					String photoName = (photoDateOriginal == null) ? null : df.format(photoDateOriginal);
					builder.append('\t').append(photoName).append(((referenceYear == photoYear) ? "   " : " Y!"));
					photoName = (photoDateDigitized == null) ? null : df.format(photoDateDigitized);
					builder.append('\t').append(photoName).append('\n');
				}

				// Prints the prompt and reads the command.
				System.out.println(builder.toString());
				System.out.println("[Q = Quit, # = Fix photo number #, +# = advance all photos plus # hours, %# = ditto, for minutes, C = Copy from digitized, N = Fix all nulls, Y = Fix different years, R = Rename the files and quits.]");
				System.out.print("> ");
				command = scanner.nextLine();

				// Execute the commands.
				if (command != null && command.length() > 0) switch (command.toUpperCase().charAt(0)) {
				case 'Q':
					// Quits.
					System.out.println("Ciao!");
					return;

				case 'N':
					// Fixes all photos that have no name assigned.
					for (int i = 0; i < photoFiles.size(); i++)
						if (photoDatesOriginal.get(i) == null) fixName(i);
					break;

				case 'Y':
					fixDifferentYear();
					break;

				case 'R':
					renamePhotos();
					break;

				case 'C':
					copyFromDigitized();
					break;

				case '+':
					advanceHour(command.substring(1));
					break;

				case '%':
					advanceMinute(command.substring(1));
					break;

				default:
					try {
						int photoNumber = Integer.parseInt(command);
						fixName(photoNumber);
					}
					catch (NumberFormatException e) {
						System.out.println("Unknown command: " + command);
					}
				}
			}
		}
	}

	private static void fixName(int number) throws Exception {
		// Checks if the photo exists.
		if ((number < 0) || (number >= photoFiles.size())) {
			System.out.println("Photo doesn't exist: " + number);
			return;
		}

		// Can't fix the first photo.
		if (number == 0) {
			System.out.println("Sorry, can't fix the first photo.");
			return;
		}

		// Sets the date of the file as one second after the previous file.
		Date previousDate = photoDatesOriginal.get(number - 1);
		Date newDate = new Date(previousDate.getTime() + 1000);
		photoDatesOriginal.set(number, newDate);
	}

	private static void fixDifferentYear() throws Exception {
		// Uses as reference the first photo.
		Date referenceDate = photoDatesOriginal.get(0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(referenceDate);
		int referenceYear = calendar.get(Calendar.YEAR);

		// Checks all other dates.
		for (int i = 1; i < photoFiles.size(); i++) {
			calendar.setTime(photoDatesOriginal.get(i));
			int year = calendar.get(Calendar.YEAR);
			if (year != referenceYear) fixName(i);
		}
	}

	private static void advanceHour(String param) throws Exception {
		int hours = Integer.parseInt(param);
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < photoFiles.size(); i++) {
			calendar.setTime(photoDatesOriginal.get(i));
			calendar.add(Calendar.HOUR, hours);
			photoDatesOriginal.set(i, calendar.getTime());
		}
	}

	private static void advanceMinute(String param) throws Exception {
		int minutes = Integer.parseInt(param);
		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < photoFiles.size(); i++) {
			calendar.setTime(photoDatesOriginal.get(i));
			calendar.add(Calendar.MINUTE, minutes);
			photoDatesOriginal.set(i, calendar.getTime());
		}
	}

	private static void copyFromDigitized() throws Exception {
		for (int i = 0; i < photoFiles.size(); i++)
			if (photoDatesOriginal.get(i) == null) photoDatesOriginal.set(i, photoDatesDigitized.get(i));
	}

	private static void renamePhotos() throws Exception {
		// Checks if there are any nulls.
		for (int i = 0; i < photoFiles.size(); i++) {
			if (photoDatesOriginal.get(i) == null) {
				System.out.println("Can't rename. There are still nulls in the table. Fix it first.");
				return;
			}
		}

		// Renames all photos.
		for (int i = 0; i < photoFiles.size(); i++) {
			String photoName = df.format(photoDatesOriginal.get(i));
			renamePhoto(rootFolder, photoFiles.get(i), photoName);
		}
		System.exit(0);
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
