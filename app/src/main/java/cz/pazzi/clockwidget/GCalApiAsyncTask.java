package cz.pazzi.clockwidget;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.model.Calendar;

import java.io.IOException;
import java.util.*;

/**
 * An asynchronous task that handles the Google Calendar API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class GCalApiAsyncTask extends AsyncTask<Void, Void, Void> {
    private GCalActivity mActivity;

    /**
     * Constructor.
     * @param activity MainActivity that spawned this task.
     */
    GCalApiAsyncTask(GCalActivity activity) {
        this.mActivity = activity;
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            mActivity.clearResultsText();
            mActivity.updateResultsText(getDataFromApi());

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

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {
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
        List<String> eventStrings = new ArrayList<String>();

        String pageToken = null;
        int count = 0;
        do {
            eventStrings.add(String.valueOf(++count));
            CalendarList calendarList = mActivity.mService.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> calItems = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : calItems) {
                eventStrings.add(calendarListEntry.getSummary());

                Events events = mActivity.mService.events().list(calendarListEntry.getId())
                        .setMaxResults(10)
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
                    eventStrings.add(
                            String.format("%s (%s) - %s", event.getSummary(), start, event.getCreator().getDisplayName()));
                }
            }

            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return eventStrings;
    }

}