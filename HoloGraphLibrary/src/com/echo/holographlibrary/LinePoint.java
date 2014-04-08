/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 * 
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.echo.holographlibrary;

import android.graphics.Path;
import android.graphics.Region;

public class LinePoint {
	private float mX = 0;
	private float mY = 0;
	private Path mPath;
	private Region mRegion;
    private int mColor = 0xFF000000;
    private int mSelectedColor = 0x8033B5E5;

    public LinePoint(){
    }

	public LinePoint(double x, double y){
		this.mX = (float)x;
		this.mY = (float)y;
	}
	public LinePoint(float x, float y){
		this.mX = x;
		this.mY = y;
	}
	public float getX() {
		return mX;
	}
	public void setX(float x) {
		this.mX = x;
	}
	public float getY() {
		return mY;
	}
	public void setY(float y) {
		this.mY = y;
	}
	
	public void setX(double x){
		this.mX = (float) x;
	}
	
	public void setY(double y){
		this.mY = (float) y;
	}
	public Region getRegion() {
		return mRegion;
	}
	public void setRegion(Region region) {
		this.mRegion = region;
	}
	public Path getPath() {
		return mPath;
	}
	public void setPath(Path path) {
		this.mPath = path;
	}
	
	@Override
	public String toString(){
		return "x= " + mX + ", y= " + mY;
	}

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }
}
