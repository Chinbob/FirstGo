package com.chin.ben.firstgo;

//Other parts of the android libraries that we use
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import java.util.Random;
import android.graphics.Rect;

public class TheGame extends GameThread{

    private Random random = new Random();

    private Bitmap dplay;
    private Bitmap dbird;
    private Bitmap dcloud;
    private Bitmap dfish;
    private Bitmap dgoompa;
    private float goompaExt = 0;
    private boolean goompaUp = true;
    private Rect goompa = new Rect();

    private boolean[] intropropFace = {true, true, true, true, true};
    private float[] intropropX = {(float) (mCanvasWidth * 0.5875),0,0,mCanvasWidth / 2,(float) (mCanvasWidth * 0.8475) + 3, (float) (mCanvasWidth * 0.4625)};
    private float[] intropropY = {(float) (mCanvasHeight * 0.263333),(float) (mCanvasHeight * 0.75),0,50,(float) (mCanvasHeight * 0.4666666), (float) (mCanvasHeight * 0.673333333)};

	//This is run before anything else, so we can prepare things here
	public TheGame(GameView gameView) {
		//House keeping
		super(gameView);
    
        dbird = setupBitmap(R.drawable.bird);
        dcloud = setupBitmap(R.drawable.tcloud);
        dfish = setupBitmap(R.drawable.fish);
        dgoompa = setupBitmap(R.drawable.mob);
        dplay = setupBitmap(R.drawable.play);

	}

    public Bitmap setupBitmap(int path){

        return BitmapFactory.decodeResource(mGameView.getContext().getResources(), path);

    }

	//This is run before a new game (also after an old game)
	@Override
	public void setupBeginning() {

        intropropX[0] = (float) (mCanvasWidth * 0.5875);
        intropropY[0] = (float) (mCanvasHeight * 0.263333);

        intropropY[1] = (float) (mCanvasHeight * 0.75);
        intropropX[1] = (float) (0);

        intropropX[2] = 0;
        intropropY[2] = 0;

        intropropX[3] = mCanvasWidth / 2;
        intropropY[3] = 50;

        intropropX[4] = (float) (mCanvasWidth * 0.8475) + 3;//339
        intropropY[4] = (float) (mCanvasHeight * 0.4666666);//140

        intropropX[5] = (float) (mCanvasWidth * 0.4625); //185
        intropropY[5] = (float) (mCanvasHeight * 0.673333333); //202

        goompa.set((int)(intropropX[4]), (int)(intropropY[4]), (int)(intropropX[4] + dgoompa.getWidth()),(int) (mCanvasHeight * 0.58333333));
        
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

	@Override
	protected void doDraw(Canvas canvas) {
		//If there isn't a canvas to do nothing
		//It is ok not understanding what is happening here
		if(canvas == null) return;

		//House keeping
		super.doDraw(canvas);

		//canvas.drawBitmap(bitmap, x, y, paint) uses top/left corner of bitmap as 0,0 
		//we use 0,0 in the middle of the bitmap, so negate half of the width and height of the ball to draw the ball as expected
		//A paint of null means that we will use the image without any extra features (called Paint)

        canvas.drawBitmap(dbird, intropropX[0], intropropY[0], null);
        canvas.drawBitmap(dfish, intropropX[1], intropropY[1], null);
        canvas.drawBitmap(dcloud, intropropX[2], intropropY[2], null);
        canvas.drawBitmap(dcloud, intropropX[3], intropropY[3], null);
        canvas.drawBitmap(dgoompa, null, goompa, null);
        canvas.drawBitmap(dplay, intropropX[5], intropropY[5], null);

    }

	//This is run whenever the phone is touched by the user
	@Override
	protected void actionOnTouch(float x, float y) {
		
	}
	
	//This is run whenever the phone moves around its axises 
	@Override
	protected void actionWhenPhoneMoved(float xDirection, float yDirection, float zDirection) {

	}


    //This is run just before the game "scenario" is printed on the screen
    @Override
    protected void updateGame(float secondsElapsed) {

        float baseMove = mCanvasWidth / 400;

        if(baseMove == 0) {
            baseMove = 0.25f;
        }

        if(random.nextInt(50) == 1){
            dbird = flip(dbird, 1);
            intropropFace[0] = !intropropFace[0];
        }

        if(intropropFace[1] && intropropX[1] <= 3){
            intropropFace[1] = !intropropFace[1];
            dfish = flip(dfish, 1);
        } else if(!intropropFace[1] && intropropX[1] + dbird.getWidth() + 3 >= mCanvasWidth){
            intropropFace[1] = !intropropFace[1];
            dfish = flip(dfish, 1);
        } else if(random.nextInt(250) == 1){
            intropropFace[1] = !intropropFace[1];
            dfish = flip(dfish, 1);
        }

        intropropX[1] += intropropFace[1] ? -(baseMove * 2) : baseMove * 2;

        if(intropropX[2] > mCanvasWidth){
            intropropX[2] = -dcloud.getWidth();
        }
        if(intropropX[3] > mCanvasWidth){
            intropropX[3] = -dcloud.getWidth();
        }

        intropropX[2]+= baseMove;
        intropropX[3]+= baseMove;

        if(intropropFace[4] && intropropX[4] <= mCanvasWidth * 0.8475){
            intropropFace[4] = !intropropFace[4];
            dgoompa = flip(dgoompa, 1);
        } else if(!intropropFace[4] && intropropX[4] + dgoompa.getWidth() + 2 >= mCanvasWidth){
            intropropFace[4] = !intropropFace[4];
            dgoompa = flip(dgoompa, 1);
        }

        if(goompaUp && goompaExt >= 10){
            goompaUp = false;
        } else if(!goompaUp && goompaExt <= 0){
            goompaUp = true;
        }
        goompaExt += (goompaUp ? 1 : -1);
        goompa.top = (int) (intropropY[4] + goompaExt);
        /*
        if((intropropFace[4] && intropropX[4] <= (mCanvasWidth * 0.8475)) || !intropropFace[4] && intropropX[4] >= mCanvasWidth){
            intropropFace[4] = !intropropFace[4];
            dgoompa = flip(dgoompa, 0);
        }*/

        intropropX[4] += (intropropFace[4] ? -( baseMove ) : baseMove );

        goompa.left += (intropropFace[4] ? -( baseMove ) :   baseMove );
        goompa.right += (intropropFace[4] ? -( baseMove ) :   baseMove );
        
        }
}


