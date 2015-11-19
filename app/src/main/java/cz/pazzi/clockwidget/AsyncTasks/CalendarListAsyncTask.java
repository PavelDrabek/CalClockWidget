package cz.pazzi.clockwidget.AsyncTasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.pazzi.clockwidget.GCalActivity;
import cz.pazzi.clockwidget.Interfaces.ICalendarListWatcher;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.data.GoogleProvider;

/**
 * Created by pavel on 03.11.15.
 */
public class CalendarListAsyncTask extends AsyncTask<Void, Void, List<GCalendar>> {

//    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };
//    private static final String PREF_ACCOUNT_NAME = "accountName";

//    com.google.api.services.calendar.Calendar mService;
//    GoogleAccountCredential credential;
//    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
//    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    ICalendarListWatcher watcher;
    Context context;
    GoogleProvider gProvider;

    public CalendarListAsyncTask(Context context, ICalendarListWatcher watcher) {
        this.context = context;
        this.watcher = watcher;
        this.gProvider = GoogleProvider.getInstance(context);
    }

    @Override
    protected List<GCalendar> doInBackground(Void... params) {
//        SharedPreferences settings = context.getSharedPreferences(GCalActivity.class.getName(), Context.MODE_PRIVATE);
//        credential = GoogleAccountCredential.usingOAuth2(
//                context, Arrays.asList(SCOPES))
//                .setBackOff(new ExponentialBackOff())
//                .setSelectedAccountName("pazzicz@gmail.com");
////                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
//
//        mService = new com.google.api.services.calendar.Calendar.Builder(
//                transport, jsonFactory, credential)
//                .setApplicationName("Google Calendar API Android Quickstart")
//                .build();

        List<GCalendar> calendars = new ArrayList<>();
        String pageToken = null;

        while (gProvider.accountName == null) {
            synchronized(this) {
                try {
                    wait(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Calendar calendarService = GoogleProvider.GetServiceCalendar(context);
        if(calendarService == null) {
            watcher.OnCalendarsError("cannot get calendar service");
            return null;
        }

        do {
            try {
//                CalendarList calendarList = gProvider.mService.calendarList().list().setPageToken(pageToken).execute();
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

    @Override
    protected void onPostExecute(List<GCalendar> result) {
        if(result != null) {
            watcher.OnCalendarsDownloaded(result);
        }
    }
}
