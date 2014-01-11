package com.example.myapplication2;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Arrays;
import java.util.List;

/**
 * Created by wesj on 12/11/13.
 */
public class AdvancedColorPicker extends LinearLayout {
    private static final String LOGTAG = "AdvancedColorPicker";
    private static final int HUE = 1;
    private static final int SAT = 2;
    private static final int VAL = 3;
    private static final int RED = 4;
    private static final int GREEN = 5;
    private static final int BLUE = 6;

    // Conversions between HSV and RGB can be lossy, so we store both
    // separately so that all of the sliders can move independently
    private float[] mHSV = new float[] { 0f, 1f, 1f };
    private int mColor = Color.BLACK;

    private Bar mHueBar;
    private Bar mSatBar;
    private Bar mValBar;
    private Bar mRedBar;
    private Bar mGreenBar;
    private Bar mBlueBar;
    private Wheel wheel;

    private final static List<Integer> DEFAULT_COLORS = Arrays.asList(
            Color.rgb(215, 57, 32),
            Color.rgb(255, 134, 5),
            Color.rgb(255, 203, 19),
            Color.rgb(95, 173, 71),
            Color.rgb(84, 201, 168),
            Color.rgb(33, 161, 222),
            Color.rgb(16, 36, 87),
            Color.rgb(91, 32, 103),
            Color.rgb(212, 221, 228),
            Color.BLACK);

    private boolean mUpdating;
    private Animator mAnimator;
    private final int HSV = 0;
    private final int RGB = 1;
    private int mCurrentMode = HSV;

    private class Bar {
        private SeekBar seekbar;
        private EditText label;

        public Bar(SeekBar bar, EditText label) {
            this.seekbar = bar;
            this.label = label;
        }
    }

    public AdvancedColorPicker(Context context) {
        this(context, null);
    }

    public AdvancedColorPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("NewApi")
    public AdvancedColorPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.advanced_color_picker, this);
        Color.colorToHSV(DEFAULT_COLORS.get(2), mHSV);

        wheel = (Wheel) findViewById(R.id.wheel);
        for (Integer i : DEFAULT_COLORS) {
            Slice s = new Slice(context);
            s.setBackgroundColor(i);
            s.setTag(i);
            wheel.addView(s);
        }

        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.sliderBox);
        wheel.addListener(new Wheel.WheelListener() {
            @Override
            public void onChange(int index) {
                int color = DEFAULT_COLORS.get(index);
                if (mCurrentMode == HSV) {
                    float[] hsv = new float[3];
                    Color.colorToHSV(color, hsv);
                    mAnimator = new HSVAnimator(new float[] {mHSV[0], mHSV[1], mHSV[2]}, hsv);
                } else {
                    mAnimator = new ColorAnimator(new int[] { Color.red(mColor), Color.green(mColor), Color.blue(mColor) },
                                                  new int[] { Color.red(color),  Color.green(color),  Color.blue(color) });
                }
                mAnimator.start();
            }
        });
        Display display;
        display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size.x > size.y) {
            setOrientation(HORIZONTAL);
            wheel.setCenter(1.02f, 0.5f);
        } else {
            setOrientation(VERTICAL);
            wheel.setCenter(0.5f, 1.02f);
        }

        mHueBar = findBar(R.id.hueSeekbar, R.id.hueLabel, HUE);
        mSatBar = findBar(R.id.satSeekbar, R.id.satLabel, SAT);
        mValBar = findBar(R.id.valSeekbar, R.id.valLabel, VAL);

        mRedBar = findBar(R.id.redSeekbar, R.id.redLabel, RED);
        mGreenBar = findBar(R.id.greenSeekbar, R.id.greenLabel, GREEN);
        mBlueBar = findBar(R.id.blueSeekbar, R.id.blueLabel, BLUE);


        TextView hsvLabel = (TextView) findViewById(R.id.hsvLabel);
        hsvLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mColor = Color.HSVToColor(mHSV);
                updateGradients(mColor);
                flipper.showNext();
                mCurrentMode = RGB;
            }
        });

        TextView rgbLabel = (TextView) findViewById(R.id.rgbLabel);
        rgbLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Color.colorToHSV(mColor, mHSV);
                updateGradients(mHSV);
                flipper.showPrevious();
                mCurrentMode = HSV;
            }
        });

        updateGradients(mHSV);
    }

    @SuppressLint("NewApi")
    private Bar findBar(final int seekbarId, final int labelId, final int type) {
        BorderedBox border = new BorderedBox();
        border.setBorderRadius(1);
        border.setBorderThickness(3);
        border.setBorderColor(Color.LTGRAY);
        border.setShadowInset(true);
        border.setShadowShader(new LinearGradient(0f, 0f, 0f, 50, new int[] {
                Color.argb(100,0,0,0), Color.argb(0,0,0,0)
        }, null, Shader.TileMode.CLAMP));

        final Bar bar = new Bar((SeekBar) findViewById(seekbarId),
                                (EditText) findViewById(labelId));
        if (type != HUE) {
            bar.seekbar.setProgressDrawable(border);
        } else {
            bar.seekbar.setBackground(new ShapeDrawable(new RectShape()));
        }

        bar.label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mUpdating)
                    return;

                float val = 0;
                try {
                    val = Float.parseFloat(bar.label.getText().toString());
                } catch (Exception ex) { }

                switch (type) {
                    case HUE: setHSV(val, mHSV[1], mHSV[2]); break;
                    case SAT: setHSV(mHSV[0], val / 100f, mHSV[2]); break;
                    case VAL: setHSV(mHSV[0], mHSV[1], val / 100f); break;
                    default:
                        switch(type) {
                            case RED: mColor = Color.rgb((int) val, Color.green(mColor), Color.blue(mColor)); break;
                            case BLUE: mColor = Color.rgb(Color.red(mColor), Color.green(mColor), (int) val); break;
                            case GREEN: mColor = Color.rgb(Color.red(mColor), (int) val, Color.blue(mColor)); break;
                        }
                        updateGradients(mColor);
                        setWheel(mColor);
                }
            }
        });
        bar.seekbar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                if (mUpdating)
                    return;

                switch (type) {
                    case HUE: setHSV(progress / 100f * 360f, mHSV[1], mHSV[2]); break;
                    case SAT: setHSV(mHSV[0], 1.0f - progress / 100f, mHSV[2]); break;
                    case VAL: setHSV(mHSV[0], mHSV[1], progress / 100f);        break;
                    default:
                        switch (type) {
                            case RED:   mColor = Color.rgb((int) (progress / 100f * 255f), Color.green(mColor), Color.blue(mColor)); break;
                            case BLUE:  mColor = Color.rgb(Color.red(mColor), Color.green(mColor), (int) (progress / 100f * 255f)); break;
                            case GREEN: mColor = Color.rgb(Color.red(mColor), (int) (progress / 100f * 255f), Color.blue(mColor)); break;
                        }
                        updateGradients(mColor);
                        setWheel(mColor);
                }
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
                updateLabels();
            }
        });

        return bar;
    }

    private void setWheel(float[] hsv) {
        setWheel(Color.HSVToColor(hsv));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void updateGradients(float[] hsv) {
        if (mUpdating) return;
        mUpdating = true;

        final int SIZE = 10;
        int[] grad = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            grad[i] = Color.HSVToColor(new float[]{360*i/(SIZE-1), hsv[1], hsv[2]});
        }

        mHueBar.seekbar.setBackgroundGradientColors(grad, mHueBar.seekbar.getMeasuredWidth());
        mSatBar.seekbar.setGradientColors(new int[] {
                Color.HSVToColor(new float[]{hsv[0], 1, hsv[2]}),
                Color.HSVToColor(new float[]{hsv[0], 0, hsv[2]}),
        }, mSatBar.seekbar.getMeasuredWidth());
        mValBar.seekbar.setGradientColors(new int[] {
                Color.HSVToColor(new float[]{hsv[0], hsv[1], 0}),
                Color.HSVToColor(new float[]{hsv[0], hsv[1], 1}),
        }, mValBar.seekbar.getMeasuredWidth());

        int color = Color.HSVToColor(hsv);
        mHueBar.seekbar.setThumbColor(color);
        mSatBar.seekbar.setThumbColor(color);
        mValBar.seekbar.setThumbColor(color);

        mHueBar.seekbar.setProgress((int) (hsv[0]/360*100));
        mSatBar.seekbar.setProgress((int) (100 - hsv[1]*100));
        mValBar.seekbar.setProgress((int) (hsv[2]*100));

        mUpdating = false;
    }

    private void updateGradients(int color) {
        if (mUpdating) return;
        mUpdating = true;

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        mRedBar.seekbar.setGradientColors(new int[]{Color.rgb(0, g, b), Color.rgb(255, g, b)}, mRedBar.seekbar.getMeasuredWidth());
        mGreenBar.seekbar.setGradientColors(new int[]{Color.rgb(r, 0, b), Color.rgb(r, 255, b)}, mGreenBar.seekbar.getMeasuredWidth());
        mBlueBar.seekbar.setGradientColors(new int[]{Color.rgb(r, g, 0), Color.rgb(r, g, 255)}, mBlueBar.seekbar.getMeasuredWidth());

        mRedBar.seekbar.setThumbColor(color);
        mGreenBar.seekbar.setThumbColor(color);
        mBlueBar.seekbar.setThumbColor(color);

        Log.i(LOGTAG, "Red " + r + " ----- " + (r*100/255));
        mRedBar.seekbar.setProgress(r*100/255);
        mGreenBar.seekbar.setProgress(g*100/255);
        mBlueBar.seekbar.setProgress(b*100/255);

        mUpdating = false;
    }

    private void updateLabels() {
        if (mUpdating)
            return;
        mUpdating = true;
        mHueBar.label.setText(Integer.toString(Math.round(mHSV[0])));
        mSatBar.label.setText(Integer.toString(Math.round(mHSV[1] * 100)));
        mValBar.label.setText(Integer.toString(Math.round(mHSV[2] * 100)));

        int color = Color.HSVToColor(mHSV);
        mRedBar.label.setText(Integer.toString(Color.red(color)));
        mBlueBar.label.setText(Integer.toString(Color.blue(color)));
        mGreenBar.label.setText(Integer.toString(Color.green(color)));
        mUpdating = false;
    }

    private abstract class Animator<T> {
        protected T mStart;
        protected T mEnd;
        protected long DURATION = 500;
        private long startTime = -1;

        abstract protected void endAnimation();
        abstract protected void stepAnimation(float dt);

        public Animator(T start, T end) {
            mStart = start;
            mEnd = end;
        }

        public void start() {
            startTime = System.currentTimeMillis();
            step();
        }

        private void stop() {
            mStart = null;
            startTime = -1;
            endAnimation();
            mEnd = null;
        }

        public void step() {
            if (mEnd == null) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now > startTime + DURATION) {
                stop();
                return;
            }

            float dt = (float)(now - startTime);
            stepAnimation(dt/DURATION);

            postInvalidateOnAnimation();
        }
    }

    private class ColorAnimator extends Animator<int[]> {
        public ColorAnimator(int[] start, int[] end) {
            super(start, end);
        }

        @Override
        protected void endAnimation() {
            mColor = Color.rgb(mEnd[0], mEnd[1], mEnd[2]);
            updateGradients(mColor);
            setWheel(mColor);
            updateLabels();
            mAnimator = null;
        }

        @Override
        protected void stepAnimation(float dt) {
            mColor = Color.rgb((int) (mStart[0] + (mEnd[0] - mStart[0])*dt),
                               (int) (mStart[1] + (mEnd[1] - mStart[1])*dt),
                               (int) (mStart[2] + (mEnd[2] - mStart[2])*dt));
            updateGradients(mColor);
            setWheel(mColor);
        }
    }

    private void setWheel(int color) {
        wheel.setColor(color);
        wheel.invalidate();
    }

    private class HSVAnimator extends Animator<float[]> {
        public HSVAnimator(float[] start, float[] end) {
            super(start, end);
        }

        @Override
        protected void endAnimation() {
            setHSV(mEnd[0], mEnd[1], mEnd[2]);
            updateLabels();
            mAnimator = null;
        }

        @Override
        protected void stepAnimation(float dt) {
            setHSV(mStart[0] + (mEnd[0] - mStart[0]) * dt,
                    mStart[1] + (mEnd[1] - mStart[1]) * dt,
                    mStart[2] + (mEnd[2] - mStart[2]) * dt);
        }
    };

    public void dispatchDraw(Canvas canvas) {
        if (mAnimator != null)
            mAnimator.step();
        super.dispatchDraw(canvas);
    }

    private void setHSV(float h, float s, float v) {
        mHSV[0] = h;
        mHSV[1] = s;
        mHSV[2] = v;
        updateGradients(mHSV);
        setWheel(mHSV);
    }
}