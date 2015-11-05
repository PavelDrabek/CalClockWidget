package cz.pazzi.clockwidget.AsyncTasks;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.pazzi.clockwidget.Interfaces.ICalendarList;
import cz.pazzi.clockwidget.data.GCalendar;

/**
 * Created by pavel on 03.11.15.
 */
public class CalendarListAsyncTask extends AsyncTask<Void, Void, List<GCalendar>> {

    com.google.api.services.calendar.Calendar mService;
    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    ICalendarList watcher;

    public CalendarListAsyncTask(ICalendarList watcher) {
        this.watcher = watcher;
    }

    @Override
    protected List<GCalendar> doInBackground(Void... params) {

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        List<GCalendar> calendars = new ArrayList<>();
        String pageToken = null;
        do {
            try {
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> calItems = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : calItems) {
                    calendars.add(new GCalendar(calendarListEntry.getId()));
                }
                pageToken = calendarList.getNextPageToken();
            } catch (IOException e) { }
        } while (pageToken != null);

        return calendars;
    }

    @Override
    protected void onPostExecute(List<GCalendar> result) {
        watcher.OnCalendarsDownloaded(result);
    }
}
