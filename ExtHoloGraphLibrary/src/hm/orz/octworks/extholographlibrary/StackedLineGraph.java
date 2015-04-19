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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class StackedLineGraph extends AbstractLineGraph {

    private StackedLine drawLine = null;
    private Paint paint = new Paint();
    private float minY = 0, minX = 0;
    private float maxY = 0, maxX = 0;
    private boolean isRangeSet = false;
    private boolean isDomainSet = false;
    private int lineToFill = -1;
    private OnPointClickedListener listener;
    private boolean showHorizontalGrid = false;

    public StackedLineGraph(Context context) {
        this(context, null);
    }

    public StackedLineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void showHorizontalGrid(boolean show) {
        showHorizontalGrid = show;
    }

    public void removeAllLines() {
        drawLine = null;
        super.removeAllLines();
        update();
    }

    public void setLine(StackedLine line) {
        drawLine = line;
        for (Line l : createDrawStackedLineGraph(drawLine)) {
            addLine(l);
        }
        update();
    }

    public StackedLine getLine() {
        return drawLine;
    }

    public void setLineToFill(int indexOfLine) {
        this.lineToFill = indexOfLine;
        update();
    }

    public int getLineToFill() {
        return lineToFill;
    }

    public void setRangeY(float min, float max) {
        minY = min;
        maxY = max;
        isRangeSet = true;
    }

    public float getMaxY() {
        if (isRangeSet) {
            return maxY;
        } else {
            float max = 0.0f;
            for (StackedLinePoint point : drawLine.getPoints()) {
                if (point.getTotalValue() > max) {
                    max = point.getTotalValue();
                }
            }
            maxY = max;
            return maxY;
        }
    }

    public float getMinY() {
        if (isRangeSet) {
            return minY;
        } else {
            float min = 0.0f;
            for (StackedLinePoint point : drawLine.getPoints()) {
                if (point.getTotalValue() < min) {
                    min = point.getTotalValue();
                }
            }
            minY = min;
            return minY;
        }
    }

    public float getMaxX() {
        if (drawLine.getNumOfPoints() > 0) {
            maxX = drawLine.getNumOfPoints() - 1;
        } else {
            maxX = 0.0f;
        }
        return maxX;

    }

    public float getMinX() {
        minX = 0.0f;
        return minX;
    }

    protected void onPreDrawGraph(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        drawStackedLineGraphArea(canvas, topPadding, bottomPadding, leftPadding, rightPadding);
    }

    protected void onPostDrawGraph(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
    }

    private ArrayList<Line> createDrawStackedLineGraph(StackedLine stackedLine) {
        int numOfLines = stackedLine.getNumOfLines();
        if (numOfLines <= 0) {
            return null;
        }

        ArrayList<Line> drawStackedLines = new ArrayList<Line>();
        for (int i = 0; i <= stackedLine.getNumOfLines(); i++) {
            drawStackedLines.add(new Line());
        }

        for (int i = 0; i < stackedLine.getNumOfPoints(); i++) {
            float stackedValue = 0.0f;
            StackedLinePoint point = stackedLine.getPoint(i);
            for (int j = 0; j < numOfLines; j++) {
                Float value = point.getValue(j);
                if (value != null) {
                    stackedValue += value;
                }

                Line line = drawStackedLines.get(j);
                line.addPoint(new LinePoint(i, stackedValue));
            }
        }

        for (int i = 0; i < stackedLine.getNumOfLines(); i++) {
            Line line = drawStackedLines.get(i);
            line.setColor(stackedLine.getColor(i));
        }

        return drawStackedLines;
    }



    private void drawStackedLineGraphArea(
            Canvas canvas,
            float topPadding,
            float bottomPadding,
            float leftPadding,
            float rightPadding) {

        if (getLines().size() <= 0) {
            return;
        }

        paint.reset();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAlpha(100);

        Path path = new Path();
        LinePoint p = null;
        Line nowLine = null;
        Line lastLine = null;

        nowLine = getLines().get(0);
        if (nowLine.getSize() > 0) {
            paint.setColor(nowLine.getColor());
            paint.setAlpha(100);

            path.moveTo(
                    convertToRealXFromVirtualX(nowLine.getPoint(0).getX(), canvas, leftPadding, rightPadding),
                    convertToRealYFromVirtualY(getMinY(), canvas, topPadding, bottomPadding));
            for (int j = 0; j < nowLine.getSize(); j++) {
                path.lineTo(
                        convertToRealXFromVirtualX(nowLine.getPoint(j).getX(), canvas, leftPadding, rightPadding),
                        convertToRealYFromVirtualY(nowLine.getPoint(j).getY(), canvas, topPadding, bottomPadding));
            }
            path.lineTo(
                    convertToRealXFromVirtualX(nowLine.getPoint(nowLine.getSize() - 1).getX(), canvas, leftPadding, rightPadding),
                    convertToRealYFromVirtualY(getMinY(), canvas, topPadding, bottomPadding));
            path.moveTo(
                    convertToRealXFromVirtualX(nowLine.getPoint(0).getX(), canvas, leftPadding, rightPadding),
                    convertToRealYFromVirtualY(getMinY(), canvas, topPadding, bottomPadding));
            canvas.drawPath(path, paint);
        }
        lastLine = nowLine;

        for (int i = 1; i < getLines().size(); i++) {
            path.reset();

            nowLine = getLines().get(i);
            if (nowLine.getSize() > 0) {
                paint.setColor(nowLine.getColor());
                paint.setAlpha(100);
                path.moveTo(
                        convertToRealXFromVirtualX(nowLine.getPoint(0).getX(), canvas, leftPadding, rightPadding),
                        convertToRealYFromVirtualY(nowLine.getPoint(0).getY(), canvas, topPadding, bottomPadding));
                for (int j = 1; j < nowLine.getSize(); j++) {
                    path.lineTo(
                            convertToRealXFromVirtualX(nowLine.getPoint(j).getX(), canvas, leftPadding, rightPadding),
                            convertToRealYFromVirtualY(nowLine.getPoint(j).getY(), canvas, topPadding, bottomPadding));
                }
                for (int j = lastLine.getSize() - 1; j >= 0; j--) {
                    path.lineTo(
                            convertToRealXFromVirtualX(lastLine.getPoint(j).getX(), canvas, leftPadding, rightPadding),
                            convertToRealYFromVirtualY(lastLine.getPoint(j).getY(), canvas, topPadding, bottomPadding));
                }
                path.lineTo(
                        convertToRealXFromVirtualX(nowLine.getPoint(0).getX(), canvas, leftPadding, rightPadding),
                        convertToRealYFromVirtualY(nowLine.getPoint(0).getY(), canvas, leftPadding, bottomPadding));
                canvas.drawPath(path, paint);
            }
            lastLine = nowLine;
        }
    }
}
