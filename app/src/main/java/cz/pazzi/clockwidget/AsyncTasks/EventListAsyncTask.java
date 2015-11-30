package cz.pazzi.clockwidget.AsyncTasks;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import cz.pazzi.clockwidget.Interfaces.ICalendarListWatcher;
import cz.pazzi.clockwidget.Interfaces.IEventListWatcher;
import cz.pazzi.clockwidget.data.GEvent;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.data.GoogleProvider;

/**
 * Created by pavel on 04.11.15.
 */
public class EventListAsyncTask extends AsyncTask<Void, Void, List<GEvent>> {
    com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    IEventListWatcher watcher;
    List<GCalendar> calendars;
    Context context;
    int[] widgetIds;
    DateTime from;
    DateTime to;
    public EventListAsyncTask(Context context, IEventListWatcher watcher, int[] widgetIds) {
        this.watcher = watcher;
        this.widgetIds = widgetIds;
        this.context = context;

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

//        mService = new com.google.api.services.calendar.Calendar.Builder(
//                transport, jsonFactory, credential)
//                .setApplicationName("Google Calendar API Android Quickstart")
//                .build();

        mService = GoogleProvider.GetServiceCalendar(context);
        Log.d("asyncEvents", "doInBackground - 1");

        calendars = CalendarListAsyncTask.DownloadCalendars(context);;
        Log.d("asyncEvents", "doInBackground - 2");
        Log.d("asyncEvents", "doInBackground - calendars count " + calendars.size());

        List<GEvent> eventList = new ArrayList<>();
        for (GCalendar calEntry : calendars) {
//            eventStrings.add(calEntry.getSummary());
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
                    DateTime start = event.getStart().getDateTime();
                    DateTime end = event.getEnd().getDateTime();
                    if (start == null) {
                        // All-day events don't have start times, so just use
                        // the start date.
                        start = event.getStart().getDate();
                    }
//                eventStrings.add(String.format("%s (%s) - %s", event.getSummary(), start, event.getCreator().getDisplayName()));
                    Calendar startEvent = new GregorianCalendar();
                    Calendar endEvent = new GregorianCalendar();
                    startEvent.setTime(new Date(start.getValue()));
                    endEvent.setTime(new Date(end.getValue()));

//                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd H:m:s");
//                    Log.d("gevent", "start = " + format1.format(startEvent.getTime()) + ", end = " + format1.format(endEvent.getTime()));

                    GEvent nEvent = new GEvent(event.getSummary(), startEvent, endEvent);
                    nEvent.SetBackgroundColor(calEntry.backgroundColor);
                    nEvent.SetForegroundColor(calEntry.foregroundColor);

                    eventList.add(nEvent);
                }

            } catch (IOException e) { }
        }

        return eventList;
    }

    @Override
    protected void onPostExecute(List<GEvent> result) {
        if(result != null) {
            watcher.OnEventsDownloaded(result, context, widgetIds);
        } else {
            watcher.OnEventsError("event list is empty");
        }
    }

}
