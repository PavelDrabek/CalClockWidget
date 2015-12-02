package cz.pazzi.clockwidget.Providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.pazzi.clockwidget.Activities.WidgetPreference;
import cz.pazzi.clockwidget.R;
import cz.pazzi.clockwidget.Services.WidgetService;
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

        context.startService(new Intent(context, WidgetService.class));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d("WidgetProvider", "updating");

        //TODO: if service not running, start service
        //TODO: get only widget calendars (WidgetPreference or SharedPreference?)

        List<GEvent> events = new ArrayList<>();
        for (int widgetId : appWidgetIds) {
//            events = GoogleProvider.getInstance().GetEvents(calendarIds);
            events = GoogleProvider.getInstance().GetEvents();
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setOnClickPendingIntent(R.id.imgSettings, getPreferenceIntent(context, widgetId, ONCLICK_CLOCK));
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

        Bitmap bitmap = GetTimelineBitmap(events);

        DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int boundingX = Math.round(480 * metrics.density);
        int boundingY = Math.round(40f * metrics.density);
        float xScale = ((float) boundingX) / width;
        float yScale = ((float) boundingY) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix , false);

        BitmapDrawable bd = new BitmapDrawable(context.getResources(), scaledBitmap);
        bd.setAntiAlias(false);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setImageViewBitmap(R.id.imgTimeline, bd.getBitmap());
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews);
    }

    protected PendingIntent getPreferenceIntent(Context context, int widgetId, String action) {
        Intent intent = new Intent(context, WidgetPreference.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    Bitmap GetTimelineBitmap(List<GEvent> events) {
        int bitmapSize = 240;
        Bitmap bitmap = Bitmap.createBitmap(bitmapSize, 1, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);

        float density = bitmapSize / (float)(24 * 60);
        if(events != null) {
            for (GEvent e : events) {
                AddEventToBitmap(bitmap, e, density);
            }
        }
        return bitmap;
    }

    void AddEventToBitmap(Bitmap bitmap, GEvent event, float densityPerMinute) {
        int pixelCount = (int)(densityPerMinute * event.DurationInMinutes());
        int pixelOffset = (int)(densityPerMinute * event.StartAtMinutes());
        Log.d("addEventToBitmap", "event duration = " + event.DurationInMinutes());
        Log.d("addEventToBitmap", "density per minute = " + densityPerMinute);
        Log.d("addEventToBitmap", "pixel count = " + pixelCount);
        Log.d("addEventToBitmap", "pixel offset = " + pixelOffset);
        Log.d("addEventToBitmap", "event color = " + event.backgroundColor);

        for(int i = pixelOffset; i < pixelOffset + pixelCount && i < bitmap.getWidth(); i++) {
            bitmap.setPixel(i, 0, event.backgroundColor);
        }
    }
}
