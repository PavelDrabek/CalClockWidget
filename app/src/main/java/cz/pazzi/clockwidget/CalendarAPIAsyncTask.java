package cz.pazzi.clockwidget;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.pazzi.clockwidget.data.EventClass;

/**
 * Created by pavel on 02.10.15.
 */
public class CalendarAPIAsyncTask extends AsyncTask<Void, Void, Void> {
    private GCalActivity mActivity;

    CalendarAPIAsyncTask(GCalActivity activity) {
        this.mActivity = activity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            getDataFromApi(); // TODO:

        } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
            mActivity.showGooglePlayServicesAvailabilityErrorDialog(
                    availabilityException.getConnectionStatusCode());

        } catch (UserRecoverableAuthIOException userRecoverableException) {
            mActivity.startActivityForResult(
                    userRecoverableException.getIntent(),
                    GCalActivity.REQUEST_AUTHORIZATION);

        } catch (Exception e) {
            mActivity.updateStatus("The following error occurred:\n" +
                    e.getMessage());
        }
        if (mActivity.mProgress.isShowing()) {
            mActivity.mProgress.dismiss();
        }
        return null;
    }

    private List<EventClass> getDataFromApi() throws IOException {
        // List the next 10 events from the primary calendar.
        java.util.Calendar dateCalendar = java.util.Calendar.getInstance();
        dateCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        dateCalendar.clear(java.util.Calendar.MINUTE);
        dateCalendar.clear(java.util.Calendar.SECOND);
        dateCalendar.clear(java.util.Calendar.MILLISECOND);

        DateTime today = new DateTime(dateCalendar.getTime());
        dateCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        DateTime tomorrow = new DateTime(dateCalendar.getTime());

        DateTime now = new DateTime(System.currentTimeMillis());
        List<EventClass> dataEvents = new ArrayList<EventClass>();

        String pageToken = null;
        int count = 0;
        do {
            CalendarList calendarList = mActivity.mService.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> calItems = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : calItems) {

                Events events = mActivity.mService.events().list(calendarListEntry.getId())
//                        .setMaxResults(10)
                        .setTimeMin(today)
                        .setTimeMax(tomorrow)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events.getItems();


                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        // All-day events don't have start times, so just use
                        // the start date.
                        start = event.getStart().getDate();
                    }

                    EventClass nEvent = new EventClass();
                    nEvent.name = event.getSummary();
                    nEvent.start = start;
                    nEvent.end = event.getEnd().getDateTime();
                    nEvent.SetBackgroundColor(calendarListEntry.getBackgroundColor());
                    nEvent.SetForegroundColor(calendarListEntry.getForegroundColor());

                    dataEvents.add(nEvent);
                }
            }

            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return dataEvents;
    }

}
