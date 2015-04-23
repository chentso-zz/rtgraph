package tso.chen.rtgraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by tso on 21/04/15.
 */
public class RTGraph extends SurfaceView implements Runnable{

    Thread thread = null;
    Paint paint = new Paint();
    SurfaceHolder surfaceHolder;

    Paint maskPaint;
    Paint cursor;

    int bgColor = Color.WHITE;

    private int viewWidth;
    private int viewHeight;
    private int maskSize = 30;
    private int runner = 0;

    private Queue<Integer> activeHalf = new ConcurrentLinkedQueue<Integer>();
    private Queue<Integer> trailingHalf = new ConcurrentLinkedQueue<Integer>();
    private List<Float> data;

    private boolean running = true;

    public RTGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        maskPaint = new Paint();
        maskPaint.setColor(bgColor);
        cursor = new Paint();
        cursor.setStyle(Paint.Style.STROKE);
        cursor.setColor(Color.argb(128, 79, 114, 219));
        cursor.setStrokeWidth(1);
    }

    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        data = Collections.synchronizedList(new ArrayList<Float>(w));
        for(int i = 0; i < w; i++){
            data.add(-1.f);
        }
        viewWidth = w;
        viewHeight = h;
    }

    @Override
    public void run() {
        while(running) {
            if (surfaceHolder.getSurface().isValid()) {

                    Queue<Float> threadSafeCopy = new ConcurrentLinkedQueue<>(data);


                    paint.setColor(Color.BLACK);
                    Canvas canvas = surfaceHolder.lockCanvas();
                    canvas.drawColor(bgColor);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1);
                    paint.setFlags(Paint.ANTI_ALIAS_FLAG);
                    Path p = new Path();


                    int i = 0;
                    if (!threadSafeCopy.isEmpty()){
                        p.moveTo(i, data.get(0));
                        for(float s : threadSafeCopy){
                            p.lineTo(i++, s);
                        }
                    }
                    canvas.drawPath(p, paint);




                    int r = runner - 5;
                    if ((r + maskSize) > viewWidth){
                        canvas.drawRect(r, 0, viewWidth, viewHeight, maskPaint);
                        canvas.drawRect(0, 0, maskSize - (viewWidth - (r)), viewHeight, maskPaint);
                    } else {
                        canvas.drawRect(r, 0, r+maskSize, viewHeight, maskPaint);
                    }

                    canvas.drawRect(r, 0, r+1, viewHeight, cursor);

                    surfaceHolder.unlockCanvasAndPost(canvas);


            }
        }
    }


    public void push(float fval){

        float val = (viewHeight/2) * -fval + (viewHeight/2);

        if ((data != null)){
            data.set(runner++, val);
        }

        if (runner >= viewWidth){
            runner = 0;
        }

    }

    public void clear(){
        activeHalf.clear();
        trailingHalf.clear();
        runner = 0;
    }

    public void setMaskSize(int size){
        maskSize = size;
    }
}
