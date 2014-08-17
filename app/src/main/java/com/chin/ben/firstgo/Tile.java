package com.chin.ben.firstgo;

import android.graphics.Rect;

public class Tile {

    public int state;
    public int count;
	public int x;
	public int y;
	public int type;
	public Rect hitbox;
	
	public Tile(int x, int y, int type, int width, int height){
		this.x = x;
		this.y = y;
		this.type = type;
		this.hitbox = new Rect();
        this.hitbox.set(x * width,y * height,x * width + width, y * height + height);

        if(this.type == 1){
            state = 0;
        }

	}
	
}
