package cz.pazzi.clockwidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Pazzi on 17.9.2015.
 */
public class MyClock extends ImageView {

    public MyClock(Context context) {
        super(context);
    }

    public MyClock(Context context, AttributeSet attr){
        super(context,attr);
    }

    public MyClock(Context context, AttributeSet attr, int defaultStyles){
        super(context, attr, defaultStyles);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {

        int measuredWidth = MeasureSpec.getSize(widthSpec);
        int measuredHeight = MeasureSpec.getSize(heightSpec);

        /*measuredWidth and measured height are your view boundaries. You need to change these values based on your requirement E.g.
        if you want to draw a circle which fills the entire view, you need to select the Min(measuredWidth,measureHeight) as the radius.
        Now the boundary of your view is the radius itself i.e. height = width = radius. */
        /* After obtaining the height, width of your view and performing some changes you need to set the processed value as your view dimension by using the method setMeasuredDimension */

        setMeasuredDimension(measuredWidth, measuredHeight);

        /* If you consider drawing circle as an example, you need to select the minimum of height and width and set that value as your screen dimensions
        int d=Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(d,d); */

    }
    @Override

    protected void onDraw(Canvas canvas){
        //get the size of your control based on last call to onMeasure
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        // Now create a paint brush to draw your widget
        Paint mTextPaint = new Paint();
        mTextPaint.setColor(Color.GREEN);
        // set’s paint’s text size
        mTextPaint.setTextSize(40);
        // Define the string you want to paint
        String displayText = "My First Widget";
        // Measure width of your text string
        Float textWidth = mTextPaint.measureText(displayText);
        //Find the center
        int px= width/2;
        int py=height/2;
        // Draw the string in the center of the control
        canvas.drawText(displayText, px-textWidth/2, py, mTextPaint);
    }
}
