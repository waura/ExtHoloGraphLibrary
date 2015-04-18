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
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class LineGraph extends AbstractLineGraph {
    private ArrayList<Line> lines = new ArrayList<Line>();
    private Paint paint = new Paint();
    private float minY = 0, minX = 0;
    private float maxY = 0, maxX = 0;
    private boolean isRangeSet = false;
    private boolean isDomainSet = false;
    private int lineToFill = -1;
    private int indexSelected = -1;
    private OnPointClickedListener listener;
    private int gridColor = 0xffffffff;


    boolean debug = false;

    public LineGraph(Context context) {
        this(context, null);
    }

    public LineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGridColor(int color) {
        gridColor = color;
    }


    public void removeAllLines() {
        while (lines.size() > 0) {
            lines.remove(0);
        }
        update();
    }

    public void addLine(Line line) {
        lines.add(line);
        update();
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Line> lines) {
        this.lines = lines;
    }

    public int getLineToFill() {
        return lineToFill;
    }

    public void setLineToFill(int indexOfLine) {
        this.lineToFill = indexOfLine;
        update();
    }

    public Line getLine(int index) {
        return lines.get(index);
    }

    public int getSize() {
        return lines.size();
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

        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                maxY = point.getY();
                break;
            }
        }
        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                if (point.getY() > maxY) maxY = point.getY();
            }
        }
        return maxY;

    }

    public float getMinY() {
        if (isRangeSet) return minY;

        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                minY = point.getY();
                break;
            }
        }
        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                if (point.getY() < minY) minY = point.getY();
            }
        }
        return minY;
    }

    public float getMaxX() {
        if (isDomainSet) return maxX;

        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                maxX = point.getX();
                break;
            }
        }
        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                if (point.getX() > maxX) maxX = point.getX();
            }
        }
        return maxX;

    }

    public float getMinX() {
        if (isDomainSet) return minX;

        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                minX = point.getX();
                break;
            }
        }
        for (Line line : lines) {
            for (LinePoint point : line.getPoints()) {
                if (point.getX() < minX) minX = point.getX();
            }
        }
        return minX;
    }

    protected void drawGraphArea(Canvas ca) {
        float topPadding = 0, bottomPadding = 0;
        float leftPadding = convertToPx(6, DP), rightPadding = convertToPx(6, DP);
        float usableHeight = ca.getHeight() - bottomPadding - topPadding;
        float usableWidth = ca.getWidth() - leftPadding - rightPadding;

        paint.reset();
        Path path = new Path();

        int lineCount = 0;
        for (Line line : lines) {
            int count = 0;
            float lastXPixels = 0, newYPixels;
            float lastYPixels = 0, newXPixels;

            if (lineCount == lineToFill) {
                paint.setColor(Color.BLACK);
                paint.setAlpha(30);
                paint.setStrokeWidth(2);
                for (int i = (int) convertToPx(5, DP); i - ca.getWidth() < ca.getHeight(); i += convertToPx(10, DP)) {
                    ca.drawLine(i, ca.getHeight(), 0, ca.getHeight() - i, paint);
                }

                paint.reset();

                paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
                for (LinePoint p : line.getPoints()) {
                    float yPercent = (p.getY() - getMinY()) / (getMaxY() - getMinY());
                    float xPercent = (p.getX() - getMinX()) / (getMaxX() - getMinX());
                    if (count == 0) {
                        lastXPixels = xPercent * ca.getWidth();
                        lastYPixels = ca.getHeight() - (ca.getHeight() * yPercent);
                        path.moveTo(lastXPixels, lastYPixels);
                    } else {
                        newXPixels = xPercent * ca.getWidth();
                        newYPixels = ca.getHeight() - (ca.getHeight() * yPercent);
                        path.lineTo(newXPixels, newYPixels);
                        Path pa = new Path();
                        pa.moveTo(lastXPixels, lastYPixels);
                        pa.lineTo(newXPixels, newYPixels);
                        pa.lineTo(newXPixels, 0);
                        pa.lineTo(lastXPixels, 0);
                        pa.close();
                        ca.drawPath(pa, paint);
                        lastXPixels = newXPixels;
                        lastYPixels = newYPixels;
                    }
                    count++;
                }

                path.reset();

                path.moveTo(0, ca.getHeight() - bottomPadding);
                path.lineTo(leftPadding, ca.getHeight() - bottomPadding);
                path.lineTo(leftPadding, 0);
                path.lineTo(0, 0);
                path.close();
                ca.drawPath(path, paint);

                path.reset();

                path.moveTo(ca.getWidth(), ca.getHeight() - bottomPadding);
                path.lineTo(ca.getWidth() - leftPadding, ca.getHeight() - bottomPadding);
                path.lineTo(ca.getWidth() - leftPadding, 0);
                path.lineTo(ca.getWidth(), 0);
                path.close();

                ca.drawPath(path, paint);
            }

            lineCount++;
        }

        paint.reset();

        paint.setColor(this.gridColor);
        paint.setAlpha(50);
        paint.setAntiAlias(true);
        ca.drawLine(leftPadding, ca.getHeight() - bottomPadding, ca.getWidth(), ca.getHeight() - bottomPadding, paint);
        paint.setAlpha(255);

        Rect drawRange = new Rect(
                (int) leftPadding,
                (int) topPadding,
                (int) (leftPadding + usableWidth),
                (int) (topPadding + usableHeight));
        for (Line line : lines) {
            drawLine(ca, drawRange, line);
        }

        /* TODO:
        for (Line line : lines) {
            paint.setStrokeWidth(convertToPx(6, DP));
            paint.setStrokeCap(Paint.Cap.ROUND);

            if (line.isShowingPoints()) {
                LinePoint p = line.getPoint(indexSelected);

                if (indexSelected == pointCount && listener != null) {
                    paint.setColor(Color.parseColor("#33B5E5"));
                    paint.setAlpha(100);
                    ca.drawPath(p.getPath(), paint);
                    paint.setAlpha(255);
                }

            }
        }
        */
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int count = 0;
        int lineCount = 0;
        int pointCount;

        Region r = new Region();
        for (Line line : lines) {
            pointCount = 0;
            for (LinePoint p : line.getPoints()) {
                if (p.isOnPoint(point.x, point.y)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        indexSelected = count;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (listener != null) {
                            listener.onClick(lineCount, pointCount);
                        }
                        indexSelected = -1;
                    }
                }

                pointCount++;
                count++;
            }
            lineCount++;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            update();
        }

        return true;
    }

    public void setOnPointClickedListener(OnPointClickedListener listener) {
        this.listener = listener;
    }

public interface OnPointClickedListener {
    abstract void onClick(int lineIndex, int pointIndex);
}
}
