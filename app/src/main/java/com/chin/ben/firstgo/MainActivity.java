package com.chin.ben.firstgo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int MENU_RESUME = 1;
    private static final int MENU_START = 2;
    private static final int MENU_STOP = 3;

    private GameThread mGameThread;
    private GameView mGameView;

    private static MainActivity mthis;

    public final static String EXTRA_MESSAGE = "com.chin.firstgo.MESSAGE";
	
	/** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.mthis = this;
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.requestWindowFeature(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        
        setContentView(R.layout.activity_main);
        
        mGameView = (GameView)findViewById(R.id.gamearea);
        mGameView.setStatusView((TextView)findViewById(R.id.text));
        mGameView.setScoreView((TextView)findViewById(R.id.score));
              	
        this.startGame(mGameView, null, savedInstanceState);
    }

    private void startGame(GameView gView, GameThread gThread, Bundle savedInstanceState) {    	

    	//Set up a new game, we don't care about previous states
    	mGameThread = new TheGame(mGameView);
    	mGameView.setThread(mGameThread);
    	mGameThread.setState(GameThread.STATE_READY);
    	mGameView.startSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE));

        //mGameThread.doStart();
    }

	/*
	 * Activity state functions
	 */
	
    @Override
    protected void onPause() {
        super.onPause();
        
        if(mGameThread.getMode() == GameThread.STATE_RUNNING) {
        	mGameThread.setState(GameThread.STATE_PAUSE);
        }
    }

    protected void onResume(){
        super.onResume();

        if(mGameThread.getMode() == GameThread.STATE_PAUSE){
            mGameThread.setState(GameThread.STATE_RUNNING);
        }

    }

    protected void onRestart(){

        super.onRestart();

        mGameView.cleanup();
        mGameView.removeSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        mGameThread = null;
        mGameView = null;

    }

    
    @Override
	protected void onDestroy() {
		super.onDestroy();
    	
    	mGameView.cleanup();
        mGameView.removeSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        mGameThread = null;
        mGameView = null;

        //System.exit(0);

	}

    protected void onStop() {

        super.onStop();



        //finish();

    }
    
    /*
     * UI Functions
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, MENU_START, 0, R.string.menu_start);
        menu.add(0, MENU_STOP, 0, R.string.menu_stop);
        menu.add(0, MENU_RESUME, 0, R.string.menu_resume);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_START:
                mGameThread.doStart();
                return true;
            case MENU_STOP:    			
    			mGameThread.setState(GameThread.STATE_LOSE,  getText(R.string.message_stopped));
                return true;
            case MENU_RESUME:
                mGameThread.unpause();
                return true;
        }

        return false;
    }

	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing if nothing is selected
	}

    public static void sendMessage(){

        //System.exit(0);

        Intent intent = new Intent(mthis, selectMenu.class);

        intent.putExtra(EXTRA_MESSAGE, "");

        mthis.startActivity(intent);

    }

}

