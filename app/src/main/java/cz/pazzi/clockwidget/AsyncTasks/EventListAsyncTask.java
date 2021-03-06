package cz.pazzi.clockwidget.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import cz.pazzi.clockwidget.Interfaces.IEventListWatcher;
import cz.pazzi.clockwidget.data.GEvent;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.Providers.GoogleProvider;

/**
 * Created by pavel on 04.11.15.
 */
public class EventListAsyncTask extends AsyncTask<Void, Void, List<GEvent>> {
    IEventListWatcher watcher;
    DateTime from;
    DateTime to;

    public EventListAsyncTask(IEventListWatcher watcher) {
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
    protected List<GEvent> doInBackground(Void... params) {
        Log.d("asyncEvents", "doInBackground - start");
        return DownloadEvents(from, to);
    }

    @Override
    protected void onPostExecute(List<GEvent> result) {
        if(result != null) {
            watcher.OnEventsDownloaded(result);
        } else {
            watcher.OnEventsError("event list is empty");
        }
    }

    public static List<GEvent> DownloadEvents(DateTime from, DateTime to) {
        com.google.api.services.calendar.Calendar mService = GoogleProvider.getInstance().GetServiceCalendar();
        List<GCalendar> calendars = CalendarListAsyncTask.DownloadCalendars();;
        Log.d("asyncEvents", "doInBackground - 2");
        Log.d("asyncEvents", "doInBackground - calendars count " + calendars.size());

        List<GEvent> eventList = new ArrayList<>();
        for (GCalendar calEntry : calendars) {
            try {
                Events events = mService.events().list(calEntry.id)
                        .setMaxResults(10)
                        .setTimeMin(from)
                        .setTimeMax(to)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events.getItems();

                for (Event event : items) {
                    eventList.add(newGEvent(event, calEntry));
                }
            } catch (IOException e) { }
        }

        return eventList;
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
