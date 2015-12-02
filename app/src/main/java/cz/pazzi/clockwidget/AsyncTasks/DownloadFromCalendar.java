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
        Log.d(getClass().getName(), "doInBackground - start");

        List<GCalendar> calendars = DownloadCalendars();
        calendars = DownloadEvents(calendars, from, to);
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

    public static List<GCalendar> DownloadCalendars() {
        GoogleProvider gProvider =  GoogleProvider.getInstance();
        List<GCalendar> calendars = new ArrayList<>();
        String pageToken = null;

        com.google.api.services.calendar.Calendar calendarService = gProvider.GetServiceCalendar();

        do {
            try {
                CalendarList calendarList = calendarService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> calItems = calendarList.getItems();

                for (CalendarListEntry entry : calItems) {
                    calendars.add( new GCalendar(entry.getId(), entry.getSummary(), entry.getBackgroundColor(), entry.getForegroundColor() ));
                }
                pageToken = calendarList.getNextPageToken();
            } catch (IOException e) { }
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

                for (Event event : items) {
//                    eventList.add(newGEvent(event, calEntry));
                    calEntry.events.add(newGEvent(event, calEntry));
                }
            } catch (IOException e) { }
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
