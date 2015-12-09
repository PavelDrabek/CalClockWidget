package cz.pazzi.clockwidget.Providers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.pazzi.clockwidget.Activities.ChooseAccountActivity;
import cz.pazzi.clockwidget.AsyncTasks.DownloadFromCalendar;
import cz.pazzi.clockwidget.Interfaces.ICalendarListWatcher;
import cz.pazzi.clockwidget.R;
import cz.pazzi.clockwidget.Services.WidgetService;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.data.GEvent;

/**
 * Created by pavel on 18.11.15.
 */
public class GoogleProvider implements ICalendarListWatcher {

    private final static HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private final static JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private final static String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private Context context;
    private Calendar mService;
    private GoogleAccountCredential mCredential;

    private List<GCalendar> calendars;
    private List<GEvent> events;
    private List<ICalendarListWatcher> watchers;

    private boolean isInit = false;
    public boolean IsInit() { return isInit; }

    public boolean IsAccountSelected() { return (mCredential == null ? false : mCredential.getSelectedAccountName() != null); }

    private static GoogleProvider instance;
    public static GoogleProvider getInstance() {
        if(instance == null) {
            instance = new GoogleProvider();
        }
        return instance;
    }

    private GoogleProvider() {
        instance = this;
        isInit = false;
        watchers = new ArrayList<>();

        calendars = new ArrayList<>();
        events = new ArrayList<>();
    }

    public void Init(Context context) {
        this.context = context;

        mCredential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(GetAccountName());

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        if(!IsAccountSelected()) {
            ShowChooseAccount();
        }

        isInit = true;
    }

    public List<GCalendar> GetCalendars() {
        return calendars;
    }

    public List<GEvent> GetEvents() {
        return events;
    }

    public List<GEvent> GetEvents(List<String> calendarIds) {
        List<GEvent> result = new ArrayList<>();
        for(GCalendar c : calendars) {
            for(int i = 0; i < calendarIds.size(); i++) {
                if (c.id.equals(calendarIds.get(i))) {
                    result.addAll(c.events);
                }
            }
        }
        return result;
    }

    public void SetAccountName(String accountName) {
        Resources resources = context.getResources();
        SharedPreferences settings = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(resources.getString(R.string.preference_account_name), accountName);
        editor.commit();

        mCredential.setSelectedAccountName(accountName);
        DownloadCalendars();
    }

    private String GetAccountName() {
        Resources resources = context.getResources();
        SharedPreferences settings = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return settings.getString(resources.getString(R.string.preference_account_name), null);
    }

    public Calendar GetServiceCalendar() {
        return mService;

//        mCredential = GoogleAccountCredential.usingOAuth2(
//                context, Arrays.asList(SCOPES))
//                .setBackOff(new ExponentialBackOff())
//                .setSelectedAccountName(GetAccountName());
//
//        return new com.google.api.services.calendar.Calendar.Builder(
//                AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), mCredential)
//                .setApplicationName("Google Calendar API Android Quickstart")
//                .build();
    }

    public Intent NewChooseAccountIntent() {
        return mCredential.newChooseAccountIntent();
    }

    public void ShowChooseAccount() {
        Intent intent = new Intent(context, ChooseAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void DownloadCalendars() {
        if(IsAccountSelected()) {
            new DownloadFromCalendar(this).execute();
        } else {
            Log.w(getClass().getName(), "Cannot download calendars, because account is not selected!");
        }
    }

    public void AddListener(ICalendarListWatcher listener) {
        watchers.add(listener);
    }

    @Override
    public void OnCalendarsDownloaded(List<GCalendar> calendars) {
        this.calendars = calendars;

        if(events == null) {
            events = new ArrayList<>();
        } else {
            events.clear();
        }

        for(GCalendar c : calendars) {
            events.addAll(c.events);
        }

        for (ICalendarListWatcher w: watchers) {
            w.OnCalendarsDownloaded(calendars);
        }
    }

    @Override
    public void OnCalendarsError(String error) {
        for (ICalendarListWatcher w: watchers) {
            w.OnCalendarsError(error);
        }
    }

    private boolean isDeviceOnline() {
        Log.d(getClass().getName(), "isDeviceOnline");
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
