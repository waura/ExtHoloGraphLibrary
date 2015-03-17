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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
import android.view.View;

import java.util.ArrayList;

public class StackedLineGraph extends View {

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
        public ArrayList<DrawPoint> points = new ArrayList<DrawPoint>();
    }

    private class DrawStackedLineGraph {
        public ArrayList<DrawLine> lines = new ArrayList<DrawLine>();

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
            return lines.get(0).points.size();
        }

        public boolean isOnPoint(int pointIndex, int x, int y) {
            for (int i = 0; i < getNumOfLine(); i++) {
                DrawLine line = lines.get(i);
                DrawPoint point = line.points.get(pointIndex);
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
    private Paint txtPaint = new Paint();
    private float minY = 0, minX = 0;
    private float maxY = 0, maxX = 0;
    private boolean isMaxYUserSet = false;
    private int lineToFill = -1;
    private int indexSelected = -1;
    private OnPointClickedListener listener;
    private Bitmap fullImage;
    private boolean shouldUpdate = false;
    private boolean showMinAndMax = false;
    private boolean showHorizontalGrid = false;
    private int gridColor = 0xffffffff;
    private int labelSize = 10;

    public StackedLineGraph(Context context) {
        this(context, null);
    }

    public StackedLineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        txtPaint.setColor(0xffffffff);
        txtPaint.setTextSize(20);
        txtPaint.setAntiAlias(true);
    }

    public void setGridColor(int color) {
        gridColor = color;
    }

    public void showHorizontalGrid(boolean show) {
        showHorizontalGrid = show;
    }

    public void showMinAndMaxValues(boolean show) {
        showMinAndMax = show;
    }

    public void setTextColor(int color) {
        txtPaint.setColor(color);
    }

    public void setTextSize(float s) {
        txtPaint.setTextSize(s);
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public void update() {
        shouldUpdate = true;
        postInvalidate();
    }

    public void removeAllLines() {
        drawLine = null;
        shouldUpdate = true;
        postInvalidate();
    }

    public void setLine(StackedLine line) {
        drawLine = line;
        shouldUpdate = true;
        postInvalidate();
    }

    public StackedLine getLine() {
        return drawLine;
    }

    public void setLineToFill(int indexOfLine) {
        this.lineToFill = indexOfLine;
        shouldUpdate = true;
        postInvalidate();
    }

    public int getLineToFill() {
        return lineToFill;
    }

    public void setRangeY(float min, float max) {
        minY = min;
        maxY = max;
        isMaxYUserSet = true;
    }

    public float getMaxY() {
        if (isMaxYUserSet) {
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
        if (isMaxYUserSet) {
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

    public void onDraw(Canvas ca) {
        if (fullImage == null || shouldUpdate) {
            fullImage = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(fullImage);
            String max = (int) maxY + "";// used to display max
            String min = (int) minY + "";// used to display min
            paint.reset();
            Path path = new Path();

            float bottomPadding = 10, topPadding = 10;
            float sidePadding = 10;
            if (this.showMinAndMax)
                sidePadding = txtPaint.measureText(max);
            if (labelSize > bottomPadding) {
                bottomPadding = labelSize;
            }

            drawStackedLineGraph = createDrawStackedLineGraph(drawLine, topPadding, bottomPadding, sidePadding);

            paint.reset();

            if (this.showHorizontalGrid) {
                drawHorizontalGrid(canvas, topPadding, bottomPadding, sidePadding);
            }

            drawStackedLineGraphArea(canvas, topPadding, bottomPadding, sidePadding, drawStackedLineGraph);

            for (int i = 0; i < drawStackedLineGraph.getNumOfLine(); i++) {
                drawLine(canvas, drawStackedLineGraph.getLine(i));
            }

            if (drawLine.isShowingPoints()) {
                for (int i = 0; i < drawStackedLineGraph.getNumOfLine(); i++) {
                    drawPoints(canvas, drawStackedLineGraph.getLine(i));
                }
            }

            shouldUpdate = false;
            if (this.showMinAndMax) {
                ca.drawText(max, 0, txtPaint.getTextSize(), txtPaint);
                ca.drawText(min, 0, this.getHeight(), txtPaint);
            }
        }

        ca.drawBitmap(fullImage, 0, 0, null);
    }

    private DrawStackedLineGraph createDrawStackedLineGraph(StackedLine stackedLine, float topPadding, float bottomPadding, float sidePadding) {
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
                line.points.add(createDrawPoint(i, stackedValue, topPadding, bottomPadding, sidePadding));
            }
        }
        return drawStackedLineGraph;
    }

    private DrawPoint createDrawPoint(float virtualX, float virtualY, float topPadding, float bottomPadding, float sidePadding) {
        return new DrawPoint(
                convertToRealXFromVirtualX(virtualX, sidePadding),
                convertToRealYFromVirtualY(virtualY, topPadding, bottomPadding));
    }

    private float convertToRealXFromVirtualX(float virtualX, float sidePadding) {
        float usableWidth = getWidth() - sidePadding * 2;
        float xPercent = (virtualX - getMinX()) / (getMaxX() - getMinX());
        return (sidePadding + (xPercent * usableWidth));
    }

    private float convertToRealYFromVirtualY(float virtualY, float topPadding, float bottomPadding) {
        float usableHeight = getHeight() - topPadding - bottomPadding;
        float yPercent = (virtualY - getMinY()) / (getMaxY() - getMinY());
        return (getHeight() - bottomPadding - (usableHeight * yPercent));
    }

    private void drawHorizontalGrid(Canvas canvas, float topPadding, float bottomPadding, float sidePadding) {
        float lineSpace = (getHeight() - bottomPadding - topPadding) / 10;

        paint.setColor(this.gridColor);
        paint.setAlpha(50);
        paint.setAntiAlias(true);
        canvas.drawLine(sidePadding, getHeight() - bottomPadding, getWidth(), getHeight() - bottomPadding, paint);

        for (int i = 1; i <= 10; i++) {
            canvas.drawLine(sidePadding, getHeight() - bottomPadding - (i * lineSpace), getWidth(), getHeight() - bottomPadding - (i * lineSpace), paint);
        }
    }

    private void drawStackedLineGraphArea(Canvas canvas, float topPadding, float bottomPadding, float sidePadding, DrawStackedLineGraph drawStackedLineGraph) {
        if (drawStackedLineGraph.getNumOfLine() <= 0) {
            return;
        }

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(drawLine.getColor());
        paint.setAlpha(100);

        Path path = new Path();
        DrawPoint p = null;
        DrawLine nowLine = null;
        DrawLine lastLine = null;

        nowLine = drawStackedLineGraph.getLine(0);
        if (nowLine.points.size() > 0) {
            path.moveTo(
                    nowLine.points.get(0).x,
                    convertToRealYFromVirtualY(getMinY(), topPadding, bottomPadding));
            for (int j = 0; j < nowLine.points.size(); j++) {
                path.lineTo(nowLine.points.get(j).x, nowLine.points.get(j).y);
            }
            path.lineTo(
                    nowLine.points.get(nowLine.points.size() - 1).x,
                    convertToRealYFromVirtualY(getMinY(), topPadding, bottomPadding));
            path.moveTo(
                    nowLine.points.get(0).x,
                    convertToRealYFromVirtualY(getMinY(), topPadding, bottomPadding));
        }
        lastLine = nowLine;

        for (int i = 1; i < drawStackedLineGraph.getNumOfLine(); i++) {
            nowLine = drawStackedLineGraph.getLine(i);
            if (nowLine.points.size() > 0) {
                path.moveTo(nowLine.points.get(0).x, nowLine.points.get(0).y);
                for (int j = 1; j < nowLine.points.size(); j++) {
                    path.lineTo(nowLine.points.get(j).x, nowLine.points.get(j).y);
                }
                for (int j = lastLine.points.size() - 1; j >= 0; j--) {
                    path.lineTo(lastLine.points.get(j).x, lastLine.points.get(j).y);
                }
                path.lineTo(nowLine.points.get(0).x, nowLine.points.get(0).y);
            }
            lastLine = nowLine;
        }
        canvas.drawPath(path, paint);
    }

    private void drawLine(Canvas canvas, DrawLine line) {

        if (line.points.size() <= 1) {
            return;
        }

        float lastXPixels = 0, newYPixels;
        float lastYPixels = 0, newXPixels;
        float maxY = getMaxY();
        float minY = getMinY();
        float maxX = getMaxX();
        float minX = getMinX();

        paint.setColor(drawLine.getColor());
        paint.setStrokeWidth(6);
        paint.setAlpha(255);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(labelSize);


        {
            lastXPixels = line.points.get(0).x;
            lastYPixels = line.points.get(0).y;
        }

        for (int i = 1; i < line.points.size(); i++) {
            newXPixels = line.points.get(i).x;
            newYPixels = line.points.get(i).y;

            canvas.drawLine(lastXPixels, lastYPixels, newXPixels, newYPixels, paint);

            lastXPixels = newXPixels;
            lastYPixels = newYPixels;

            /*
            if (point.getLabel_string() != null) {
                canvas.drawText(point.getLabel_string(), lastXPixels, usableHeight + bottomPadding, paint);
            }
            */
        }
    }

    private void drawPoints(Canvas canvas, DrawLine line) {
        int pointCount = 0;

        float maxY = getMaxY();
        float minY = getMinY();
        float maxX = getMaxX();
        float minX = getMinX();

        paint.setColor(drawLine.getColor());
        paint.setStrokeWidth(6);
        paint.setStrokeCap(Paint.Cap.ROUND);

        for (DrawPoint point : line.points) {
            float xPixels = point.x;
            float yPixels = point.y;

            paint.setColor(Color.GRAY);
            canvas.drawCircle(xPixels, yPixels, 10, paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(xPixels, yPixels, 5, paint);

            Path path2 = new Path();
            path2.addCircle(xPixels, yPixels, 30, Direction.CW);
            point.setPath(path2);
            point.setRegion(new Region((int) (xPixels - 30), (int) (yPixels - 30), (int) (xPixels + 30), (int) (yPixels + 30)));

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
            shouldUpdate = true;
            postInvalidate();
        }

        return true;
    }

    public void setOnPointClickedListener(OnPointClickedListener listener) {
        this.listener = listener;
    }

    public int getLabelSize() {
        return labelSize;
    }

    public void setLabelSize(int labelSize) {
        this.labelSize = labelSize;
    }

    public interface OnPointClickedListener {
        abstract void onClick(int index);
    }
}
