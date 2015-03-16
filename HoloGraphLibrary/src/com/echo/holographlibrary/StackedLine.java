package com.echo.holographlibrary;

import java.util.ArrayList;
import java.util.Stack;

public class StackedLine {
    private ArrayList<StackedLinePoint> points = new ArrayList<StackedLinePoint>();
    private int color;
    private boolean showPoints = true;


    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public ArrayList<StackedLinePoint> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<StackedLinePoint> points) {
        this.points = points;
    }

    public void addPoint(StackedLinePoint point) {
        points.add(point);
    }

    public StackedLinePoint getPoint(int index) {
        return points.get(index);
    }

    public int getNumOfPoints() {
        return points.size();
    }

    public int getNumOfLines() {
        int maxStackedPoint = 0;
        for (StackedLinePoint point : points) {
            if (maxStackedPoint < point.getSize()) {
                maxStackedPoint = point.getSize();
            }
        }
        return maxStackedPoint;
    }

    public boolean isShowingPoints() {
        return showPoints;
    }

    public void setShowingPoints(boolean showPoints) {
        this.showPoints = showPoints;
    }


}
