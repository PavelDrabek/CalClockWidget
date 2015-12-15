package cz.pazzi.clockwidget.Providers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import java.util.Calendar;
import java.util.List;

import cz.pazzi.clockwidget.data.GEvent;

/**
 * Created by pavel on 09.12.15.
 */
public class BitmapOperations {

    public static Bitmap GetTimelineBitmap(List<GEvent> events) {
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

    public static void AddEventToBitmap(Bitmap bitmap, GEvent event, float densityPerMinute) {
        int pixelCount = (int)(densityPerMinute * event.DurationInMinutes());
        int pixelOffset = (int)(densityPerMinute * event.StartAtMinutes());
//        Log.d("addEventToBitmap", "event duration = " + event.DurationInMinutes());
//        Log.d("addEventToBitmap", "density per minute = " + densityPerMinute);
//        Log.d("addEventToBitmap", "pixel count = " + pixelCount);
//        Log.d("addEventToBitmap", "pixel offset = " + pixelOffset);
//        Log.d("addEventToBitmap", "event color = " + event.backgroundColor);

        for(int i = pixelOffset; i < pixelOffset + pixelCount && i < bitmap.getWidth(); i++) {
            bitmap.setPixel(i, 0, event.backgroundColor);
        }
    }

    public static Bitmap ScaleBitmap(Bitmap original, DisplayMetrics metrics, int newWidth, int newHeight) {
        int width = original.getWidth();
        int height = original.getHeight();
        int boundingX = Math.round(newWidth * metrics.density);
        int boundingY = Math.round(newHeight * metrics.density);
        float xScale = ((float) boundingX) / width;
        float yScale = ((float) boundingY) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(xScale, yScale);
        return Bitmap.createBitmap(original, 0, 0, width, height, matrix , false);
    }

    public static Bitmap DrawActualTime(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        float density = width / (float)(24 * 60);
        Calendar actual = Calendar.getInstance();
        int pixelOffset = (int)(density * (actual.get(Calendar.HOUR_OF_DAY) - 1) * 60 + actual.get(Calendar.MINUTE));
        for(int y = 0; y < height; y++) {
            bitmap.setPixel(pixelOffset, y, Color.BLACK);
        }

        return bitmap;
    }

    public static Bitmap DrawTextToBitmap(Bitmap original, String text) {
        Canvas canvas = new Canvas(original);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE); // Text Color
//        paint.setStrokeWidth(100); // Text Size
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
        paint.setTextSize(original.getHeight() - 30);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (original.getWidth() - bounds.width())/2;
        int y = (original.getHeight() - bounds.height()/2);

        canvas.drawBitmap(original, 0, 0, paint);
        canvas.drawText(text, x, y, paint);

        return original;
    }
}
