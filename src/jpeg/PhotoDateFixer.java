package jpeg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

/**
 * Processes all JPEG photos of a specific folder (constant ROOT_FOLDER_PATH) and for each of them tries to set EXIF
 * date information based on the photo's name using a year-month-date-hour-minute-second date pattern (constant df).
 * 
 * DEPENDS ON: commons-imaging-1.0
 * 
 * I created this program because sometimes my wife and I take pictures using different cameras in the same trip and I'd
 * like to have these pictures ordered by the date they were taken in the folder that contains the pictures of the trip.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class PhotoDateFixer {
	private static final String ROOT_FOLDER_PATH = "/Users/vitor/Temp/Pics/";

	private static File rootFolder;

	private static DateFormat dfIn = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static DateFormat dfOut = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

	private static final Map<String, File> photoMap = new HashMap<>();

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
		try (Scanner in = new Scanner(System.in)) {

			// Goes through all the JPEG files in the folder.
			for (File photoFile : folder.listFiles(jpegFilter)) {
				// Extracts the date from the file name and place it in a map.
				String name = photoFile.getName();
				name = name.substring(0, name.lastIndexOf('.'));
				Date date = dfIn.parse(name);
				photoMap.put(dfOut.format(date), photoFile);
			}

			// Asks the user if she confirms the dates for the files.
			System.out.println("Will set the following dates/times in the photos:");
			dfIn = new SimpleDateFormat("dd/MM/yyyy hh:MM:ss");
			for (Map.Entry<String, File> entry : photoMap.entrySet())
				System.out.printf("\t%s\t%s%n", entry.getValue().getName(), entry.getKey());
			System.out.print("\nConfirm? (y/N) ");
			String response = in.nextLine();

			// Continue if response was positive.
			if ("y".equals(response.toLowerCase())) {
				System.out.println("Processing...");
				for (Map.Entry<String, File> entry : photoMap.entrySet()) {
					String photoDate = entry.getKey();
					File photoFile = entry.getValue();
					String photoName = photoFile.getName();
					System.out.printf("\t%s%n", photoName);

					// The code below was adapted from
					// https://commons.apache.org/proper/commons-imaging/xref-test/org/apache/commons/imaging/examples/WriteExifMetadataExample.html
					TiffOutputSet outputSet = null;
					final ImageMetadata metadata = Imaging.getMetadata(photoFile);
					final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
					if (jpegMetadata != null) {
						final TiffImageMetadata exif = jpegMetadata.getExif();
						if (exif != null) outputSet = exif.getOutputSet();
					}
					if (outputSet == null) outputSet = new TiffOutputSet();
					final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
					exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
					exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
					exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, photoDate);
					exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, photoDate);
					File outputFile = new File(photoFile.getParent(), "_" + photoName);
					OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
					new ExifRewriter().updateExifMetadataLossless(photoFile, os, outputSet);
					
					// If all went well, deletes the old file and replaces it with the new one.
					photoFile.delete();
					outputFile.renameTo(photoFile);
				}
			}
			
			System.out.println("Done!");
		}
	}
}
