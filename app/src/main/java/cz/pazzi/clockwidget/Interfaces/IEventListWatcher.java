package cz.pazzi.clockwidget.Interfaces;

import android.content.Context;

import java.util.List;

import cz.pazzi.clockwidget.data.GEvent;

/**
 * Created by pavel on 27.11.15.
 */
public interface IEventListWatcher {
    void OnEventsDownloaded(List<GEvent> events);
    void OnEventsError(String error);
}
