package mp3;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

/**
 * Starting from a root source folder (constant SRC_FOLDER), navigate through all sub-directories considering a pre-defined
 * structure: Artist/Album/Song.mp3 and, for each MP3 file found, creates a new ID3 metadata reading the names of the directories
 * and moves the songs to a destination directory (constant DST_FOLDER) using the same folder structure.
 * 
 * DEPENDS ON: myid3.jar 
 * 
 * I created this program because I like to organize my MP3 albums. First I tag them with this program, then I listen to the
 * album. Then I use MusicBrainz Picard for the definitive tagging of the MP3s if I decide to keep them. Tagging them with this
 * program before provides some information for my music player when I listen to the album for the 1st time and also helps Picard
 * find the album in MusicBrainz database.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Mp3AddTagsFromFolderStructure {
	private static final Logger log = Logger.getLogger(Mp3CheckTagsAndFolders.class);

	private static final String SRC_FOLDER = "/media/Iomega/Music/ZZ_Temp/2_TagWithJava/";

	private static final String DST_FOLDER = "/media/Iomega/Music/ZZ_Temp/3_Listen/";

	private static final FileFilter folderFilter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};

	private static final FileFilter mp3Filter = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".mp3");
		}
	};

	public static void main(String[] args) throws Exception {
		File srcFolder = new File(SRC_FOLDER);
		log.info("Starting Mp3AddTagsFromFolderStructure. Using root folder: " + srcFolder.getAbsolutePath());

		if ((!srcFolder.exists()) || (!srcFolder.isDirectory())) {
			log.fatal("No such directory: " + srcFolder.getAbsolutePath());
			System.exit(-1);
		}

		File dstFolder = new File(DST_FOLDER);
		if (!dstFolder.exists()) {
			log.info("Creating destination folder: " + dstFolder.getAbsolutePath());
			dstFolder.mkdir();
		}
		else if (!dstFolder.isDirectory()) {
			log.fatal("Not a directory: " + dstFolder.getAbsolutePath());
			System.exit(-1);
		}

		addTagsFromFolderStructure(srcFolder, dstFolder);
		log.info("Done! JVM should delete the original MP3 files and folders now...");
	}

	private static void addTagsFromFolderStructure(File srcFolder, File dstFolder) throws Exception {
		MyID3 myID3 = new MyID3();

		// First sub-folder should be the artist.
		File[] artistFolders = srcFolder.listFiles(folderFilter);
		for (File artistFolder : artistFolders) {
			String artist = artistFolder.getName();
			log.info("Processing albums by: " + artist);

			// Creates the artists destination folder.
			File artistDstFolder = createDestinationFolder(dstFolder, artist);

			// Second sub-folder should be album.
			File[] albumFolders = artistFolder.listFiles(folderFilter);
			for (File albumFolder : albumFolders) {
				String album = albumFolder.getName();
				log.info("Processing album: " + album);

				// Creates the album temporary folder.
				File albumDstFolder = createDestinationFolder(artistDstFolder, album);

				// Inside the second sub-folder we find the mp3 files.
				File[] mp3Files = albumFolder.listFiles(mp3Filter);
				for (File mp3 : mp3Files) {
					String song = mp3.getName();

					// Strip the extension.
					song = song.substring(0, song.lastIndexOf('.'));
					log.info("Processing song: " + song);

					// Create the file object representing the MP3 file that will have the tags.
					File dstMp3 = new File(albumDstFolder, mp3.getName());
					log.info("Will create a new MP3 file with tags at this location: " + dstMp3.getAbsolutePath());

					// Read the current ID3 tag in the song.
					MusicMetadataSet sourceSet = myID3.read(mp3);

					// Create an ID3 tag for the song.
					MusicMetadata newID3 = MusicMetadata.createEmptyMetadata();
					newID3.setArtist(artist);
					newID3.setAlbum(album);
					newID3.setSongTitle(song);

					// Write the ID3 tag in the file.
					myID3.write(mp3, dstMp3, sourceSet, newID3);

					// Request the deletion of the original mp3 file once the program finishes.
					mp3.deleteOnExit();
				}
			}
		}
	}

	private static File createDestinationFolder(File parentFolder, String folderName) {
		File dstFolder = new File(parentFolder, folderName);

		// Checks if the folder already exists.
		if (dstFolder.exists()) {
			// Checks if it is indeed a folder.
			if (!dstFolder.isDirectory()) {
				log.fatal("Not a directory: " + dstFolder.getAbsolutePath());
				System.exit(-1);
			}
			else log.info("Destination folder already exists. Using it: " + dstFolder.getAbsolutePath());
		}

		// Doesn't exist, let's create it.
		else {
			log.info("Creating destination folder: " + dstFolder.getAbsolutePath());
			dstFolder.mkdir();
		}

		return dstFolder;
	}

}
