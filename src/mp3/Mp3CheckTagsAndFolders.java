package mp3;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import mp3.domain.Album;
import mp3.domain.Artist;
import mp3.domain.Track;
import mp3.tasks.RenameAlbumFolderTask;
import mp3.tasks.RenameArtistFolderTask;
import mp3.tasks.RenameTrackFileTask;
import mp3.tasks.Task;

import org.apache.log4j.Logger;
import org.cmc.music.common.ID3ReadException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

/**
 * Goes through all subdirectories of a root folder (constant ROOT) and checks if the names of Mp3 files and their
 * folders match the contents of their ID3 tags. Using ID3v2 tags only and files should obey structure: <Artist
 * Name>/(<Album Year>) <Album Name>/[<Track Artist Name>] <Track Number>. <Track Name>.mp3.
 * 
 * DEPENDS ON: myid3.jar
 * 
 * Created this program to check if the folder structure and file names of the MP3 songs match the ID3 tags set by
 * MusicBrainz Picard after I organize the albums.
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class Mp3CheckTagsAndFolders {
	private static final Logger log = Logger.getLogger(Mp3CheckTagsAndFolders.class);

	private static final String ROOT = "/media/Iomega/Music/ZZ_Temp/3_TagsWritten2/";

	public static void main(String[] args) throws Exception {
		File root = new File(ROOT);
		log.info("Starting Mp3CheckTagsAndFolders. Using root folder: " + root.getAbsolutePath());

		if ((!root.exists()) || (!root.isDirectory())) {
			log.fatal("No such directory: " + root.getAbsolutePath());
			System.exit(-1);
		}

		checkTagsAndFolders(root);
	}

	private static void checkTagsAndFolders(File root) throws ID3ReadException, IOException {
		MyID3 myID3 = new MyID3();
		int diffs = 0;
		SortedSet<Task> tasks = new TreeSet<Task>();

		// Parse the folders and store the information in objects.
		log.info("Parsing root folder...");
		SortedSet<Artist> artists = Mp3Utils.readArtists(root);

		// Analyze the objects.
		for (Artist artist : artists) {
			log.info("Verifying artist: " + artist);

			for (Album album : artist.getAlbums()) {
				log.info("Verifying album: " + album);

				for (Track track : album.getTracks()) {
					log.info("Verifying track: " + track);

					// Reads the meta-data set from the track file and check for differences.
					MusicMetadataSet sourceSet = myID3.read(track.getFile());
					if (sourceSet == null) {
						log.error("No meta-data for file: " + track.getFile().getAbsolutePath());
					}
					else {
						// If meta-data is found, get raw version
						// (The simplified version capitalizes letters and places spaces around dashes).
						// (E.g.: a-ha -> A - Ha)
						IMusicMetadata metadata = sourceSet.id3v2Raw.values;

						// Extract the information for comparison.
						String artistName = Mp3Utils.filterChars(metadata.getArtist());
						String albumName = Mp3Utils.filterChars(metadata.getAlbum());
						String trackName = Mp3Utils.filterChars(metadata.getSongTitle());
						Number trackNumber = metadata.getTrackNumberNumeric();

						// Compare the information extracted from the ID3.
						if (!artist.getName().equals(artistName)) {
							// Artist name is different. Add a task to rename the artist folder.
							log.warn("Diff #" + (++diffs) + ": Artist Folder = [" + artist.getName() + "]; ID3 = [" + artistName + "]");
							tasks.add(new RenameArtistFolderTask(artistName, artist));
						}
						if (!album.getName().equals(albumName)) {
							// Album name is different. Add a task to rename the album folder.
							log.warn("Diff #" + (++diffs) + ": Album Folder = [" + album.getName() + "]; ID3 = [" + albumName + "]");
							tasks.add(new RenameAlbumFolderTask(albumName, album));
						}
						if (!track.getName().equals(trackName)) {
							// Track name is different. Add a task to rename the track file.
							log.warn("Diff #" + (++diffs) + ": Track File = [" + track.getName() + "]; ID3 = [" + trackName + "]");
							tasks.add(new RenameTrackFileTask(trackName, track));
						}
						if (!track.getNumber().equals(trackNumber)) {
							log.warn("Diff #" + (++diffs) + ": Track File = [" + track.getNumber() + "]; ID3 = [" + trackNumber + "]");
						}
					}
				}
			}
		}

		log.warn("Differences found: " + diffs);

		if (diffs == 0) {
			System.out.println();
			System.out.println("No differences found");
		}
		else {
			int fixes = 0;

			// Print a description of the tasks for confirmation.
			System.out.println();
			System.out.println("The following tasks will be ran to fix the differences:");
			int count = 0;
			for (Task t : tasks) {
				System.out.println((++count) + " - " + t);
			}

			// Ask user for confirmation.
			System.out.println();
			System.out.println("Continue? (y/N)");

			// Read confirmation.
			Scanner scanner = new Scanner(System.in);
			String answer = scanner.nextLine();
			if ((answer.length() > 0) && ("y".equalsIgnoreCase("" + answer.charAt(0)))) {
				// Run tasks.
				for (Task t : tasks) {
					log.info("Fix # " + (++fixes) + ". Executing task: " + t);
					t.execute();
				}
			}

			log.warn("Fixes executed: " + fixes);
		}
	}
}
