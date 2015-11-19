package cz.pazzi.clockwidget.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

import cz.pazzi.clockwidget.Activities.GoogleProviderActivity;
import cz.pazzi.clockwidget.R;

/**
 * Created by pavel on 18.11.15.
 */
public class GoogleProvider {
    /**
     * A Google Calendar API service object used to access the API.
     * Note: Do not confuse this class with API library's model classes, which
     * represent specific data structures.
     */
    public Calendar mService;
    private static GoogleAccountCredential mCredential;
    public static GoogleAccountCredential credential;

    private final static HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private final static JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private final static String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    public String accountName = null;

    private static GoogleProvider instance;
    public static GoogleProvider getInstance(Context context) {
        getInstaceWithoutCallActivity(context);

        if(instance.accountName == null) {
            ShowPickupAccountActivity(context);
        }
        return instance;
    }

    public static GoogleProvider getInstaceWithoutCallActivity(Context context) {
        if(instance == null) {
            instance = new GoogleProvider(context);
        }

        return instance;
    }

    private GoogleProvider(Context context) {
        accountName = GetAccountName(context);
    }

    private static void SetAccountName(Context context, String accountName) {
        Resources resources = context.getResources();
        SharedPreferences settings = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(resources.getString(R.string.preference_account_name), accountName);
        editor.commit();
    }

    private static String GetAccountName(Context context) {
        SetAccountName(context, null);

        Resources resources = context.getResources();
        SharedPreferences settings = context.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return settings.getString(resources.getString(R.string.preference_account_name), null);
    }

    private static void ShowPickupAccountActivity(Context context) {
        Intent intentShowG = new Intent(context, GoogleProviderActivity.class);
        context.startActivity(intentShowG);
    }

    public static Calendar GetServiceCalendar(Context context) {
        String accountName = GetAccountName(context);
        if(mCredential == null) {
            mCredential = GoogleAccountCredential.usingOAuth2(
                    context, Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff())
                    .setSelectedAccountName(accountName);
        }

        if(accountName == null) {
            ShowPickupAccountActivity(context);
            return null;
        }

        Calendar mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();

        return mService;
    }
}
