package hm.orz.octworks.extholographlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.util.AttributeSet;

import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;

public abstract class AbstractLineGraph extends Graph {

    private Paint txtPaint = new Paint();
    private Paint numPaint = new Paint();

    private Bitmap fullImage;

    private boolean shouldUpdate = false;

    private String xAxisTitle = null;
    private String yAxisTitle = null;
    private boolean showYAxisValues = true;
    private boolean showXAxisValues = true;

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

            float usableHeight = getHeight() - bottomPadding - topPadding;
            float usableWidth = getWidth() - leftPadding - rightPadding;
            Bitmap graphAreaImage = Bitmap.createBitmap((int)usableWidth, (int)usableHeight, Bitmap.Config.ARGB_8888);
            Canvas graphAreaCanvas = new Canvas(graphAreaImage);
            drawGraphArea(graphAreaCanvas);

            if (showXAxisValues) {
                drawXAxisValues(canvas, topPadding, bottomPadding, leftPadding, rightPadding);
            }

            if (showYAxisValues) {
                drawYAxisValues(canvas, topPadding, bottomPadding, leftPadding, rightPadding);
            }

            canvas.drawBitmap(graphAreaImage, leftPadding, topPadding, null);
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

    protected abstract void drawGraphArea(Canvas ca);

    private void drawXAxisValues(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        float usableWidth = getWidth() - leftPadding - rightPadding;

        int minSize = (int) convertToPx(50, DP);

        // Find unique integers to display on the x axis
        List<Integer> values = new LinkedList<Integer>();
        int prevNum = Integer.MIN_VALUE;
        int numbersToShow = (int) usableWidth / minSize + 1;
        float step = (getMaxX() - getMinX()) / (numbersToShow - 1);
        for (int i = 0; i < numbersToShow; i++) {
            int num = (int) (getMinX() + i * step);
            if (num != prevNum) {
                values.add(num);
            }
            prevNum = num;
        }

        // Draw the x axis
        for (int i = 0; i < values.size(); i++) {
            String num = values.get(i).toString();

            // Find the proper position for the text
            float pos = i * usableWidth / (values.size() - 1);
            // Add padding for the y axis
            pos += leftPadding;
            // Center text
            pos -= numPaint.measureText(num) / 2;

            // Draw text
            canvas.drawText(num, pos, getHeight() - numPaint.getTextSize() / 3, numPaint);
        }
    }

    private void drawYAxisValues(Canvas canvas, float topPadding, float bottomPadding, float leftPadding, float rightPadding) {
        float usableHeight = getHeight() - bottomPadding - topPadding;
        float usableWidth = getWidth() - leftPadding - rightPadding;

        int minSize = (int) convertToPx(50, DP);

        // Rotate the canvas for the y axis
        canvas.save();
        canvas.rotate(-90, getWidth() / 2, getHeight() / 2);
        canvas.translate(0, getHeight() / 2);
        canvas.translate(0, -getWidth() / 2);
        canvas.translate(-getHeight() / 2, 0);
        canvas.translate(getWidth() / 2, 0);

        // Find unique integers to display on the y axis
        List<Integer> values = new LinkedList<Integer>();
        int prevNum = Integer.MIN_VALUE;
        int numbersToShow = (int) usableHeight / minSize + 1;
        float step = (getMaxY() - getMinY()) / (numbersToShow - 1);
        for (int i = 0; i < numbersToShow; i++) {
            int num = (int) (getMinY() + i * step);
            if (num != prevNum) {
                values.add(num);
            }
            prevNum = num;
        }

        // Draw the y axis
        for (int i = 0; i < values.size(); i++) {
            String num = values.get(i).toString();

            // Find the proper position for the text
            float pos = i * usableHeight / (values.size() - 1);
            // Add padding for the x axis
            pos += bottomPadding;
            // Center text
            pos -= numPaint.measureText(num) / 2;

            // Draw text
            canvas.drawText(num, pos, numPaint.getTextSize(), numPaint);
        }

        // Restore canvas upright
        canvas.restore();
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

}
