package google;

import java.net.URL;

import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.calendar.CalendarEntry;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.calendar.CalendarFeed;
import com.google.gdata.data.extensions.BaseEventEntry.Visibility;

/**
 * Using the Google API, marks all events in a given calendar as private.
 * 
 * DEPENDS ON: gdata-calendar-2.0.jar, gdata-client-1.0.jar,gdata-core-1.0.jar, gdata-core-1.0.jar
 * 
 * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
 * @version 1.0
 */
public class MakeAllCalendarEventsPrivate {
	private static final String GOOGLE_USERNAME = "your-username@gmail.com";

	private static final String GOOGLE_PASSWORD = "your-password";
	
	private static final String METAFEED_URL_BASE = "https://www.google.com/calendar/feeds/";
	
	private static final String OWNCALENDARS_FEED_URL_SUFFIX = "/owncalendars/full";
	
	private static final String EVENT_FEED_URL_SUFFIX = "/private/full";

	public static void main(String[] args) throws Exception {
		// Creates a new calendar service.
		CalendarService myService = new CalendarService("makeAllCalendarEventsPrivate");

		// Sets the user credentials. User name and password are defined in the class constants.
		myService.setUserCredentials(GOOGLE_USERNAME, GOOGLE_PASSWORD);
		
		// Obtains all calendars owned by the user.
		URL calendarsUrl = new URL(METAFEED_URL_BASE + GOOGLE_USERNAME + OWNCALENDARS_FEED_URL_SUFFIX);
		CalendarFeed calendarsFeed = myService.getFeed(calendarsUrl, CalendarFeed.class);
		
		// Goes through all calendars.
		int j;
		for (j = 0; j < calendarsFeed.getEntries().size(); j++) {
			// Obtains the calendar.
			CalendarEntry calendar = calendarsFeed.getEntries().get(j);
			System.out.println("\n\nProcessing calendar: " + calendar.getTitle().getPlainText());
			
			// Obtains the calendar id.
			String calendarId = calendar.getId();
			
			// Build the calendar URL from the calendar id.
			int idx1 = calendarId.indexOf("feeds/") + 6;
			int idx2 = calendarId.indexOf("calendars/") + 10;
			String calendarUrl = calendarId.substring(0, idx1) + calendarId.substring(idx2) + EVENT_FEED_URL_SUFFIX;
			
			// Obtains the list of all events in the given calendar.
			URL feedUrl = new URL(calendarUrl);
			CalendarEventFeed feed = myService.getFeed(feedUrl, CalendarEventFeed.class);
	
			// Goes through all events.
			int i;
			for (i = 0; i < feed.getEntries().size(); i++) {
				// Obtains the entry.
				CalendarEventEntry entry = feed.getEntries().get(i);
				System.out.println("\t* " + entry.getId());
	
				// Checks if the entry has visibility set. Otherwise, create it.
				Visibility visibility = entry.getVisibility();
				if (visibility == null) {
					visibility = new Visibility(Visibility.DEFAULT_VALUE);
					entry.setVisibility(visibility);
				}
				
				// If the visibility is default, change to private. If public, leave it.
				if (Visibility.DEFAULT.equals(visibility)) visibility.setValue(Visibility.PRIVATE_VALUE);
				
				// FIXME: this works only for the default calendar. Once it starts processing a different calendar, it produces an exception! 
				entry.update();
	
				// Print some information in the output.
				System.out.println("\t- " + entry.getTitle().getPlainText() + " -- Visibility is now: " + entry.getVisibility().getValue());
			}
			
			System.out.println("\nFinished calendar, processed " + i + " events.");
		}

		// Log the end of the program.
		System.out.println("\nDone! Processed " + j + " calendars.");
	}
}
