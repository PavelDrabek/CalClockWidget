package cz.pazzi.clockwidget.data;


import android.graphics.Color;

import com.google.api.client.util.DateTime;

/**
 * Created by pavel on 02.10.15.
 */
public class EventClass {
    public String name;
    public DateTime start;
    public DateTime end;

    public int backgroundColor;
    public int foregroundColor;

    public void SetBackgroundColor(String str) {
        backgroundColor = Color.parseColor(str);
    }
    public void SetForegroundColor(String str) {
        foregroundColor = Color.parseColor(str);
    }
}
