package com.minicup.waveprogress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by gy on 2018/3/6.
 */

public class WaveProgress extends View {

    private final Context context;
    private String centerText;
    private float centerTextSize;
    private int centerTextColor;
    private int ballColor;
    private float radius;
    private Paint roundPaint;
    private Paint fontPaint;
    private int progress;
    private int width;
    private int height;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;
    private GestureDetector detector;
    private SingleTapThread singleTapThread;
    private float maxProgress = 100;
    private Paint progressPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Path wavePath;
    private float space = 30;
    private Paint paint;
    private float tide;

    public WaveProgress(Context context) {
        this(context, null);
    }

    public WaveProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setBackgroundColor(Color.GRAY);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveProgress);
        centerText = typedArray.getString(R.styleable.WaveProgress_centerText);
        centerTextSize = typedArray.getDimension(R.styleable.WaveProgress_centerTextSize,24f);
        centerTextColor = typedArray.getColor(R.styleable.WaveProgress_centerTextColor,0xFFFFFF);
        ballColor = typedArray.getColor(R.styleable.WaveProgress_ballColor,0xFF4081);
        radius = typedArray.getDimension(R.styleable.WaveProgress_ballRadius,260f);
        typedArray.recycle();
        initPaint();
        tide = 30;

    }
    private void initPaint() {
        roundPaint = new Paint();
        roundPaint.setColor(ballColor);
        roundPaint.setAntiAlias(true);//抗锯齿
        fontPaint = new Paint();

        fontPaint.setTextSize(centerTextSize);
        fontPaint.setColor(centerTextColor);
        fontPaint.setAntiAlias(true);
        fontPaint.setFakeBoldText(true);//粗体

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = getDefaultSize(100, widthMeasureSpec);
        int heightSize = getDefaultSize(100, heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getWidth();
        height = getHeight();

        simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                if (singleTapThread == null) {
                    singleTapThread = new SingleTapThread();
                    getHandler().postDelayed(singleTapThread, 100);
                }
                return super.onSingleTapConfirmed(e);
            }
        };
            detector = new GestureDetector(context, simpleOnGestureListener);

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            });
        bitmap = Bitmap.createBitmap((int) radius * 2, (int) radius * 2, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        wavePath = new Path();

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        bitmapCanvas.drawCircle(radius,radius, radius, roundPaint);
        float y = (1 - (float) progress / maxProgress) * radius * 2 ;

        if(progress==100){
            tide = 0;
        }

        wavePath.moveTo(2*radius, y);
        wavePath.lineTo(2*radius, 2*radius);
        wavePath.lineTo(0, 2* radius);
        wavePath.lineTo(0, y);

        wavePath.lineTo(-((float) progress / maxProgress)*2000, y);

        for (int i = 0; i < 15; i++) {
            wavePath.rQuadTo(50, tide, 100, 0);
            wavePath.rQuadTo(50, -tide, 100, 0);
        }

        wavePath.close();
        bitmapCanvas.drawPath(wavePath, progressPaint);

        float v = (width - 2 * radius) / 2;
        canvas.drawBitmap(bitmap,v, v, null);


        float textWidth = fontPaint.measureText(progress + "%");
        Paint.FontMetrics fontMetrics = fontPaint.getFontMetrics();
        float ascent = fontMetrics.ascent;
        float descent = fontMetrics.descent;
        float textHeight = descent - ascent;
        canvas.drawText(progress + "%", width/2 - textWidth / 2, height/2 + textHeight/2 , fontPaint);
    }

    private class SingleTapThread implements Runnable {
        @Override
        public void run() {
            if (progress < maxProgress) {
                progress++;
                invalidate();
                getHandler().postDelayed(singleTapThread, 100);

            } else {
                getHandler().removeCallbacks(singleTapThread);
            }
        }
    }
}
