package com.chin.ben.firstgo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public class MenuThread extends Thread {
    //Different mMode states
    public static final int STATE_LOSE = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_WIN = 5;

    public float[] headCoord = {-10,-10};

    private long lastFrame;

    //Control variable for the mode of the game (e.g. STATE_WIN)
    protected int mMode = 1;

    //Control of the actual running inside run()
    private boolean mRun = false;

    //The surface this thread (and only this thread) writes upon
    private SurfaceHolder mSurfaceHolder;

    //the message handler to the View/Activity thread
    private Handler mHandler;

    //Android Context - this stores almost all we need to know
    private Context mContext;

    //The view
    public MenuView mGameView;

    //We might want to extend this call - therefore protected
    protected int mCanvasWidth = 1;
    protected int mCanvasHeight = 1;

    //Last time we updated the game physics
    protected long mLastTime = 0;

    protected Bitmap mBackgroundImage;
    private Bitmap mhead;

    protected long score = 0;

    private long now;
    private float elapsed;


    public MenuThread(MenuView gameView) {
        mGameView = gameView;

        mSurfaceHolder = gameView.getHolder();
        mHandler = gameView.getmHandler();
        mContext = gameView.getContext();

        mBackgroundImage = BitmapFactory.decodeResource
                (gameView.getContext().getResources(),
                        R.drawable.menu);

        mhead = setupBitmap(R.drawable.head);

    }

    public Bitmap setupBitmap(int path){

        return BitmapFactory.decodeResource(mGameView.getContext().getResources(), path);

    }

    /*
	 * Called when app is destroyed, so not really that important here
	 * But if (later) the game involves more thread, we might need to stop a thread, and then we would need this
	 * Dare I say memory leak...
	 */
    public void cleanup() {
        this.mContext = null;
        this.mGameView = null;
        this.mHandler = null;
        this.mSurfaceHolder = null;
    }

    //Pre-begin a game
    public void setupBeginning(){

    }

    //Starting up the game
    public void doStart() {
        synchronized (mSurfaceHolder) {

            setupBeginning();

            mLastTime = System.currentTimeMillis() + 100;

            setState(STATE_RUNNING);

        }
    }

    //The thread start
    @Override
    public void run() {
        Canvas canvasRun;
        lastFrame = System.currentTimeMillis();
        while (mRun) {
            canvasRun = null;
            try {
                canvasRun = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    if (mMode == STATE_RUNNING) {
                        updatePhysics();
                    }
                    doDraw(canvasRun);
                }
            } finally {
                if (canvasRun != null) {
                    if (mSurfaceHolder != null)
                        mSurfaceHolder.unlockCanvasAndPost(canvasRun);
                }
            }
            while ((System.currentTimeMillis() - mLastTime) < 20) {

            }
        }
    }

    /*
	 * Surfaces and drawing
	 */
    public void setSurfaceSize(int width, int height) {
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;

            // don't forget to resize the background image
            mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
        }
    }


    protected void doDraw(Canvas canvas) {

        if (canvas == null) return;

        if (mBackgroundImage != null) canvas.drawBitmap(mBackgroundImage, 0, 0, null);

        canvas.drawBitmap(mhead, headCoord[0], headCoord[1], null);
    }

    private void updatePhysics() {
        now = System.currentTimeMillis();
        elapsed = (now - mLastTime) / 1000.0f;

        updateGame(elapsed);

        mLastTime = now;
    }

    protected void updateGame(float secondsElapsed){

    }
	
	/*
	 * Control functions
	 */

    //Finger touches the screen
    public boolean onTouch(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }

        if (mMode == STATE_READY || mMode == STATE_LOSE || mMode == STATE_WIN) {
            doStart();
            return true;
        }

        if (mMode == STATE_PAUSE) {
            unpause();
            return true;
        }

        float x = e.getRawX();
        float y = e.getRawY();
        //370 and 306
        if((mCanvasWidth * 0.54) < x  && x < (mCanvasWidth * 0.6) && (mCanvasHeight * 0.158) < y && y < (mCanvasHeight * 0.3)){
            headCoord[0] = (float) (mCanvasWidth * 0.5472);
            headCoord[1] = (float) (mCanvasHeight * 0.1589);
            selectMenu.Level = 0;
            selectMenu.World = 0;
            synchronized (mSurfaceHolder){
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("level", "1");
                b.putInt("msg", 0);
                msg.setData(b);
                mHandler.sendMessage(msg);
                msg = mHandler.obtainMessage();
                b = new Bundle();
                b.putString("world", "0");
                b.putInt("msg", 1);
                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        } else if((mCanvasWidth * 0.44) < x  && x < (mCanvasWidth * 0.51) && (mCanvasHeight * 0.284) < y && y < (mCanvasHeight * 0.4)){
            headCoord[0] = (float) (mCanvasWidth * 0.4477);
            headCoord[1] = (float) (mCanvasHeight * 0.2847);
            selectMenu.Level = 1;
            selectMenu.World = 0;
            synchronized (mSurfaceHolder) {
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("level", "0");
                b.putInt("msg", 0);
                msg.setData(b);
                mHandler.sendMessage(msg);
                msg = mHandler.obtainMessage();
                b = new Bundle();
                b.putString("world", "0");
                b.putInt("msg", 1);
                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }
        synchronized (mSurfaceHolder) {
            this.actionOnTouch(e.getRawX(), e.getRawY());
        }

        return false;
    }

    protected void actionOnTouch(float x, float y) {
        //Override to do something
    }

    //The Accellerometer has changed
    public void onSensorChanged(SensorEvent event) {
        synchronized (mSurfaceHolder) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                actionWhenPhoneMoved(event.values[2],event.values[1], event.values[0]);
            }
        }
    }

    protected void actionWhenPhoneMoved(float xDirection, float yDirection, float zDirection) {
        //Override to do something
    }

    /*
	 * Game states
	 */
    public void pause() {
        synchronized (mSurfaceHolder) {
            if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
        }
    }

    public void unpause() {
        // Move the real time clock up to now
        synchronized (mSurfaceHolder) {
            mLastTime = System.currentTimeMillis();
        }
        setState(STATE_RUNNING);
    }

    //Send messages to View/Activity thread
    public void setState(int mode) {
        synchronized (mSurfaceHolder) {
            setState(mode, null);
        }
    }

    public void setState(int mode, CharSequence message) {
        synchronized (mSurfaceHolder) {
            mMode = mode;

            if (mMode == STATE_RUNNING) {
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("text", "");
                b.putInt("viz", View.INVISIBLE);
                b.putBoolean("showAd", false);
                msg.setData(b);
                mHandler.sendMessage(msg);
            } else {
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();

                Resources res = mContext.getResources();
                CharSequence str = "";
                if (mMode == STATE_READY)
                    str = res.getText(R.string.mode_ready);
                else if (mMode == STATE_PAUSE)
                    str = res.getText(R.string.mode_pause);
                else if (mMode == STATE_LOSE)
                    str = res.getText(R.string.mode_lose);
                else if (mMode == STATE_WIN) {
                    str = res.getText(R.string.mode_win);
                }

                if (message != null) {
                    str = message + "\n" + str;
                }

                str = "";

                b.putString("text", str.toString());
                b.putInt("viz", View.VISIBLE);

                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }
    }

    /*
	 * Getter and setter
	 */
    public void setSurfaceHolder(SurfaceHolder h) {
        mSurfaceHolder = h;
    }

    public boolean isRunning() {
        return mRun;
    }

    public void setRunning(boolean running) {
        mRun = running;
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mMode) {
        this.mMode = mMode;
    }

}
