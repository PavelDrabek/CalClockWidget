package cz.pazzi.clockwidget.data;

/**
 * Created by pavel on 03.11.15.
 */
public class GCalendar {
    public String id;
    public String summary;
    public String backgroundColor;
    public String foregroundColor;

    public GCalendar(String id, String summary, String backgroundColor, String foregroundColor) {
        this.id = id;
        this.summary = summary;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }
}
