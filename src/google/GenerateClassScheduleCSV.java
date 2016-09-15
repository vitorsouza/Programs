package google;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Creates a CSV file with the schedule for a university course based on the following information from the course:
 * 
 * <ul>
 * <li>days: comma-separated list of days of the week in which the class takes place (1 is Sunday, 2 is Monday, etc.);</li>
 * <li>ch: comma-separated list of amount of hours per class, for each day;</li>
 * <li>amount: amount of classes in a semester;</li>
 * <li>classname: prefix for the class description (e.g., "Class 1:", "Class 2:" and so on);</li>
 * <li>noclassname: description to use for days after the amount of classes is complete (e.g.,
 * "Reserved slot, if needed");</li>
 * <li>holidayname: prefix for holidays (e.g., "No class because of holiday: ");</li>
 * <li>dateformat: {@link java.util.SimpleDateFormat} pattern used to parse and format dates;</li>
 * <li>startdate: date in which the academic semester starts (observing the dateformat pattern);</li>
 * <li>enddate: date in which the academic semester ends (observing the dateformat pattern);</li>
 * <li>holidays: comma-separated list of dates in which there's no class because of a holiday (observing the dateformat
 * pattern);</li>
 * </ul>
 * 
 * All the above information should be properly written in the file specified in the <code>PROPERTIES_FILE</code>
 * constant. Change it before running the class.
 * 
 * The output is a .csv file with the same name as the <code>PROPERTIES_FILE</code> (replacing .properties with .csv)
 * containing four columns: date, description, blank (to be filled in manually with observations) and amount of hours.
 * 
 * I created this script in order to import that CSV into a spreadsheet in which I write the schedule for a given class.
 * Afterwards, I complete each class description and observation columns, create a 6th column with the class room
 * number, export it back to the .csv file and use {@link CreateGoogleAppointmentsForClass} to create a Google Calendar
 * for the class.
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class GenerateClassScheduleCSV {
	private static final String PROPERTIES_FILE = "lp-2016-2.properties";

	private static final char CSV_DELIMITER = ';';

	public static void main(String[] args) throws Exception {
		// Loads the properties file.
		Properties props = new Properties();
		props.load(new FileReader(new File(PROPERTIES_FILE)));

		// Creates a map with days and expected class hours.
		Map<Integer, Integer> daysMap = new HashMap<>();
		try (Scanner daysScanner = new Scanner(props.getProperty("days")); Scanner chScanner = new Scanner(props.getProperty("ch"))) {
			daysScanner.useDelimiter("\\s*,\\s*");
			chScanner.useDelimiter("\\s*,\\s*");
			while (daysScanner.hasNext() && chScanner.hasNext())
				daysMap.put(Integer.parseInt(daysScanner.next()), Integer.parseInt(chScanner.next()));
		}

		// Reads the other properties from the file.
		int amountClasses = Integer.parseInt(props.getProperty("amount"));
		String className = props.getProperty("classname");
		String noClassName = props.getProperty("noclassname");
		String holidayName = props.getProperty("holidayname");
		DateFormat df = new SimpleDateFormat(props.getProperty("dateformat"));
		Date startDate = df.parse(props.getProperty("startdate"));
		Date endDate = df.parse(props.getProperty("enddate"));

		// Last information from the file, the set of holidays.
		Set<Date> holidaysSet = new TreeSet<>();
		try (Scanner holidaysScanner = new Scanner(props.getProperty("holidays"))) {
			holidaysScanner.useDelimiter("\\s*,\\s*");
			while (holidaysScanner.hasNext())
				holidaysSet.add(df.parse(holidaysScanner.next()));
		}

		// Class counter to be incremented.
		int count = 1;

		// Starts the calendar at the start date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		Date date = calendar.getTime();

		// Determines the output file name from the properties file name. Creates a writer on it.
		String outFileName = PROPERTIES_FILE.substring(0, PROPERTIES_FILE.lastIndexOf('.')) + ".csv";
		File outFile = new File(outFileName);
		try (PrintWriter out = new PrintWriter(outFile)) {
			// Goes through all the days in the academic period.
			while (date.before(endDate) || date.equals(endDate)) {
				// Checks if the weekday is one in which there is a class.
				if (daysMap.containsKey(calendar.get(Calendar.DAY_OF_WEEK))) {
					// Creates a line for this day.
					StringBuilder builder = new StringBuilder();
					builder.append(df.format(date)).append(CSV_DELIMITER);

					// Checks if the day is a holiday.
					if (holidaysSet.contains(date)) builder.append(holidayName).append(": ").append(CSV_DELIMITER).append(CSV_DELIMITER).append("-");

					// Checks if the amount of classes has been reached.
					else if (count > amountClasses) builder.append(noClassName).append(CSV_DELIMITER).append(CSV_DELIMITER).append("-");
					else builder.append(className).append(' ').append(count++).append(": ").append(CSV_DELIMITER).append(CSV_DELIMITER).append(daysMap.get(calendar.get(Calendar.DAY_OF_WEEK)));

					// Writes the line relative to this day and class.
					out.println(builder.toString());
				}

				// Moves to the next day, clears the builder.
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				date = calendar.getTime();
			}
		}

		System.out.println("File written: " + outFile.getAbsolutePath());
	}

}
