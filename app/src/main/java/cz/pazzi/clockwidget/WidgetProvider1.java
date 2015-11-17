package cz.pazzi.clockwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.GridView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

import cz.pazzi.clockwidget.Activities.WidgetPreference;
import cz.pazzi.clockwidget.Services.WidgetService;

/**
 * Created by Pazzi on 16.9.2015.
 */
public class WidgetProvider1 extends AppWidgetProvider {

    private GridView gridView;

    private static final String ONCLICK_CLOCK = "clickOnClock";

    public WidgetProvider1() {
        super();
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d("WidgetProvider", "onEnabled");

        context.startService(new Intent(context, WidgetService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("WidgetProvider", "updating");
//        Toast.makeText(context, "onUpdate clock " + Arrays.toString(appWidgetIds), Toast.LENGTH_SHORT).show();

        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            Toast.makeText(context, "onUpdate widget id is: " + widgetId, Toast.LENGTH_SHORT).show();
            remoteViews.setOnClickPendingIntent(R.id.imgSettings, getPendingSelfIntent(context, widgetId, ONCLICK_CLOCK));
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        UpdateClock(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WidgetProvider", "onReceive: " + intent.getAction());

        if(ONCLICK_CLOCK.equals(intent.getAction())) {
            int rnd = new Random().nextInt(100);
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
//            Toast.makeText(context, "onclick clock " + widgetId + " " + rnd, Toast.LENGTH_SHORT).show();

//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//            remoteViews.setTextViewText(R.id.txtRandom, String.valueOf(rnd));
//            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
        }
        super.onReceive(context, intent);
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

//        return date.format(cal.getTime());
    }

    private void UpdateClock(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        remoteViews.setTextViewText(R.id.text_date, GetDateString());
        remoteViews.setTextViewText(R.id.text_time, GetTimeString());

        ComponentName widgetComponent = new ComponentName(context, WidgetProvider1.class);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetComponent, remoteViews);
    }

    protected PendingIntent getPendingSelfIntent(Context context, int widgetId, String action) {
        Intent intent = new Intent(context, WidgetPreference.class);
//        Intent intent = new Intent(context, WidgetProvider1.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        Log.d("WidgetProvider", "setting intent id = " + widgetId);
//        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
