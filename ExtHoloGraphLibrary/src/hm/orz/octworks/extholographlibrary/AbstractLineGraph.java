package hm.orz.octworks.extholographlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public abstract class AbstractLineGraph extends Graph {

    private static final int UNSELECTED = -1;

    private static final int GRAPH_STROKE_WIDTH = 3;
    private static final int GRID_STROKE_WIDTH = 1;
    private static final int POINT_DIAMETER = 6;
    private static final int POINT_CLICK_DIAMETER = 30;

    private ArrayList<Line> lines = new ArrayList<Line>();

    private Paint paint = new Paint();
    private Paint txtPaint = new Paint();
    private Paint numPaint = new Paint();

    private Bitmap fullImage;

    private boolean shouldUpdate = false;

    private int selectedLineIndex = UNSELECTED;
    private int selectedPointIndex = UNSELECTED;

    private String xAxisTitle = null;
    private String yAxisTitle = null;

    private ArrayList<Integer> xGridList = new ArrayList<Integer>();
    private ArrayList<Integer> yGridList = new ArrayList<Integer>();

    private boolean showYAxisValues = true;
    private boolean showXAxisValues = true;

    private int gridColor = 0xffffffff;

    private OnPointClickedListener listener;

    public interface OnPointClickedListener {
        abstract void onClick(int lineIndex, int pointIndex);
    }

    public AbstractLineGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        txtPaint.setColor(0xdd000000);
        txtPaint.setTextSize(convertToPx(20, SP));
        numPaint.setColor(0xdd000000);
        numPaint.setTextSize(convertToPx(16, SP));
    }

    public void update() {
        shouldUpdate = true;
        postInvalidate();
    }

    public abstract float getMaxY();

    public abstract float getMinY();

    public abstract float getMaxX();

    public abstract float getMinX();

    public void showXAxisValues(boolean show) {
        showXAxisValues = show;
    }

    public void showYAxisValues(boolean show) {
        showYAxisValues = show;
    }

    public void setTextColor(int color) {
        txtPaint.setColor(color);
    }

    public void setTextSize(float s) {
        txtPaint.setTextSize(s);
    }

    public void setYAxisTitle(String title) {
        yAxisTitle = title;
    }

    public void setXAxisTitle(String title) {
        xAxisTitle = title;
    }

    public void setXGrid(ArrayList<Integer> gridList) {
        this.xGridList = gridList;
    }

    public void setYGrid(ArrayList<Integer> gridList) {
        this.yGridList = gridList;
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

    public Line getLine(int index) {
        return lines.get(index);
    }

    public int getSize() {
        return lines.size();
    }

    public void setOnPointClickedListener(OnPointClickedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        int lineCount = 0;
        int pointCount;

        for (Line line : lines) {
            pointCount = 0;
            for (LinePoint p : line.getPoints()) {
                if (p.isOnPoint(point.x, point.y)) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        selectedLineIndex = lineCount;
                        selectedPointIndex = pointCount;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (listener != null) {
                            listener.onClick(lineCount, pointCount);
                        }

                        selectedLineIndex = UNSELECTED;
                        selectedPointIndex = UNSELECTED;
                    }
                }
                pointCount++;
            }
            lineCount++;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
            update();
        }

        return true;
    }

    public void onDraw(Canvas ca) {
        if (fullImage == null || shouldUpdate) {
            fullImage = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(fullImage);

            float topPadding = 0, bottomPadding = 0, leftPadding = 0, rightPadding = 0;
            if (showXAxisValues || showYAxisValues) {
                topPadding = numPaint.measureText(getMaxY() + "") / 2;
                rightPadding = numPaint.measureText(getMaxX() + "") / 2;
                leftPadding = numPaint.getTextSize() * 2f;
                bottomPadding = numPaint.getTextSize() * 2f;
            }

            onPreDrawGraph(canvas, topPadding, bottomPadding, leftPadding, rightPadding);

            if (showXAxisValues) {
                drawXAxisValues(canvas, topPadding, bottomPadding, leftPadding, rightPadding);
            }

            if (showYAxisValues) {
                drawYAxisValues(canvas, topPadding, bottomPadding, leftPadding, rightPadding);
            }

            drawHorizontalGrid(canvas, topPadding, bottomPadding, leftPadding, rightPadding);

            for (Line line : lines) {
                drawLine(canvas, line, topPadding, bottomPadding, leftPadding, rightPadding);
            }

            // draw select marker
            {
                if (selectedLineIndex != UNSELECTED) {
                    Line selectedLine = lines.get(selectedLineIndex);
                    if (selectedLine != null) {
                        LinePoint selectedPoint = selectedLine.getPoint(selectedPointIndex);
                        drawPointSelectedMark(ca, selectedPoint);
                    }
                }
            }

            onPostDrawGraph(canvas, topPadding, bottomPadding, leftPadding, rightPadding);

            shouldUpdate = false;
        }

        Matrix m = new Matrix();
        if (xAxisTitle != null) {
            drawXAxisTitle(ca, xAxisTitle);
            m.preScale(1, (getHeight() - txtPaint.getTextSize()) / getHeight());
        }

        if (yAxisTitle != null) {
            drawYAxisTitle(ca, yAxisTitle);
            m.postTranslate(txtPaint.getTextSize(), 0);
            m.preScale((getWidth() - txtPaint.getTextSize()) / getWidth(), 1);
        }

        ca.drawBitmap(fullImage, m, null);
    }

    protected abstract void onPreDrawGraph(Canvas canvas,
                                           float topPadding,
                                           float bottomPadding,
                                           float leftPadding,
                                           float rightPadding);

    protected abstract void onPostDrawGraph(Canvas canvas,
                                            float topPadding,
                                            float bottomPadding,
                                            float leftPadding,
                                            float rightPadding);

    protected float convertToRealXFromVirtualX(float virtualX, Canvas canvas, float leftPadding, float rightPadding) {
        float usableWidth = canvas.getWidth() - leftPadding - rightPadding;
        float xPercent = (virtualX - getMinX()) / (getMaxX() - getMinX());
        return (leftPadding + (xPercent * usableWidth));
    }

    protected float convertToRealYFromVirtualY(float virtualY, Canvas canvas, float topPadding, float bottomPadding) {
        float usableHeight = canvas.getHeight() - topPadding - bottomPadding;
        float yPercent = (virtualY - getMinY()) / (getMaxY() - getMinY());
        return (canvas.getHeight() - bottomPadding - (usableHeight * yPercent));
    }

    private void drawPointSelectedMark(Canvas canvas, LinePoint point) {
        paint.setStrokeWidth(convertToPx(6, DP));
        paint.setStrokeCap(Paint.Cap.ROUND);

        paint.setColor(Color.parseColor("#33B5E5"));
        paint.setAlpha(100);
        canvas.drawPath(point.getPath(), paint);
        paint.setAlpha(255);
    }

    private void drawHorizontalGrid(Canvas canvas,
                                    float topPadding,
                                    float bottomPadding,
                                    float leftPadding,
                                    float rightPadding) {
        float usableHeight = canvas.getHeight() - topPadding - bottomPadding;
        float height = getMaxY() - getMinY();

        paint.reset();
        paint.setColor(this.gridColor);
        paint.setStrokeWidth(convertToPx(GRID_STROKE_WIDTH, DP));
        paint.setAlpha(50);
        paint.setAntiAlias(true);

        for (Integer gridValue : yGridList) {
            float pos = usableHeight * (gridValue / height);

            canvas.drawLine(
                    leftPadding,
                    canvas.getHeight() - (pos + bottomPadding),
                    canvas.getWidth() - rightPadding,
                    canvas.getHeight() - (pos + bottomPadding),
                    paint);
        }
    }

    private void drawXAxisValues(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        float usableWidth = getWidth() - leftPadding - rightPadding;

        int minSize = (int) convertToPx(50, DP);
        float width = getMaxX() - getMinX();

        // Draw the x axis
        for (Integer gridValue : xGridList) {
            String num = gridValue.toString();

            // Find the proper position for the text
            float pos = usableWidth * (gridValue / width);
            // Add padding for the y axis
            pos += leftPadding;
            // Center text
            pos -= numPaint.measureText(num) / 2;

            // Draw text
            canvas.drawText(num, pos, getHeight() - numPaint.getTextSize() / 3, numPaint);
        }
    }

    private void drawYAxisValues(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        float usableHeight = canvas.getHeight() - bottomPadding - topPadding;

        int minSize = (int) convertToPx(50, DP);
        float height = getMaxY() - getMinY();

        // Draw the y axis
        for (Integer gridValue : yGridList) {
            String num = gridValue.toString();

            // Find the proper position for the text
            float posY = usableHeight * (gridValue / height);
            posY = canvas.getHeight() - (posY + bottomPadding);

            // Center text
            float posX = numPaint.getTextSize() - numPaint.measureText(num) / 2;
            posY += numPaint.measureText(num) / 2;

            // Draw text
            canvas.drawText(num, posX, posY, numPaint);
        }
    }

    private void drawXAxisTitle(Canvas canvas, String xAxisTitle) {
        canvas.drawText(xAxisTitle, (getWidth() - txtPaint.measureText(xAxisTitle)) / 2, getHeight() - txtPaint.getTextSize() / 3, txtPaint);
    }

    private void drawYAxisTitle(Canvas canvas, String yAxisTitle) {
        canvas.save();
        canvas.rotate(-90, getWidth() / 2, getHeight() / 2);
        canvas.translate(0, getHeight() / 2);
        canvas.translate(0, -getWidth() / 2);
        canvas.drawText(yAxisTitle, (getWidth() - txtPaint.measureText(yAxisTitle)) / 2, txtPaint.getTextSize() * 4 / 5, txtPaint);
        canvas.restore();
    }

    protected void drawLine(Canvas canvas, Line line, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        drawSimpleLine(canvas, line, topPadding, bottomPadding, leftPadding, rightPadding);
        if (line.isShowingPoints()) {
            for (int i = 0; i < line.getSize(); i++) {
                drawPoint(canvas, line.getPoint(i), line.getColor(), topPadding, bottomPadding, leftPadding, rightPadding);
            }
        }
    }

    private void drawSimpleLine(Canvas canvas, Line line, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        if (line.getSize() <= 1) {
            return;
        }

        LinePoint lastPoint, newPoint;

        paint.reset();
        paint.setColor(line.getColor());
        paint.setStrokeWidth(convertToPx(GRAPH_STROKE_WIDTH, DP));
        paint.setAlpha(255);
        paint.setTextAlign(Paint.Align.CENTER);

        {
            lastPoint = line.getPoint(0);
        }

        for (int i = 1; i < line.getSize(); i++) {
            newPoint = line.getPoint(i);

            canvas.drawLine(
                    convertToRealXFromVirtualX(lastPoint.getX(), canvas, leftPadding, rightPadding),
                    convertToRealYFromVirtualY(lastPoint.getY(), canvas, topPadding, bottomPadding),
                    convertToRealXFromVirtualX(newPoint.getX(), canvas, leftPadding, rightPadding),
                    convertToRealYFromVirtualY(newPoint.getY(), canvas, topPadding, bottomPadding),
                    paint);

            lastPoint = newPoint;
        }
    }

    private void drawPoint(Canvas canvas, LinePoint point, int color, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        int pointCount = 0;

        paint.reset();
        paint.setStrokeWidth(convertToPx(GRAPH_STROKE_WIDTH, DP));
        paint.setStrokeCap(Paint.Cap.ROUND);

        float xPixels = convertToRealXFromVirtualX(point.getX(), canvas, leftPadding, rightPadding);
        float yPixels = convertToRealYFromVirtualY(point.getY(), canvas, topPadding, bottomPadding);

        paint.setColor(color);
        canvas.drawCircle(xPixels, yPixels, convertToPx(POINT_DIAMETER, DP), paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(xPixels, yPixels, convertToPx(POINT_DIAMETER / 2, DP), paint);

        Path path2 = new Path();
        path2.addCircle(xPixels, yPixels, convertToPx(POINT_CLICK_DIAMETER, DP), Path.Direction.CW);
        point.setPath(path2);
        point.setRegion(new Region(
                (int) (xPixels - convertToPx(POINT_CLICK_DIAMETER, DP)),
                (int) (yPixels - convertToPx(POINT_CLICK_DIAMETER, DP)),
                (int) (xPixels + convertToPx(POINT_CLICK_DIAMETER, DP)),
                (int) (yPixels + convertToPx(POINT_CLICK_DIAMETER, DP))));
    }

    private float getGraphPaddingTop() {
        if (showXAxisValues || showYAxisValues) {
            return numPaint.measureText(getMaxY() + "") / 2;
        }
        return 0.0f;
    }

    private float getGraphPaddingBottom() {
        if (showXAxisValues || showYAxisValues) {
            return numPaint.getTextSize() * 2f;
        }
        return 0.0f;
    }

    private float getGraphPaddingLeft() {
        if (showXAxisValues || showYAxisValues) {
            return numPaint.getTextSize() * 2f;
        }
        return 0.0f;
    }

    private float getGraphPaddingRight() {
        if (showXAxisValues || showYAxisValues) {
            return numPaint.measureText(getMaxX() + "") / 2;
        }
        return 0.0f;
    }

}

