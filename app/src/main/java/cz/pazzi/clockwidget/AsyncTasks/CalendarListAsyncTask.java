package cz.pazzi.clockwidget.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.pazzi.clockwidget.Interfaces.ICalendarListWatcher;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.Providers.GoogleProvider;

/**
 * Created by pavel on 03.11.15.
 */
public class CalendarListAsyncTask extends AsyncTask<Void, Void, List<GCalendar>> {

    ICalendarListWatcher watcher;
    Context context;

    public CalendarListAsyncTask(Context context, ICalendarListWatcher watcher) {
        this.context = context;
        this.watcher = watcher;
    }

    @Override
    protected List<GCalendar> doInBackground(Void... params) {
        return DownloadCalendars();
    }

    @Override
    protected void onPostExecute(List<GCalendar> result) {
        if(result != null) {
            watcher.OnCalendarsDownloaded(result);
        } else {
            watcher.OnCalendarsError("calendar list is empty");
        }
    }

    public static List<GCalendar> DownloadCalendars() {
        GoogleProvider gProvider =  GoogleProvider.getInstance();
        List<GCalendar> calendars = new ArrayList<>();
        String pageToken = null;

        Calendar calendarService = gProvider.GetServiceCalendar();

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
}
