package cz.pazzi.clockwidget.Providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.pazzi.clockwidget.Activities.WidgetPreference;
import cz.pazzi.clockwidget.R;
import cz.pazzi.clockwidget.Services.WidgetService;
import cz.pazzi.clockwidget.data.GCalendar;
import cz.pazzi.clockwidget.data.GEvent;

/**
 * Created by Pazzi on 16.9.2015.
 */
public class WidgetProvider1 extends AppWidgetProvider {

    private static final String ONCLICK_CLOCK = "clickOnClock";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WidgetProvider", "onEnabled");

        WidgetService.StartService(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("WidgetProvider", "updating");

        //TODO: if service not running, start service
        //TODO: bitmap downloading...
        //TODO: bitmap no events today

        if(WidgetService.instance == null) {
            Toast.makeText(context, "service is not running", Toast.LENGTH_SHORT).show();
        }

        List<GEvent> events = null;
        for (int widgetId : appWidgetIds) {
            events = GetEventsForWidget(context, widgetId);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setOnClickPendingIntent(R.id.imgSettings, getPreferenceIntent(context, widgetId, ONCLICK_CLOCK));
            remoteViews.setOnClickPendingIntent(R.id.part_clock, getClockIntent(context, widgetId, ONCLICK_CLOCK));
            remoteViews.setOnClickPendingIntent(R.id.part_timeline, getCalendarIntent(context, widgetId, ONCLICK_CLOCK));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
            UpdateTimeLine(context, widgetId, events);
        }

        UpdateClock(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WidgetProvider", "onReceive: " + intent.getAction());

        if(ONCLICK_CLOCK.equals(intent.getAction())) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
        }
        super.onReceive(context, intent);
    }

    private List<GEvent> GetEventsForWidget(Context context, int widgetId) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        List<GCalendar> allCals = GoogleProvider.getInstance().GetCalendars();
        List<GEvent> events = new ArrayList<>();

        String prefix = String.valueOf(widgetId) + "_";

        for (int i = 0; i < allCals.size(); i++) {
            if (pref.getBoolean(prefix + allCals.get(i).id, true)) {
                events.addAll(allCals.get(i).events);
            }
        }

        if(events.size() <= 0) {
            Log.w(getClass().getName(), "event list for widget " + widgetId + " is empty");
            if(allCals.size() <= 0) {
                Log.w(getClass().getName(), "calendar list for widget " + widgetId + " is empty");
            }
        }
        return events;
    }

    private String GetTimeString() {
        Calendar cal = Calendar.getInstance();
//        String str = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        String myString = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
        return myString;
    }

    private String GetDateString() {
        Calendar cal = Calendar.getInstance();
//        String str = String.format("%2d.%2d.%d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
//        SimpleDateFormat date = new SimpleDateFormat("EEE, MMMM d", Locale.getDefault());
        String myString = DateFormat.getDateInstance(DateFormat.LONG).format(cal.getTime());
        return myString;
    }

    private void UpdateClock(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        remoteViews.setTextViewText(R.id.text_date, GetDateString());
        remoteViews.setTextViewText(R.id.text_time, GetTimeString());

        ComponentName widgetComponent = new ComponentName(context, WidgetProvider1.class);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetComponent, remoteViews);
    }

    private void UpdateTimeLine(Context context, int widgetId, List<GEvent> events) {

        DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();

        Bitmap bitmap = BitmapOperations.GetTimelineBitmap(events);
        Bitmap scaledBitmap = BitmapOperations.ScaleBitmap(bitmap, metrics, 480, 40);
        scaledBitmap = BitmapOperations.DrawActualTime(scaledBitmap);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setImageViewBitmap(R.id.imgTimeline, scaledBitmap);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
    }

    protected PendingIntent getPreferenceIntent(Context context, int widgetId, String action) {
        Intent intent = new Intent(context, WidgetPreference.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected PendingIntent getClockIntent(Context context, int widgetId, String action) {
        Intent openClockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, openClockIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    protected PendingIntent getCalendarIntent(Context context, int widgetId, String action) {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
//        ContentUris.appendId(builder, 0);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
