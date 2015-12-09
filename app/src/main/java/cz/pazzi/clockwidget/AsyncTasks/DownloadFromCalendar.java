package cz.pazzi.clockwidget.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import cz.pazzi.clockwidget.Interfaces.ICalendarListWatcher;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.data.GEvent;
import cz.pazzi.clockwidget.Providers.GoogleProvider;

/**
 * Created by pavel on 02.12.15.
 */
public class DownloadFromCalendar extends AsyncTask<Void, Void, List<GCalendar>> {
    ICalendarListWatcher watcher;
    DateTime from;
    DateTime to;

    public DownloadFromCalendar(ICalendarListWatcher watcher) {
        this.watcher = watcher;

        java.util.Calendar dateCalendar = java.util.Calendar.getInstance();
        dateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        dateCalendar.clear(java.util.Calendar.MINUTE);
        dateCalendar.clear(java.util.Calendar.SECOND);
        dateCalendar.clear(java.util.Calendar.MILLISECOND);
        from = new DateTime(dateCalendar.getTime());

        dateCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        to = new DateTime(dateCalendar.getTime());
    }

    @Override
    protected List<GCalendar> doInBackground(Void... params) {
        Log.d(getClass().getName(), "Downloading events...");

        List<GCalendar> calendars = DownloadCalendars();
        Log.d(getClass().getName(), "Calendars downloaded " + calendars.size());
        calendars = DownloadEvents(calendars, from, to);

        int eventCount = 0;
        for (GCalendar c: calendars) {
            eventCount += c.events.size();
        }

        Log.d(getClass().getName(), "Events downloaded " + eventCount);
        return  calendars;
    }

    @Override
    protected void onPostExecute(List<GCalendar> result) {
        if(result != null) {
            watcher.OnCalendarsDownloaded(result);
        } else {
            watcher.OnCalendarsError("event list is empty");
        }
    }

    //TODO: after 5 minutes get error: "java.net.ConnectException: failed to connect to www.googleapis.com/216.58.213.10 (port 443) after 20000ms: isConnected failed: ECONNREFUSED (Connection refused)"

    public static List<GCalendar> DownloadCalendars() {
        GoogleProvider gProvider =  GoogleProvider.getInstance();
        List<GCalendar> calendars = new ArrayList<>();
        String pageToken = null;

        com.google.api.services.calendar.Calendar calendarService = gProvider.GetServiceCalendar();

        do {
            try {
                CalendarList calendarList = calendarService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> calItems = calendarList.getItems();

                if(calItems.size() <= 0) {
                    Log.w("DownloadFromCalendar", "warning downloaded calendars list is empty");
                }

                for (CalendarListEntry entry : calItems) {
                    calendars.add( new GCalendar(entry.getId(), entry.getSummary(), entry.getBackgroundColor(), entry.getForegroundColor() ));
                }
                pageToken = calendarList.getNextPageToken();
            } catch (IOException e) {
                Log.e("DownloadFromCalendar", "error during downloading calendars from google: " + e.toString());
            }
        } while (pageToken != null);

        return calendars;
    }

    public static List<GCalendar> DownloadEvents(List<GCalendar> calendars, DateTime from, DateTime to) {
        com.google.api.services.calendar.Calendar mService = GoogleProvider.getInstance().GetServiceCalendar();

//        List<GEvent> eventList = new ArrayList<>();
        for (GCalendar calEntry : calendars) {
            try {
                Events events = mService.events().list(calEntry.id)
                        .setMaxResults(30)
                        .setTimeMin(from)
                        .setTimeMax(to)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events.getItems();
                if(items.size() <= 0) {
                    Log.w("DownloadFromCalendar", "warning downloaded event list is empty");
                }

                for (Event event : items) {
//                    eventList.add(newGEvent(event, calEntry));
                    calEntry.events.add(newGEvent(event, calEntry));
                }



            } catch (IOException e) {
                Log.e("DownloadFromCalendar", "error during downloading events from google: " + e.toString());
            }
        }

        return calendars;
    }

    private static GEvent newGEvent(Event event, GCalendar calendar) {
        DateTime start = event.getStart().getDateTime();
        DateTime end = event.getEnd().getDateTime();
        if (start == null) {
            // All-day events don't have start times, so just use
            // the start date.
            start = event.getStart().getDate();
        }
        if(end == null) {
            end = event.getEnd().getDate();
        }
        Calendar startEvent = new GregorianCalendar();
        Calendar endEvent = new GregorianCalendar();
        startEvent.setTime(new Date(start.getValue()));
        endEvent.setTime(new Date(end.getValue()));

//        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd H:m:s");
//        Log.d("gevent", "start = " + format1.format(startEvent.getTime()) + ", end = " + format1.format(endEvent.getTime()));

        GEvent nEvent = new GEvent(event.getSummary(), startEvent, endEvent);
        nEvent.SetBackgroundColor(calendar.backgroundColor);
        nEvent.SetForegroundColor(calendar.foregroundColor);

        return nEvent;
    }
}
