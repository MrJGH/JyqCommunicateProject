package com.tencent.qcloud.tuikit.tuichat.classicui.widget.input;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.qcloud.tuikit.tuichat.R;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 语音录制的动画效果
 */
public class WaveVoiceView extends View {
    private static   int LINE_WIDTH = 3;//默认矩形波纹的宽度，3像素, 原则上从layout的attr获得
    private Paint paint = new Paint();
    private Runnable task;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private RectF rectLeft = new RectF();//波纹矩形的数据
    private int updateSpeed;
    private int lineHeight;
    private int lineColor;
    private float lineWidth;

    private float mWidth;
    private float mHeight;
    private float mScal;

    public WaveVoiceView(Context context) {
        super(context);
    }

    public WaveVoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, context);
        resetView(mWaveList);
        task = new LineJitterTask();
    }

    private void initView(AttributeSet attrs, Context context) {
        //获取布局属性里的值
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveVoiceView);
        lineColor = mTypedArray.getColor(R.styleable.WaveVoiceView_voiceLineColor, Color.parseColor("#3399FF"));
        lineWidth = mTypedArray.getDimension(R.styleable.WaveVoiceView_voiceLineWidth, LINE_WIDTH);
        updateSpeed = mTypedArray.getColor(R.styleable.WaveVoiceView_updateSpeed, UPDATE_INTERVAL_TIME);
        mTypedArray.recycle();
        mScal = getResources().getDimension(R.dimen.wave_scal);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取实际宽高的一半
        int widthCentre = getWidth();
        int heightCentre = getHeight() / 2;
        paint.setStrokeWidth(0);

        //设置颜色
        paint.setColor(lineColor);
        //填充内部
        paint.setStyle(Paint.Style.FILL);
        //设置抗锯齿
        paint.setAntiAlias(true);
        for (int i = 0; i < 20; i++) {

            //左边矩形
            rectLeft.left = widthCentre  - (2 + 2 * i) * (lineWidth );
            rectLeft.top = heightCentre - (mHeight / 32 +(float) mWaveList.get(i) / 8 * mHeight / 4)  ;
            rectLeft.right = widthCentre  - (1 + 2 * i) * (lineWidth);
            rectLeft.bottom = heightCentre + (mHeight / 32 +(float) mWaveList.get(i) / 8 * mHeight / 4 );

            canvas.drawRoundRect(rectLeft, 0, 0, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
    }

  //  private static final int[] DEFAULT_WAVE_HEIGHT = {3,3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};
    private static final int UPDATE_INTERVAL_TIME = 200;//100ms更新一次
    private LinkedList<Integer> mWaveList = new LinkedList<>();
    private float maxDb;

    private void resetView(List<Integer> list ) {
        list.clear();
        for (int i = 0;i<20;i++) {
            list.add(1);
        }
    }

    private synchronized void refreshElement() {
        maxDb = lineHeight;
        mWaveList.add(0, (int)maxDb);
        mWaveList.removeLast();
    }

    public boolean isStart = false;

    private class LineJitterTask implements Runnable {
        @Override
        public void run() {
            while (isStart) {
                refreshElement();
                try {
                    Thread.sleep(updateSpeed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                postInvalidate();
            }
        }
    }

    public synchronized void startRecord() {
        isStart = true;
        executorService.execute(task);
    }

    public synchronized void stopRecord() {
        isStart = false;
        mWaveList.clear();
        resetView(mWaveList);
        postInvalidate();
    }


    public void setUpdateSpeed(int updateSpeed) {
        this.updateSpeed = updateSpeed;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
//        Logger.getLogger().e("lineHeight="+lineHeight);
    }

}
