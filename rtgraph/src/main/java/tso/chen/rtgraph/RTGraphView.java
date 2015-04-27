package tso.chen.rtgraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RTGraphView extends View implements Runnable {
    Context c;
    Thread thread;
    Paint paint = new Paint();
    Paint maskPaint;
    Paint cursor;
    int bgColor = Color.WHITE;
    int appColor;

    private int graphWidth;
    private int graphHeight;

    private int runner = 0;
    private List<Float> data;
    private boolean running = false;
    private boolean isReady = false;
    private float lastVal = 0;

    private boolean fillMode = false;
    private boolean demoMode = false;

    private int param_maskSize = 5;
    private float param_cornerRadius = 4.f;

    public RTGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;

        getAppColor();

        maskPaint = new Paint();
        maskPaint.setColor(bgColor);

        cursor = new Paint();
        cursor.setStyle(Paint.Style.STROKE);
        cursor.setColor(Color.argb(128, 79, 114, 219));
        cursor.setStrokeWidth(1);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.f);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

    }

    public void start(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void pause(){
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void matchAppColor(){
        bgColor = appColor;
    }

    private void getAppColor(){
        TypedValue a = new TypedValue();
        c.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            int color = a.data;
            appColor = color;
        }
    }

    @Override
    public void run() {
        while(running){
            // If this is in demo mode, then push some noise signal in
            if (demoMode) {
                // Can't push data if onSizeChanged hasn't been invoked yet
                if (!isReady) continue;

                // push noise signal
                push(generateNoise());
                try { thread.sleep(5,0);}
                catch (InterruptedException e) { e.printStackTrace(); }
            }

            // Refresh the frame
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Queue<Float> threadSafeCopy = new ConcurrentLinkedQueue<>(data);

        if (canvas == null) return;
        // Draw a rounded rectangle for background
        RectF rectF = new RectF(0, 0, graphWidth, graphHeight);
        canvas.drawRoundRect(rectF, param_cornerRadius, param_cornerRadius, maskPaint);

        Path p = new Path();
        int i = 0;
        if (!threadSafeCopy.isEmpty()){

            if (fillMode) {
                p.moveTo(0, graphHeight);
            } else {
                p.moveTo(i, data.get(0));
            }

            for(float s : threadSafeCopy){
                p.lineTo(i++, s);
            }

            if (fillMode){
                p.lineTo(graphWidth, graphHeight);
            }
        }
        canvas.drawPath(p, paint);

        int r = runner - 5;
        if ((r + param_maskSize) > graphWidth){
            canvas.drawRect(r, 0, graphWidth, graphHeight, maskPaint);
            canvas.drawRect(0, 0, param_maskSize - (graphWidth - r), graphHeight, maskPaint);
        } else {
            canvas.drawRect(r, 0, r+ param_maskSize, graphHeight, maskPaint);
        }
        canvas.drawRect(r, 0, r+1, graphHeight, cursor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        data = Collections.synchronizedList(new ArrayList<Float>(w));
        for(int i = 0; i < w; i++){
            data.add(-1.f);
        }
        isReady = true;
        graphWidth = w;
        graphHeight = h;

        setMaskSize(param_maskSize);
    }


    /** Push data into the data array for the graph
     *
     * @param fval
     */
    public void push(float fval){

        // If onSizeChanged hasn't been invoked, don't do anything yet
        if (!isReady) return;


        float val = (graphHeight/2) * -fval + (graphHeight/2);

        if ((data != null)){
            data.set(runner++, val);
        }

        if (runner >= graphWidth){
            runner = 0;
        }
    }



    /** Only for demo purposes
     *
     * @return
     */
    public float generateNoise(){

        Random r = new Random();
        int choice = r.nextInt(3) - 1;

        if (choice == 1) {
            lastVal += 0.015;
        } else if (choice == -1){
            lastVal -= 0.015;
        }

        if (lastVal > 1) lastVal = 1;
        if (lastVal < -1) lastVal = -1;

        return lastVal;
    }



    public void setBackGroundColor(int color){
        this.bgColor = color;
    }

    public void setCornerRadius(float radius){
        this.param_cornerRadius = radius;
    }

    /** Set the mask size using an integer of percentage size to the graph width
     *
     * @param percentage Percentage to the width of the graph
     */
    public void setMaskSize(int percentage){
        param_maskSize = (int)((float) percentage / 100 * graphWidth);
        return;
    }

    public void setLineWidth(float lineWidth){
        paint.setStrokeWidth(lineWidth);
        return;
    }

    public void setDemoMode(boolean status){
        demoMode = status;
        return;
    }

}
