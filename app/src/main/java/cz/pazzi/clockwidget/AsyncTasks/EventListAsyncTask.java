package cz.pazzi.clockwidget.AsyncTasks;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.pazzi.clockwidget.data.EventClass;
import cz.pazzi.clockwidget.data.GCalendar;

/**
 * Created by pavel on 04.11.15.
 */
public class EventListAsyncTask extends AsyncTask<Void, Void, List<EventClass>> {
    com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    List<GCalendar> calendars;
    DateTime from;
    DateTime to;
    public EventListAsyncTask(List<GCalendar> calendars) {
        this.calendars = calendars;

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
    protected List<EventClass> doInBackground(Void... params) {

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        List<EventClass> eventList = new ArrayList<>();
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
                    eventList.add(new EventClass(event.getSummary(), start, end));
                }
            } catch (IOException e) { }
        }
        return eventList;
    }
}
