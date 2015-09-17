package cz.pazzi.clockwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Pazzi on 16.9.2015.
 */
public class WidgetProvider1 extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        MyClock custom_clock = new MyClock(context);
        custom_clock.measure(100,20);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Toast.makeText(context, "on update", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Toast.makeText(context, "on receive", Toast.LENGTH_LONG).show();
    }
}
