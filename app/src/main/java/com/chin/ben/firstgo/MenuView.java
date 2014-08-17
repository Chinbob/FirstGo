package com.chin.ben.firstgo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuView extends SurfaceView implements SurfaceHolder.Callback {
	private volatile MenuThread thread;

	private SensorEventListener sensorAccelerometer;

	//Handle communication from the GameThread to the View/Activity Thread
	private Handler mHandler;

	//Pointers to the views
	private TextView mWorld;
	private TextView mLevel;
    private TextView mScore;
    private TextView mCoins;


	public MenuView(Context context, AttributeSet attrs) {
		super(context, attrs);

		//Get the holder of the screen and register interest
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		//Set up a handler for messages from GameThread
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message m) {
                switch(m.getData().getInt("msg")){
                    case 0:
                        mLevel.setText(m.getData().getString("level"));
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
 			}
		};
	}
	
	//Used to release any resources.
	public void cleanup() {
		this.thread.setRunning(false);
		this.thread.cleanup();
		
		this.removeCallbacks(thread);
		thread = null;
		
		this.setOnTouchListener(null);
		sensorAccelerometer = null;
		
		SurfaceHolder holder = getHolder();
		holder.removeCallback(this);
	}
	
	/*
	 * Setters and Getters
	 */

	public void setThread(MenuThread newThread) {

		thread = newThread;

		setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if(thread!=null) {
					return thread.onTouch(event);
				}
				else return false;
			}

		});

		this.sensorAccelerometer = new SensorEventListener() {

			public void onAccuracyChanged(Sensor arg0, int arg1) {
				// not needed
			}

			public void onSensorChanged(SensorEvent event) {
				if(thread!=null) {
					if (thread.isAlive()) {
						thread.onSensorChanged(event);
					}
				}
			}
		};

		setClickable(true);
		setFocusable(true);
	}
	
	public MenuThread getThread() {
		return thread;
	}

	public Handler getmHandler() {
		return mHandler;
	}
	
	
	/*
	 * Screen functions
	 */
	
	//ensure that we go into pause state if we go out of focus
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if(thread!=null) {
			if (!hasWindowFocus)
				thread.pause();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {

        //System.exit(0x0);

		if(thread!=null) {
			thread.setRunning(true);
			
			if(thread.getState() == Thread.State.NEW){
				//Just start the new thread
				thread.start();
			}
			else {
				if(thread.getState() == Thread.State.TERMINATED){
					//Start a new thread
					//Should be this to update screen with old game: new GameThread(this, thread);
					//The method should set all fields in new thread to the value of old thread's fields 
					thread = new MenuThread(this);
					thread.setRunning(true);
					thread.start();
				}
			}
		}
	}
	
	//Always called once after surfaceCreated. Tell the GameThread the actual size
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(thread!=null) {
			thread.setSurfaceSize(width, height);			
		}
	}

	/*
	 * Need to stop the GameThread if the surface is destroyed
	 * Remember this doesn't need to happen when app is paused on even stopped.
	 */
	public void surfaceDestroyed(SurfaceHolder arg0) {
		
		boolean retry = true;
		if(thread!=null) {
			thread.setRunning(false);
		}
		
		//join the thread with this thread
		while (retry) {
			try {
				if(thread!=null) {
					thread.join();
				}
				retry = false;
			} 
			catch (InterruptedException e) {

			}
		}
	}
	
	/*
	 * Accelerometer
	 */

	public void startSensor(SensorManager sm) {
		sm.registerListener(this.sensorAccelerometer, 
				sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),	
				SensorManager.SENSOR_DELAY_GAME);
	}
	
	public void removeSensor(SensorManager sm) {
		sm.unregisterListener(this.sensorAccelerometer);
		this.sensorAccelerometer = null;
	}

    public TextView getmWorld() {
        return mWorld;
    }

    public void setmWorld(TextView mWorld) {
        this.mWorld = mWorld;
    }

    public TextView getmLevel() {
        return mLevel;
    }

    public void setmLevel(TextView mLevel) {
        this.mLevel = mLevel;
    }

    public TextView getmScore() {
        return mScore;
    }

    public void setmScore(TextView mScore) {
        this.mScore = mScore;
    }

    public TextView getmCoins() {
        return mCoins;
    }

    public void setmCoins(TextView mCoins) {
        this.mCoins = mCoins;
    }
}


