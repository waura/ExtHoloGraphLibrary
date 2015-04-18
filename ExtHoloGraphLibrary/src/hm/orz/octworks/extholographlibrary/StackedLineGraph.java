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
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class StackedLineGraph extends AbstractLineGraph {

    private StackedLine drawLine = null;
    ArrayList<Line> drawStackedLines = new ArrayList<Line>();
    private Paint paint = new Paint();
    private float minY = 0, minX = 0;
    private float maxY = 0, maxX = 0;
    private boolean isRangeSet = false;
    private boolean isDomainSet = false;
    private int lineToFill = -1;
    private int indexSelected = -1;
    private OnPointClickedListener listener;
    private int gridColor = 0xffffffff;
    private boolean showHorizontalGrid = false;

    public interface OnPointClickedListener {
        abstract void onClick(int lineIndex, int pointIndex);
    }

    public StackedLineGraph(Context context) {
        this(context, null);
    }

    public StackedLineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGridColor(int color) {
        gridColor = color;
    }

    public void showHorizontalGrid(boolean show) {
        showHorizontalGrid = show;
    }

    public void removeAllLines() {
        drawLine = null;
        update();
    }

    public void setLine(StackedLine line) {
        drawLine = line;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int count = 0;
        int lineCount = 0;
        int pointCount;

        Region r = new Region();
        for (int i = 0; i < drawStackedLines.size(); i++) {
            Line line = drawStackedLines.get(i);

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


    protected void drawGraphArea(Canvas ca) {
        float topPadding = 0, bottomPadding = 0;
        float leftPadding = convertToPx(6, DP), rightPadding = convertToPx(6, DP);
        float usableHeight = ca.getHeight() - bottomPadding - topPadding;
        float usableWidth = ca.getWidth() - leftPadding - rightPadding;

        paint.reset();

        drawStackedLines = createDrawStackedLineGraph(drawLine);

        if (this.showHorizontalGrid) {
            drawHorizontalGrid(ca, topPadding, bottomPadding, leftPadding, rightPadding);
        }

        drawStackedLineGraphArea(ca, topPadding, bottomPadding, leftPadding, rightPadding, drawStackedLines);

        Rect drawRange = new Rect(
                (int) leftPadding,
                (int) topPadding,
                (int) (leftPadding + usableWidth),
                (int) (topPadding + usableHeight));
        for (int i = 0; i < drawStackedLines.size(); i++) {
            drawLine(ca, drawRange, drawStackedLines.get(i));
        }
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

    private void drawHorizontalGrid(Canvas canvas,
                                    float topPadding,
                                    float bottomPadding,
                                    float leftPadding,
                                    float rightPadding) {
        float lineSpace = (canvas.getHeight() - bottomPadding - topPadding) / 10;

        paint.setColor(this.gridColor);
        paint.setAlpha(50);
        paint.setAntiAlias(true);
        canvas.drawLine(leftPadding, canvas.getHeight() - bottomPadding, canvas.getWidth(), canvas.getHeight() - bottomPadding, paint);

        for (int i = 1; i <= 10; i++) {
            canvas.drawLine(
                    leftPadding,
                    canvas.getHeight() - bottomPadding - (i * lineSpace),
                    canvas.getWidth(),
                    canvas.getHeight() - bottomPadding - (i * lineSpace),
                    paint);
        }
    }

    private void drawStackedLineGraphArea(
            Canvas canvas,
            float topPadding,
            float bottomPadding,
            float leftPadding,
            float rightPadding,
            ArrayList<Line> drawStackedLines) {

        if (drawStackedLines.size() <= 0) {
            return;
        }

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAlpha(100);

        Path path = new Path();
        LinePoint p = null;
        Line nowLine = null;
        Line lastLine = null;

        nowLine = drawStackedLines.get(0);
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

        for (int i = 1; i < drawStackedLines.size(); i++) {
            path.reset();

            nowLine = drawStackedLines.get(i);
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

    private float convertToRealXFromVirtualX(float virtualX, Canvas canvas, float leftPadding, float rightPadding) {
        float usableWidth = canvas.getWidth() - leftPadding - rightPadding;
        float xPercent = (virtualX - getMinX()) / (getMaxX() - getMinX());
        return (leftPadding + (xPercent * usableWidth));
    }

    private float convertToRealYFromVirtualY(float virtualY, Canvas canvas, float topPadding, float bottomPadding) {
        float usableHeight = canvas.getHeight() - topPadding - bottomPadding;
        float yPercent = (virtualY - getMinY()) / (getMaxY() - getMinY());
        return (canvas.getHeight() - bottomPadding - (usableHeight * yPercent));
    }
}
