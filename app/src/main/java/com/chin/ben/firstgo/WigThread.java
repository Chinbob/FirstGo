package com.chin.ben.firstgo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WigThread extends Thread {

    public static Random random;

    //Different mMode states
    public static final int STATE_LOSE = 1, STATE_PAUSE = 2, STATE_READY = 3, STATE_RUNNING = 4, STATE_WIN = 5;

    private long lastFrame;

    public WigActivity bob;
    private List<Mob> mobs;
    private Rect[] clouds;
    private int coins;
    public int translateX, translateY;
    public boolean done = false;
    public Tile[][] world;
    protected int offSet = 0;
    protected int mMode = 1;
    private boolean mRun = false;
    private SurfaceHolder mSurfaceHolder;
    private Handler mHandler;
    private Context mContext;
    public WigView mGameView;
    protected int mCanvasWidth = 1;
    protected int mCanvasHeight = 1;

    protected long mLastTime = 0;

    public MediaPlayer death;
    public Player player;
    private Bitmap[] textures;
    protected Bitmap mBackgroundImage;

    protected long score = 0;

    private long now;
    private float elapsed;


    public WigThread(WigView gameView) {
        mGameView = gameView;

        mSurfaceHolder = gameView.getHolder();
        mHandler = gameView.getmHandler();
        mContext = gameView.getContext();

        random = new Random();

        mBackgroundImage = BitmapFactory.decodeResource(gameView.getContext().getResources(),R.drawable.back);

        Bitmap bitmap = setupBitmap(R.drawable.capnwiggles);

        player = new Player(0, mCanvasHeight / 2, bitmap.getWidth(), bitmap.getHeight(), bitmap);

        translateX = 0;
        translateY = 0;
        coins = 0;

        textures = new Bitmap[8];
        textures[0] = setupBitmap(R.drawable.dirt);
        textures[1] = setupBitmap(R.drawable.grass);
        textures[2] = setupBitmap(R.drawable.coin1);
        textures[3] = setupBitmap(R.drawable.coin2);
        textures[4] = setupBitmap(R.drawable.coin3);
        textures[5] = setupBitmap(R.drawable.coin4);
        textures[6] = setupBitmap(R.drawable.coin5);
        textures[7] = setupBitmap(R.drawable.cloud);

        mobs = new ArrayList<Mob>();

        death = MediaPlayer.create(bob, R.raw.cedric);

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

        synchronized (mSurfaceHolder) {
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("msg", 1);
            b.putString("coins", " " + mCanvasWidth);
            msg.setData(b);
            mHandler.sendMessage(msg);
            msg = mHandler.obtainMessage();
            Bundle bu = new Bundle();
            bu.putInt("msg", 2);
            bu.putString("goompa", " " + mCanvasHeight);
            msg.setData(bu);
            mHandler.sendMessage(msg);
        }

        if(player != null){player.setY(mCanvasHeight / 2); player.setX(2);}

        WigActivity.theight = mCanvasHeight / 10;
        Log.d("setupBeginning", mCanvasHeight + " " + mCanvasWidth);
        WigActivity.twidth = WigActivity.theight;

        if(mCanvasWidth != 1){
            Message message = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("msg", 0);
            message.setData(b);
            mHandler.sendMessage(message);
        }
        clouds = new Rect[2];

        if(mCanvasWidth > 1) {
            int y = random.nextInt(mCanvasHeight / 3 * 2);
            clouds[0] = new Rect(-textures[7].getWidth(), y, 0, y + textures[7].getHeight());
            clouds[1] = new Rect(-textures[7].getWidth(), y, 0, y + textures[7].getHeight());
        }

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
                    if (mMode == STATE_RUNNING && mCanvasWidth > 1) {
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
            WigActivity.theight = height / 10;
            WigActivity.twidth = WigActivity.theight;

            // don't forget to resize the background image
            mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
        }
    }


    protected void doDraw(Canvas canvas) {

        if (canvas == null) return;

        if (mBackgroundImage != null) canvas.drawBitmap(mBackgroundImage, 0, 0, null);

        if (textures == null) return;

        canvas.translate(-translateX, -translateY);

        for (Rect rect: clouds){
            if(rect == null) continue;
            canvas.drawBitmap(textures[7], null, rect, null);
        }

        for (Tile[] atile: world){
            for(Tile tile: atile){

                tile.count++;

                switch(tile.type){
                    case 1:
                        if(tile.state == 0) {
                            canvas.drawBitmap(textures[0], null, tile.hitbox, null);
                        } else {
                            canvas.drawBitmap(textures[1], null, tile.hitbox, null);
                        }
                        break;
                    case 2:

                        if(tile.state < 3 && tile.hitbox.intersect(player.hitbox)){
                            tile.state = 3;
                            tile.count = 0;
                            coins++;
                            Message message = mHandler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putInt("msg", 1);
                            b.putString("coins", "" + coins);
                            message.setData(b);
                            mHandler.sendMessage(message);
                        }

                        switch (tile.state){
                            case 0:
                                canvas.drawBitmap(textures[2], null, tile.hitbox, null);
                                if (tile.count > 10){
                                    tile.state++;
                                    tile.count = 0;
                                }
                                break;
                            case 1:
                                canvas.drawBitmap(textures[3], null, tile.hitbox, null);
                                if (tile.count > 10){
                                    tile.state++;
                                    tile.count = 0;
                                }
                                break;
                            case 2:
                                canvas.drawBitmap(textures[4], null, tile.hitbox, null);
                                if (tile.count > 10){
                                    tile.state = 0;
                                    tile.count = 0;
                                }
                                break;
                            case 3:
                                canvas.drawBitmap(textures[5], null, tile.hitbox, null);
                                if (tile.count > 10){
                                    tile.state++;
                                    tile.count = 0;
                                }
                                break;
                            case 4:
                                canvas.drawBitmap(textures[6], null, tile.hitbox, null);
                                if (tile.count > 10){
                                    tile.type = 0;
                                }
                                break;

                        }
                    default:
                        break;
                }

            }
        }

        canvas.drawBitmap(player.getTex(), null, player.hitbox, null);

    }

    private void updatePhysics() {
        now = System.currentTimeMillis();
        elapsed = (now - mLastTime) / 1000.0f;

        updateGame(elapsed);

        mLastTime = now;

        try {
            player.update();
        } catch(ArrayIndexOutOfBoundsException e){

        }

        for (Rect rect: clouds){
            if (rect != null){
                rect.offset(1, 0);
            }
        }

        if(player.getY() + player.vY + 5 >= mCanvasHeight){
            death.start();
            this.doStart();
        }

    }

    protected void updateGame(float secondsElapsed){

        if( translateX > -1 && player.getX() < mCanvasWidth / 3 + translateX){

            translateX -= Math.abs(player.vX) + 1;

        } else if(translateX < WigActivity.twidth * world.length + 1 && player.getX() > mCanvasWidth / 3 * 2 + translateX){

            translateX += Math.abs(player.vX) + 1;

        }

        if(random.nextInt(250) == 1){
            for (int i = 0; i != clouds.length - 1; i++){
                Rect rect = clouds[i];
                if(rect == null) {
                    int y = random.nextInt(mCanvasHeight / 3 * 2);
                    clouds[i] =new Rect(translateX-textures[7].getWidth(), y, translateX, y + textures[7].getHeight());
                    rect = clouds[i];
                }
                if(rect.left > translateX + mCanvasWidth){
                    int y = random.nextInt(mCanvasHeight / 3 * 2);
                    rect.set(translateX-rect.width(), y, translateX, y + rect.height());
                }
            }
        }

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

        } else if((mCanvasWidth * 0.44) < x  && x < (mCanvasWidth * 0.51) && (mCanvasHeight * 0.284) < y && y < (mCanvasHeight * 0.4)){

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
        mMode = mode;
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

    public static Bitmap flip(Bitmap src, int type) {
        Matrix matrix = new Matrix();

        if(type == 0) {
            matrix.preScale(1.0f, -1.0f);
        }
        else if(type == 1) {
            matrix.preScale(-1.0f, 1.0f);
        } else {
            return src;
        }

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public class Player {

        private boolean falling;
        public int moveRight;
        public float vX;
        public float vY;
        private float X;
        private float Y;
        private int width;
        private int height;
        public Rect hitbox;
        public Bitmap player;
        public Bitmap[] walk;
        public Bitmap[] swing;
        private int count;
        private int state;

        public Player(int x, int y, int width, int height, Bitmap texture){

            moveRight = 0;
            state = 0;
            count = 0;
            this.X = x;
            this.Y = y;
            this.width = width;
            this.height = height;
            this.hitbox = new Rect();
            this.hitbox.set((int) X, (int) Y, (int) X + width, (int) Y + height);
            this.player = texture;
            walk = new Bitmap[]{setupBitmap(R.drawable.capnwigglesw1), setupBitmap(R.drawable.capnwigglesw2), setupBitmap(R.drawable.capnwigglesw3)};
            swing = new Bitmap[]{setupBitmap(R.drawable.capnwiggless1), setupBitmap(R.drawable.capnwiggless2), setupBitmap(R.drawable.capnwiggless3)};


        }

        public void update() {

            if(count == 30){
                count = 0;
                state = 0;
            }

            if(state != 2 && Math.abs(vX) < 1){
                state = 0;
            } else if (state == 0){
                state = 1;
                count = 0;
            }

            if(state != 0){
                count++;
            }

            vX += moveRight * 5;

            if(WigActivity.twidth == 0 || WigActivity.theight + 1 == 0) return;

            Tile select =  world[(int) (X + width / 2) / WigActivity.twidth][(int) (Y + height) / WigActivity.theight + 1];

            boolean fall = true;
            boolean fail = false;

            if(vY >= 0){
                if(select.type == 1){
                    if(select.hitbox.top < Y + vY + height + 1){
                        fall = false;
                        vY = 0;
                    }
                }
            }
            if(fall){vY += 1;}

            if(select.y > 1) {
                select = world[select.x][select.y - 2];
                if (vY < 0) {
                    if (select.type == 1) {
                        if (select.hitbox.bottom > Y + vY) {
                            vY = 0;
                        }
                    }
                }
            } else {
                fail = true;
            }
            if(select.x < mCanvasWidth) {
                select = world[select.x + 1][select.y + 1];
                if(fail){
                    select = world[select.x][select.y - 2];
                    fail = false;
                }
                if (vX > 0) {
                    if (select.type == 1) {
                        if (select.hitbox.left < (int)(X + vX + width)) {
                            vX = 0;
                        }
                    }
                }
            } else{
                fail = true;
            }

            if(select.x > 2) {
                select = world[select.x - 2][select.y];
                if(fail) {
                    select = world[select.x + 1][select.x + 1];
                }
                if (vX < 0) {
                    if (select.type == 1) {
                        if (select.hitbox.right < X + vX) {
                            vX = 0;
                        }
                    }
                }
            }

            /*

            if(vY > 0){

                Tile inTile = world[(int) (X + width / 2) / WigActivity.twidth][(int) (Y + height) / WigActivity.theight - 1];

                if(inTile.type == 1){
                    if(inTile.hitbox.top <= Y + vY){
                        Log.e("Y + vY", "" + Y + vY);
                        vY = 0;
                    } else {
                        vY += 1;
                    }
                } else {
                    vY += 1;
                }

            } else {
                vY += 1;
            }

            for (Tile[] tilea: world){

                float firstV = (X + width / 2);
                float secV = (tilea[0].hitbox.left + WigActivity.twidth / 2);
                float thirV = WigActivity.twidth * 2;


                if(Math.abs(firstV - secV) > thirV){
                    continue;
                }

                //Log.i("WigThread.Player.world.tilea.tile.hitbox.right ", "" + (Math.abs(firstV - secV) > thirV));//" " + tilea[0].hitbox.right

                for(Tile tile: tilea){

                    if(Math.abs((Y + height / 2) - (tile.hitbox.top + WigActivity.theight / 2)) > WigActivity.theight * 2){
                        continue;
                    }

                    if(tile.type == 1 && (tile.hitbox.intersect(new Rect((int)(hitbox.left + vX),(int)(hitbox.top + vY + 1), (int)(hitbox.right + vX), (int)(hitbox.bottom + vY + 1))))){

                        //vX = 0;
                        vY = 0;
                        //moveRight = 0;

                    } else {
                        if (hitbox.intersect(tile.hitbox)){
                            vY += mCanvasHeight / 100;
                        }
                    }

                    if(tile.type == 1 && (tile.hitbox.intersect(new Rect((int)(hitbox.left + vX),(int)(hitbox.top), (int)(hitbox.right + vX), (int)(hitbox.bottom))))){

                        vX = 0;
                        moveRight = 0;

                    }

                }
            }*/

            if(X + vX < 1) vX = 0;
            X += vX;
            if(Y + vY < 1) vY = 0;
            Y += vY;
            vX *= 0.5;
            vY *= 0.91;
            hitbox.set((int)X, (int)Y, (int)X + width,(int) Y + height);

        }

        public void draw(){



        }

        public Bitmap getTex(){

            Bitmap dst;
            switch(state){
                case 0:
                    if(vX > 0){
                        return player;
                    } else return flip(player, 1);
                case 1:
                    if(count < 5){
                        dst = walk[0];
                    } else if(count < 10){
                        dst = walk[1];
                    } else{
                        dst = walk[2];
                    }
                    if(vX < 0){
                        return flip(dst, 1);
                    } else return dst;
                case 2:
                    if(count < 5){
                        dst = swing[0];
                    } else if(count < 10){
                        dst = swing[1];
                    } else{
                        dst = swing[2];
                    }
                    if(vX < 0){
                        return flip(dst, 1);
                    } else return dst;
            }

            return null;

        }

        public void destroy(){
            player.recycle();
            player = null;
            hitbox = null;
        }

        public void setX(float x){ this.X = x; }

        public void setY(float y){ this.Y = y; }

        public float getX(){ return X; }

        public float getY(){ return Y; }

    }

}
