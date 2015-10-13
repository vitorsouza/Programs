package google;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * Creates a Google Calendar with events for each class, plus each no-class reserved slot. Currently does not create
 * whole-day events for deadlines nor an event for the final exam, which should be created manually afterwards.
 * 
 * The events are created based on a .csv file (indicated by the <code>CSV_FILE</code> constant) which can be produced
 * by running {@link GenerateClassScheduleCSV} first, then importing the data into a spreadsheet, completing the class
 * description, adding a 6th column with the class room number (5th column left blank) and, finally, exporting the data
 * back to CSV format.
 * 
 * Moreover, the script uses the same <code>PROPERTIES_FILE</code> as {@link GenerateClassScheduleCSV}, with some extra
 * properties added:
 * 
 * <ul>
 * <li>starttime: comma-separated list of (rounded) hours in which classes start each day (refer to days property);</li>
 * <li>acronym: acronym for this course, appended before each calendar event name;</li>
 * <li>calendar.name: name of the calendar that will be created to hold the events representing the classes;</li>
 * <li>calendar.description: description of the aforementioned calendar;</li>
 * <li>calendar.location: location (city, state, country) of the aforementioned calendar;</li>
 * <li>address: faculty/university name and address, to be appended to the class room name of each class.</li>
 * </ul>
 * 
 * Finally, to be able to connect to your Google Calendar this class uses OAuth. To make it work, you need to:
 * 
 * <ol>
 * <li>Open your browser and log in your Google account;</li>
 * <li>Visit https://code.google.com/apis/console/?api=calendar, join the developer program;</li>
 * <li>Open the Credentials section and download client secrets in JSON format;</li>
 * <li>Move the file to the root of the project (the program looks for it in the current working directory) with the
 * name client_secrets.json;</li>
 * <li>When you run the program for the first time your web browser will open and ask you to give permission for
 * CreateGoogleAppointmentsForClass to use your Google Calendar. Authorize it and you're set.</li>
 * </ol>
 * 
 * The steps above should be done only once and repeated in case your OAuth authorization is revoked or expires.
 * 
 * Parts of the code from this class has been copied/adapted from:
 * https://code.google.com/p/google-api-java-client/source/browse/calendar-cmdline-sample/src
 * /main/java/com/google/api/services/samples/calendar/cmdline/CalendarSample.java?repo=samples. (Licensed under the
 * Apache License, Version 2.0)
 * 
 * DEPENDS ON: google-api-client-1.18.0-rc.jar; google-api-services-calendar-v3-rev117-1.19.1.jar;
 * google-http-client-1.18.0-rc.jar; google-http-client-jackson2-1.18.0-rc.jar; google-oauth-client-1.18.0-rc.jar;
 * google-oauth-client-java6-1.18.0-rc.jar; google-oauth-client-jetty-1.18.0-rc.jar; jackcess-2.0.2.jar;
 * jackson-core-2.1.3.jar; jakarta-regexp-1.5.jar; javax.servlet-api-3.0.1.jar; jetty-6.1.26.jar; jetty-util-6.1.26.jar
 *
 * @author Vítor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class CreateGoogleAppointmentsForClass {
	private static final TimeZone MY_TIME_ZONE = TimeZone.getTimeZone("BRT");

	private static final String PROPERTIES_FILE = "lp-2015-2.properties";

	private static final String CSV_FILE = "lp-2015-2.csv";

	private static final String CSV_DELIMITER = "\\s*;\\s*";

	/**
	 * Be sure to specify the name of your application. If the application name is {@code null} or blank, the application
	 * will log a warning. Suggested format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "CreateGoogleAppointmentsForClass";

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport httpTransport;

	/**
	 * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single globally shared instance
	 * across your application.
	 */
	private static FileDataStoreFactory dataStoreFactory;

	/** Directory to store user credentials. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/cgafc_temp");

	private static com.google.api.services.calendar.Calendar client;

	/** Main method: the script. */
	public static void main(String[] args) throws Exception {
		// Loads the properties file.
		Properties props = new Properties();
		props.load(new FileReader(new File(PROPERTIES_FILE)));

		// Authorize the use of Google Calendar.
		httpTransport = GoogleNetHttpTransport.newTrustedTransport(); // initialize the transport
		dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR); // initialize the data store factory
		Credential credential = authorize(); // authorization

		// set up global Calendar instance
		client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

		// Creates a new calendar.
		Calendar entry = new Calendar();
		entry.setSummary(props.getProperty("calendar.name"));
		entry.setDescription(props.getProperty("calendar.description"));
		entry.setLocation(props.getProperty("calendar.location"));
		Calendar googleCalendar = client.calendars().insert(entry).execute();
		System.out.println("Created a new Google Calendar with ID: " + googleCalendar.getId());

		// Creates maps with days and expected class start and amount of hours.
		Map<Integer, Integer> startTimeMap = new HashMap<>();
		Map<Integer, Integer> chMap = new HashMap<>();
		try (Scanner daysScanner = new Scanner(props.getProperty("days")); Scanner startScanner = new Scanner(props.getProperty("starttime")); Scanner chScanner = new Scanner(props.getProperty("ch"))) {
			daysScanner.useDelimiter("\\s*,\\s*");
			startScanner.useDelimiter("\\s*,\\s*");
			chScanner.useDelimiter("\\s*,\\s*");
			while (daysScanner.hasNext() && startScanner.hasNext() && chScanner.hasNext()) {
				Integer day = Integer.parseInt(daysScanner.next());
				startTimeMap.put(day, Integer.parseInt(startScanner.next()));
				chMap.put(day, Integer.parseInt(chScanner.next()));
			}
		}

		// Prepares a calendar and a date formatter. Also gets the no-class name, course acronym and location.
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		DateFormat df = new SimpleDateFormat(props.getProperty("dateformat"));
		String noClassName = props.getProperty("noclassname");
		String acronym = props.getProperty("acronym");
		String address = props.getProperty("address");

		// Reads the CSV file line by line.
		try (Scanner scanner = new Scanner(new File(CSV_FILE))) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				// Breaks the line using the CSV delimiter.
				String[] columns = line.split(CSV_DELIMITER);

				// Sets the calendar to the start time of the appointment.
				calendar.setTime(df.parse(columns[0]));
				int weekday = calendar.get(java.util.Calendar.DAY_OF_WEEK);
				Integer startTime = startTimeMap.get(weekday);
				if (startTime != null) calendar.set(java.util.Calendar.HOUR_OF_DAY, startTime);

				// Creates the appointment for classes (class hours is filled with a number) and no-class reserved slots.
				Integer ch = chMap.get(weekday);
				if ((ch != null && ch.toString().equals(columns[3])) || columns[1].equals(noClassName)) createEvent(googleCalendar, calendar, acronym, columns[1], ch, columns[5] + ", " + address);
			}
		}

		System.out.println("Done!");
	}

	private static void createEvent(Calendar googleCalendar, java.util.Calendar calendar, String acronym, String description, Integer ch, String location) throws Exception {
		System.out.printf("Creating appointment at %tF %<tR for \"%s - %s\"%n", calendar.getTime(), acronym, description);

		// Creates the event using the course acronym and class description as summary.
		Event event = new Event();
		event.setSummary(acronym + " - " + description);
		event.setLocation(location);

		// Sets the start date/time for the event.
		Date startDate = calendar.getTime();
		DateTime startDateTime = new DateTime(startDate, MY_TIME_ZONE);
		event.setStart(new EventDateTime().setDateTime(startDateTime));

		// Adds the class hours to the start time and sets the end time for the class.
		calendar.add(java.util.Calendar.HOUR_OF_DAY, ch);
		DateTime endDateTime = new DateTime(calendar.getTime(), MY_TIME_ZONE);
		event.setEnd(new EventDateTime().setDateTime(endDateTime));

		// Stores the event in Google Calendar.
		client.events().insert(googleCalendar.getId(), event).execute();
	}

	/** Authorizes the installed application to access user's protected data. */
	private static Credential authorize() throws Exception {
		// load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(new FileInputStream(new File("client_secrets.json"))));
		if (clientSecrets.getDetails().getClientId().startsWith("Enter") || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar into client_secrets.json");
			System.exit(1);
		}

		// set up authorization code flow
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory).build();

		// authorize
		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}
}
