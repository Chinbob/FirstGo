package com.chin.ben.firstgo;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Mob {

    public float vX;
    public float vY;
    private float x;
    private float y;
    private float width;
    private float height;
	public double health;
	public boolean faceRight;
	public Bitmap image;
	public Rect hitbox;
	public int state = WigThread.random.nextInt(2), count = WigThread.random.nextInt(51);
	
	public Mob(float x, float y, float width, float height, Bitmap image) {
		this.x = x;
        this.y = y;
        this.vX = 0;
        this.vY = 0;
        this.width = width;
        this.height = height;
		this.hitbox = new Rect((int)x,(int)y,(int)(x + width),(int)(y + height));
		this.image = image;

	}

	public void draw() {
		
	}	
	
	public void update(int delta){
		x = (x + vX * delta);
		y = (y + vY * delta);
		hitbox.offsetTo((int)x, (int) y);
	}

    public void setLoc(float x, float y){
        this.x = x;
        this.y = y;
        hitbox.offsetTo((int)x, (int) y);
    }

    public void setWidth(float width){
        this.width = width;
        hitbox.inset((int)((hitbox.right - hitbox.left) - width), 0);
    }

    public void setHeight(float height){
        this.height = height;
        hitbox.inset(0, (int)((hitbox.bottom - hitbox.top) - height));
    }
	
}
