package hm.orz.octworks.extholographlibrary;

import java.util.ArrayList;
import java.util.Stack;

public class StackedLine {
    private ArrayList<StackedLinePoint> points = new ArrayList<StackedLinePoint>();
    private ArrayList<Integer> colors = new ArrayList<Integer>();
    private boolean showPoints = true;


    public Integer getColor(int indexOfLine) {
        return colors.get(indexOfLine);
    }

    public void setColor(int indexOfLine, int color) {
        this.colors.add(indexOfLine, color);
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

    public StackedLinePoint getPoint(int indexOfLine) {
        return points.get(indexOfLine);
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
