package com.chin.ben.firstgo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class WigView extends SurfaceView implements SurfaceHolder.Callback {
	private volatile WigThread thread;

	private SensorEventListener sensorAccelerometer;

	//Handle communication from the GameThread to the View/Activity Thread
	private Handler mHandler;

	//Pointers to the views
    private TextView loading;
    private TextView score;
    private TextView goompas;

	public WigView(Context context, AttributeSet attrs) {
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
                        loading.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        score.setText(m.getData().getString("coins"));
                        break;
                    case 2:
                        goompas.setText(m.getData().getString("goompa"));
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

	public void setThread(WigThread newThread) {

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
	
	public WigThread getThread() {
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
			if (!hasWindowFocus) {
                thread.pause();
            } else if(thread.mMode != GameThread.STATE_RUNNING){
                thread.setState(GameThread.STATE_RUNNING);
            }
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {

		if(thread!=null) {
			thread.setRunning(true);
			
			if(thread.getState() == Thread.State.NEW){
				//Just start the new thread
                thread.setupBeginning();
                thread.world = IOHandle.getLevel(getResources().openRawResource(R.raw.l0));
				thread.start();
			}
			else {
				if(thread.getState() == Thread.State.TERMINATED){
					//Start a new thread
					//Should be this to update screen with old game: new GameThread(this, thread);
					//The method should set all fields in new thread to the value of old thread's fields 
					thread = new WigThread(this);
					thread.setRunning(true);
                    thread.setupBeginning();
                    thread.world = IOHandle.getLevel(getResources().openRawResource(R.raw.l0));
					thread.start();
				}
			}
		}
	}
	
	//Always called once after surfaceCreated. Tell the GameThread the actual size
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(thread!=null) {
			thread.setSurfaceSize(width, height);
            thread.world = IOHandle.getLevel(getResources().openRawResource(R.raw.l0));
            thread.setupBeginning();
		}
        Log.d("WigView.surfaceChanged", "Surface size has been set.");
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

    public TextView getLoading() {
        return loading;
    }

    public void setLoading(TextView loading) {
        this.loading = loading;
    }

    public void setScore(TextView score) {
        this.score = score;
    }

    public void setGoompas(TextView goompas) {
        this.goompas = goompas;
    }

    /*
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    WindowManager wm = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    setMeasuredDimension(display.getWidth(), display.getHeight());
    }
    */

}


