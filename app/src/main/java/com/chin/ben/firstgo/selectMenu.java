package com.chin.ben.firstgo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.chin.ben.firstgo.R;

public class selectMenu extends Activity {

    public static final String SLevel = "com.chin.ben.firstgo.SL";

    public static int Level = -1;
    public static int World = -1;
    public static int Score = -1;
    public static int Coins = -1;

    public MenuView menuView;
    public MenuThread menuThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_select_menu);

        menuView = (MenuView) findViewById(R.id.myview);
        menuView.setmCoins((TextView)findViewById(R.id.coins));
        menuView.setmWorld((TextView) findViewById(R.id.world));
        menuView.setmLevel((TextView) findViewById(R.id.level));
        menuView.setmScore((TextView) findViewById(R.id.score));

        this.startGame();

    }

    public void startGame(){

        menuThread = new MenuThread(menuView);
        menuView.setThread(menuThread);
        menuThread.setState(GameThread.STATE_READY);
        menuView.startSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE));

    }

    protected void onStop() {

        super.onStop();

        finish();

    }

    protected void onDestroy() {
        super.onDestroy();

        menuView.cleanup();
        menuView.removeSensor((SensorManager)getSystemService(Context.SENSOR_SERVICE));
        menuThread = null;
        menuView = null;

        //System.exit(0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);



    }

    public void sendMessage(View view){

        if(Level == -1) return;

        Intent intent = new Intent(this, WigActivity.class);

        intent.putExtra(SLevel, World == 0 ? Integer.toString(0 - Level) : Integer.toString(Level));

        this.startActivity(intent);

    }


}
