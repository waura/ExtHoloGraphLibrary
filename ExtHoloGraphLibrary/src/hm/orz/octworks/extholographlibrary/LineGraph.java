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

package hm.orz.octworks.extholographlibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class LineGraph extends AbstractLineGraph {

    private Paint paint = new Paint();
    private float minY = 0, minX = 0;
    private float maxY = 0, maxX = 0;
    private boolean isRangeSet = false;
    private boolean isDomainSet = false;
    private int lineToFill = -1;


    public LineGraph(Context context) {
        this(context, null);
    }

    public LineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getLineToFill() {
        return lineToFill;
    }

    public void setLineToFill(int indexOfLine) {
        this.lineToFill = indexOfLine;
        update();
    }

    public void setRangeY(float min, float max) {
        minY = min;
        maxY = max;
        isRangeSet = true;
    }

    public void setDomain(float min, float max) {
        minX = min;
        maxX = max;
        isDomainSet = true;
    }

    public float getMaxY() {
        if (isRangeSet) return maxY;

        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                maxY = point.getY();
                break;
            }
        }
        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                if (point.getY() > maxY) maxY = point.getY();
            }
        }
        return maxY;

    }

    public float getMinY() {
        if (isRangeSet) return minY;

        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                minY = point.getY();
                break;
            }
        }
        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                if (point.getY() < minY) minY = point.getY();
            }
        }
        return minY;
    }

    public float getMaxX() {
        if (isDomainSet) return maxX;

        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                maxX = point.getX();
                break;
            }
        }
        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                if (point.getX() > maxX) maxX = point.getX();
            }
        }
        return maxX;

    }

    public float getMinX() {
        if (isDomainSet) return minX;

        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                minX = point.getX();
                break;
            }
        }
        for (Line line : getLines()) {
            for (LinePoint point : line.getPoints()) {
                if (point.getX() < minX) minX = point.getX();
            }
        }
        return minX;
    }

    protected void onPreDrawGraph(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        if (getLines().size() <= 0) {
            return;
        }

        // draw graph area
        paint.reset();
        Path path = new Path();

        int lineCount = 0;
        for (Line line : getLines()) {
            int count = 0;
            float lastXPixels = 0, newYPixels;
            float lastYPixels = 0, newXPixels;

            if (lineCount == lineToFill) {
                paint.setColor(Color.BLACK);
                paint.setAlpha(30);
                paint.setStrokeWidth(2);
                for (int i = (int) convertToPx(5, DP); i - canvas.getWidth() < canvas.getHeight(); i += convertToPx(10, DP)) {
                    canvas.drawLine(i, canvas.getHeight(), 0, canvas.getHeight() - i, paint);
                }

                paint.reset();

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                for (LinePoint p : line.getPoints()) {
                    float yPercent = (p.getY() - getMinY()) / (getMaxY() - getMinY());
                    float xPercent = (p.getX() - getMinX()) / (getMaxX() - getMinX());
                    if (count == 0) {
                        lastXPixels = xPercent * canvas.getWidth();
                        lastYPixels = canvas.getHeight() - (canvas.getHeight() * yPercent);
                        path.moveTo(lastXPixels, lastYPixels);
                    } else {
                        newXPixels = xPercent * canvas.getWidth();
                        newYPixels = canvas.getHeight() - (canvas.getHeight() * yPercent);
                        path.lineTo(newXPixels, newYPixels);
                        Path pa = new Path();
                        pa.moveTo(lastXPixels, lastYPixels);
                        pa.lineTo(newXPixels, newYPixels);
                        pa.lineTo(newXPixels, 0);
                        pa.lineTo(lastXPixels, 0);
                        pa.close();
                        canvas.drawPath(pa, paint);
                        lastXPixels = newXPixels;
                        lastYPixels = newYPixels;
                    }
                    count++;
                }

                path.reset();

                path.moveTo(0, canvas.getHeight() - bottomPadding);
                path.lineTo(leftPadding, canvas.getHeight() - bottomPadding);
                path.lineTo(leftPadding, 0);
                path.lineTo(0, 0);
                path.close();
                canvas.drawPath(path, paint);

                path.reset();

                path.moveTo(canvas.getWidth(), canvas.getHeight() - bottomPadding);
                path.lineTo(canvas.getWidth() - leftPadding, canvas.getHeight() - bottomPadding);
                path.lineTo(canvas.getWidth() - leftPadding, 0);
                path.lineTo(canvas.getWidth(), 0);
                path.close();

                canvas.drawPath(path, paint);
            }

            lineCount++;
        }
    }

    protected void onPostDrawGraph(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
    }
}
