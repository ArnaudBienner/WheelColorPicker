package com.example.myapplication2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by wesj on 12/12/13.
 */
public class Slice extends View {

    private static final String LOGTAG = "Slice";
    private static Path mPath;
    private static Paint mPaint = new Paint();
    private static Paint mInnerRing = new Paint();
    private static int mStrokeWidth = 40;
    private static int mInnerWidth = 120;
    private float mAngle;
    private float mRadius;
    private float mOffset;

    public Slice(Context context) {
        super(context);
        init(context);
    }

    public Slice(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Slice(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mInnerRing.setColor(Color.argb(20, 0, 0, 0));
        mInnerRing.setStrokeWidth(mStrokeWidth);
        mInnerRing.setStyle(Paint.Style.FILL);

        Drawable d = getBackground();
        int color = Color.BLACK;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // http://stackoverflow.com/questions/8089054/get-the-background-color-of-a-button-in-android
            // If the ColorDrawable makes use of its bounds in the draw method,
            // we may not be able to get the color we want. This is not the usual
            // case before Ice Cream Sandwich (4.0.1 r1).
            // Yet, we change the bounds temporarily, just to be sure that we are
            // successful.
            ColorDrawable colorDrawable = (ColorDrawable) d;

            Rect bounds = new Rect();
            bounds.set(colorDrawable.getBounds()); // Save the original bounds.
            colorDrawable.setBounds(0, 0, 1, 1); // Change the bounds.

            Bitmap bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            colorDrawable.draw(canvas);
            color = bitmap.getPixel(0, 0);

            colorDrawable.setBounds(bounds); // Restore the original bounds.
        } else {
            color = ((ColorDrawable) d).getColor();
        }
        mPaint.setColor(color);
    }

    protected void onConfigurationChanged (Configuration newConfig) {
        mPath = null;
    }

    public void draw(Canvas canvas) {
        int w = getWidth();

        int save = canvas.save();
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
        canvas.drawCircle(0, getHeight()/2, mInnerWidth + mStrokeWidth, mInnerRing);

        canvas.restoreToCount(save);
    }

    public void setParams(float angle, float r, float offset) {
        mAngle = angle;
        mRadius = r;
        if (mOffset != offset) {
            mOffset = offset;
        }
    }
}
