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
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class StackedLineGraph extends AbstractLineGraph {

    private class DrawPoint {
        public float x;
        public float y;

        private Path path;
        private Region region;

        public DrawPoint(float argX, float argY) {
            x = argX;
            y = argY;
        }

        public Region getRegion() {
            return region;
        }

        public void setRegion(Region region) {
            this.region = region;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public boolean isOnPoint(int x, int y) {
            if (getPath() != null && getRegion() != null) {
                region.setPath(getPath(), getRegion());
                if (region.contains(x, y)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class DrawLine {
        private ArrayList<DrawPoint> points = new ArrayList<DrawPoint>();

        private int color;

        public void addPoint(DrawPoint point) {
            points.add(point);
        }

        public DrawPoint getPoint(int index) {
            return points.get(index);
        }

        public int getNumOfPoint() {
            return this.points.size();
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    private class DrawStackedLineGraph {
        private ArrayList<DrawLine> lines = new ArrayList<DrawLine>();

        public DrawStackedLineGraph(int numOfLines) {
            for (int i = 0; i < numOfLines; i++) {
                lines.add(new DrawLine());
            }
        }

        public DrawLine getLine(int index) {
            return lines.get(index);
        }

        public int getNumOfLine() {
            return lines.size();
        }

        public int getNumOfPoint() {
            if (getNumOfLine() <= 0) {
                return 0;
            }
            return lines.get(0).getNumOfPoint();
        }

        public boolean isOnPoint(int pointIndex, int x, int y) {
            for (int i = 0; i < getNumOfLine(); i++) {
                DrawLine line = lines.get(i);
                DrawPoint point = line.getPoint(pointIndex);
                if (point.isOnPoint(x, y)) {
                    return true;
                }
            }
            return false;
        }
    }

    private StackedLine drawLine = null;
    private DrawStackedLineGraph drawStackedLineGraph = null;
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

    protected void drawGraphArea(Canvas ca) {
        float topPadding = 0, bottomPadding = 0;
        float leftPadding = convertToPx(6, DP), rightPadding = convertToPx(6, DP);

        paint.reset();

        drawStackedLineGraph = createDrawStackedLineGraph(ca, drawLine, topPadding, bottomPadding, leftPadding, rightPadding);

        if (this.showHorizontalGrid) {
            drawHorizontalGrid(ca, topPadding, bottomPadding, leftPadding, rightPadding);
        }

        drawStackedLineGraphArea(ca, topPadding, bottomPadding, leftPadding, rightPadding, drawStackedLineGraph);

        for (int i = 0; i < drawStackedLineGraph.getNumOfLine(); i++) {
            drawLine(ca, drawStackedLineGraph.getLine(i));
        }

        if (drawLine.isShowingPoints()) {
            for (int i = 0; i < drawStackedLineGraph.getNumOfLine(); i++) {
                drawPoints(ca, drawStackedLineGraph.getLine(i));
            }
        }

    }

    private DrawStackedLineGraph createDrawStackedLineGraph(Canvas canvas,
                                                            StackedLine stackedLine,
                                                            float topPadding,
                                                            float bottomPadding,
                                                            float leftPadding,
                                                            float rightPadding) {
        int numOfLines = stackedLine.getNumOfLines();
        if (numOfLines <= 0) {
            return null;
        }

        DrawStackedLineGraph drawStackedLineGraph = new DrawStackedLineGraph(stackedLine.getNumOfLines());

        for (int i = 0; i < stackedLine.getNumOfPoints(); i++) {
            float stackedValue = 0.0f;
            StackedLinePoint point = stackedLine.getPoint(i);
            for (int j = 0; j < numOfLines; j++) {
                Float value = point.getValue(j);
                if (value != null) {
                    stackedValue += value;
                }

                DrawLine line = drawStackedLineGraph.getLine(j);
                line.addPoint(createDrawPoint(canvas, i, stackedValue, topPadding, bottomPadding, leftPadding, rightPadding));
            }
        }

        for (int i = 0; i < stackedLine.getNumOfLines(); i++) {
            DrawLine line = drawStackedLineGraph.getLine(i);
            line.setColor(stackedLine.getColor(i));
        }

        return drawStackedLineGraph;
    }

    private DrawPoint createDrawPoint(Canvas canvas,
                                      float virtualX,
                                      float virtualY,
                                      float topPadding,
                                      float bottomPadding,
                                      float leftPadding,
                                      float rightPadding) {
        return new DrawPoint(
                convertToRealXFromVirtualX(virtualX, canvas, leftPadding, rightPadding),
                convertToRealYFromVirtualY(virtualY, canvas, topPadding, bottomPadding));
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
            DrawStackedLineGraph drawStackedLineGraph) {

        if (drawStackedLineGraph.getNumOfLine() <= 0) {
            return;
        }

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAlpha(100);

        Path path = new Path();
        DrawPoint p = null;
        DrawLine nowLine = null;
        DrawLine lastLine = null;

        nowLine = drawStackedLineGraph.getLine(0);
        if (nowLine.getNumOfPoint() > 0) {
            paint.setColor(nowLine.getColor());
            paint.setAlpha(100);

            path.moveTo(
                    nowLine.getPoint(0).x,
                    convertToRealYFromVirtualY(getMinY(), canvas, topPadding, bottomPadding));
            for (int j = 0; j < nowLine.getNumOfPoint(); j++) {
                path.lineTo(nowLine.getPoint(j).x, nowLine.getPoint(j).y);
            }
            path.lineTo(
                    nowLine.getPoint(nowLine.getNumOfPoint() - 1).x,
                    convertToRealYFromVirtualY(getMinY(), canvas, topPadding, bottomPadding));
            path.moveTo(
                    nowLine.getPoint(0).x,
                    convertToRealYFromVirtualY(getMinY(), canvas, topPadding, bottomPadding));
            canvas.drawPath(path, paint);
        }
        lastLine = nowLine;

        for (int i = 1; i < drawStackedLineGraph.getNumOfLine(); i++) {
            path.reset();

            nowLine = drawStackedLineGraph.getLine(i);
            if (nowLine.points.size() > 0) {
                paint.setColor(nowLine.getColor());
                paint.setAlpha(100);
                path.moveTo(nowLine.getPoint(0).x, nowLine.getPoint(0).y);
                for (int j = 1; j < nowLine.getNumOfPoint(); j++) {
                    path.lineTo(nowLine.getPoint(j).x, nowLine.getPoint(j).y);
                }
                for (int j = lastLine.getNumOfPoint() - 1; j >= 0; j--) {
                    path.lineTo(lastLine.getPoint(j).x, lastLine.getPoint(j).y);
                }
                path.lineTo(nowLine.getPoint(0).x, nowLine.getPoint(0).y);
                canvas.drawPath(path, paint);
            }
            lastLine = nowLine;
        }
    }

    private void drawLine(Canvas canvas, DrawLine line) {

        if (line.getNumOfPoint() <= 1) {
            return;
        }

        float lastXPixels = 0, newYPixels;
        float lastYPixels = 0, newXPixels;

        paint.setColor(line.getColor());
        paint.setStrokeWidth(6);
        paint.setAlpha(255);
        paint.setTextAlign(Align.CENTER);

        {
            lastXPixels = line.getPoint(0).x;
            lastYPixels = line.getPoint(0).y;
        }

        for (int i = 1; i < line.getNumOfPoint(); i++) {
            newXPixels = line.getPoint(i).x;
            newYPixels = line.getPoint(i).y;

            canvas.drawLine(lastXPixels, lastYPixels, newXPixels, newYPixels, paint);

            lastXPixels = newXPixels;
            lastYPixels = newYPixels;
        }
    }

    private void drawPoints(Canvas canvas, DrawLine line) {
        int pointCount = 0;

        paint.setStrokeWidth(6);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (DrawPoint point : line.points) {
            float xPixels = point.x;
            float yPixels = point.y;

            paint.setColor(Color.GRAY);
            canvas.drawCircle(xPixels, yPixels, convertToPx(6, DP), paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(xPixels, yPixels, convertToPx(3, DP), paint);

            Path path2 = new Path();
            path2.addCircle(xPixels, yPixels, convertToPx(30, DP), Direction.CW);
            point.setPath(path2);
            point.setRegion(new Region((int) (xPixels - convertToPx(30, DP)), (int) (yPixels - convertToPx(30, DP)), (int) (xPixels + convertToPx(30, DP)), (int) (yPixels + convertToPx(30, DP))));

            if (indexSelected == pointCount && listener != null) {
                paint.setColor(Color.parseColor("#33B5E5"));
                paint.setAlpha(100);
                canvas.drawPath(point.getPath(), paint);
                paint.setAlpha(255);
            }

            pointCount++;
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        Region r = new Region();
        for (int i = 0; i < drawStackedLineGraph.getNumOfPoint(); i++) {
            if (drawStackedLineGraph.isOnPoint(i, point.x, point.y) && event.getAction() == MotionEvent.ACTION_DOWN) {
                indexSelected = i;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (drawStackedLineGraph.isOnPoint(i, point.x, point.y) && listener != null) {
                    listener.onClick(i);
                }
                indexSelected = -1;
            }
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
        abstract void onClick(int index);
    }
}
