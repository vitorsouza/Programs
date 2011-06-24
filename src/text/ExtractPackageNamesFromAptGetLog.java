package text;

import java.io.File;
import java.util.Scanner;

/**
 * Reads a log file which should contain parts of the /var/log/apt/history.log file that describes the history of operations
 * executed by apt-get, extracts and prints out the names of the packages that were installed.
 * 
 * I created this program to uninstall a program and all of its dependencies that are installed along with it. There's probably a
 * simpler way to do it, but I didn't want to look for it. :)
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class ExtractPackageNamesFromAptGetLog {
	public static void main(String[] args) throws Exception {
		File log = new File("apt.log");
		Scanner scanner = new Scanner(log);
		String line = scanner.nextLine();

		StringBuilder names = new StringBuilder();

		Scanner lineScanner = new Scanner(line);
		lineScanner.useDelimiter("\\),\\s*");
		while (lineScanner.hasNext()) {
			String token = lineScanner.next();
			int idx = token.indexOf(':');
			String pkg = token.substring(0, idx);
			names.append(pkg).append(' ');
			System.out.println(pkg);
		}

		System.out.println(names);
	}
}
