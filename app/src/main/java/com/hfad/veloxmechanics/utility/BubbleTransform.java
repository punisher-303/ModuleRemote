package com.hfad.veloxmechanics.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.core.content.res.ResourcesCompat;

import com.hfad.veloxmechanics.R;
import com.squareup.picasso.Transformation;


public class BubbleTransform implements Transformation {

    private static final int outerMargin = 40;
    private final int margin;  // dp

    private Context context = null;

    // margin is the board in dp
    public BubbleTransform(final int margin) {
        this.margin = margin;
    }

    public BubbleTransform(Context context, final int margin) {
        this.margin = margin;
        this.context = context;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paintBorder = new Paint();

        if (context != null)
            paintBorder.setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null));
        else
            paintBorder.setColor(Color.CYAN);
        paintBorder.setStrokeWidth(margin);
        canvas.drawRoundRect(new RectF(outerMargin, outerMargin, source.getWidth() - outerMargin, source.getHeight() - outerMargin), 0, 0, paintBorder);

        Paint trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        trianglePaint.setStrokeWidth(2);
        if (context != null)
            trianglePaint.setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorAccent, null));
        else
            trianglePaint.setColor(Color.CYAN);
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setAntiAlias(true);

        Path triangle = new Path();
        triangle.setFillType(Path.FillType.EVEN_ODD);
        triangle.moveTo(outerMargin, source.getHeight() / 2);
        triangle.lineTo(source.getWidth() / 2, source.getHeight());
        triangle.lineTo(source.getWidth() - outerMargin, source.getHeight() / 2);
        triangle.close();

        canvas.drawPath(triangle, trianglePaint);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(margin + outerMargin, margin + outerMargin, source.getWidth() - (margin + outerMargin), source.getHeight() - (margin + outerMargin)), 0, 0, paint);

        if (source != output) {
            source.recycle();
        }

        return output;

    }

    @Override
    public String key() {
        return "round";
    }
}
