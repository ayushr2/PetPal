package com.hackharvard.petsafeandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;


/**
 * This code was taken from the link mentioned below. We acknowledge the curator of this class for
 * their intellectual property and are not plagiarising.
 *
 * This class implements Picasso library's Transformation interface. So a transformation object can
 * be passed into Picasso method. This class basically converts an image into a circular image.
 *
 * @author Ayush Ranjan
 * @see <a href="https://stackoverflow.com/questions/26112150/android-create-circular-image-with-picasso">Source</a>
 * @since 11/08/17.
 */
public class CircleTransformation implements Transformation {

    /**
     * Overriden method from Transformation interface of Picasso Library.
     *
     * @param source source image being passed in which has to be processed
     * @return processed circular image with minimum loss of data
     */
    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        // x, y are the center coordinates
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // source is cropped from the centre in a square shape of maximum size
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}