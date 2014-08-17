package com.chin.ben.firstgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chin.ben.firstgo.R;

public class WigActivity extends Activity {

    public static WigThread hey;

    public static int twidth;
    public static int theight;

    public WigView wigView;
    public WigThread wigThread;

    public ImageButton rbutton;
    public ImageButton lbutton;
    public ImageButton jbutton;
    public Chronometer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.e("Start", "WigActivity.onCreate()");

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_wig);

        lbutton = (ImageButton) findViewById(R.id.leftb);
        rbutton = (ImageButton) findViewById(R.id.rightb);
        jbutton = (ImageButton) findViewById(R.id.jumpb);
        wigView = (WigView) findViewById(R.id.wigarea);
        timer = (Chronometer) findViewById(R.id.chronometer2);
        wigView.setLoading((TextView) findViewById(R.id.Loading));
        wigView.setScore((TextView)findViewById(R.id.gcoins));
        wigView.setGoompas((TextView)findViewById(R.id.goompas));

        lbutton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        wigThread.player.moveRight = -1;
                        break;
                    case MotionEvent.ACTION_UP:
                        wigThread.player.moveRight = 0;
                        break;
                    default:
                        break;
                }

                return false;
            }

        });

        rbutton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        wigThread.player.moveRight = 1;
                        break;
                    case MotionEvent.ACTION_UP:
                        wigThread.player.moveRight = 0;
                        break;
                    default:
                        break;
                }

                return false;
            }

        });

        jbutton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch(motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        wigThread.player.vY -= wigThread.mCanvasHeight / 20;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    default:
                        break;
                }

                return false;
            }

        });

        this.startGame();

    }

    public void startGame(){

        wigThread = new WigThread(wigView);
        wigThread.bob = this;
        hey = wigThread;
        wigView.setThread(wigThread);
        wigThread.setState(GameThread.STATE_RUNNING);
        wigView.startSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        timer.start();

    }

    protected  void onPause(){
        super.onPause();

        if(wigThread.mMode == WigThread.STATE_RUNNING){
            wigThread.setState(WigThread.STATE_PAUSE);
        }

    }

    protected void onResume() {
        super.onResume();

        if(wigThread.mMode == WigThread.STATE_PAUSE){
            wigThread.setState(WigThread.STATE_RUNNING);
        }

    }


    protected void onStop() {

        super.onStop();

        finish();

    }

    protected void onDestroy() {
        super.onDestroy();

        wigView.cleanup();
        wigView.removeSensor((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        timer.stop();
        timer = null;
        wigThread = null;
        wigView = null;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);

        menu.add(0, 0, 0, R.string.menu_start);
        menu.add(0, 1, 0, R.string.menu_stop);
        menu.add(0, 2, 0, R.string.menu_resume);
        menu.add(0, 3, 0, "PAUSE");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                wigThread.doStart();
                return true;
            case 1:
                wigThread.setState(GameThread.STATE_LOSE,  "LOST");
                return true;
            case 2:
                wigThread.unpause();
                return true;
            case 3:
                wigThread.pause();
                return true;
        }

        return false;
    }

    public void toggleChVis(View view){

        timer.setVisibility(timer.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);

    }

}
