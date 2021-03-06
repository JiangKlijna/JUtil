package com.jiangKlijna.java;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * img/bitmap 工具
 * Author: com.jiangKlijna
 */
public class Image {
    private Image() {
    }

    //bitmap  to  uri
    public static Uri Bitmap2Uri(Context con, Bitmap bitmap) {
        return Uri.parse(MediaStore.Images.Media.insertImage(con.getContentResolver(), bitmap, null, null));
    }

    //uri  to  bitmap
    public static Bitmap Uri2Bitmap(Context con, Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(con.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //bitmap to intarr
    public static int[] bitmap2Ints(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        return pixels;
    }

    // bitmap to byteArr
    public static byte[] bitmap2Bytes(Bitmap bitmap, Bitmap.CompressFormat format) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    //byteArr to bitmap
    public static Bitmap bytes2Bitmap(byte[] bytes) {
        return (bytes == null || bytes.length == 0) ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    public static Drawable bitmap2Drawable(Resources res, Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(res, bitmap);
    }

    public static byte[] drawable2Bytes(Drawable drawable, Bitmap.CompressFormat format) {
        return drawable == null ? null : bitmap2Bytes(drawable2Bitmap(drawable), format);
    }

    public static Drawable bytes2Drawable(Resources res, byte[] bytes) {
        return res == null ? null : bitmap2Drawable(res, bytes2Bitmap(bytes));
    }

    /**
     * @param view
     * @return 将一个view转换为bitmap
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * @param view
     * @param rect
     * @return 把view按照rect裁剪
     */
    public static Bitmap convertViewToBitmap(View view, RectF rect) {
        view.buildDrawingCache();
        Bitmap reBitmap = view.getDrawingCache();
//        Canvas canvas = new Canvas(reBitmap);
//        canvas.setBitmap(reBitmap);
//        canvas.clipRect(rectF);
        Matrix matrix = new Matrix();
        matrix.postScale(1, 1);
        Bitmap bitmap = Bitmap.createBitmap(reBitmap, (int) (rect.left), (int) (rect.top), (int) (rect.right - rect.left), (int) (rect.bottom - rect.top), matrix, false);
        view.destroyDrawingCache();
        return bitmap;
    }

    /**
     * @param fileAbsolutePath img文件路径
     * @param screenp          bitmap的最大显示长宽
     * @return
     */
    public static Bitmap getImgMarkBitmap(String fileAbsolutePath, Point screenp) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileAbsolutePath, options);
        options.inJustDecodeBounds = false;
        if (options.outWidth > screenp.x || options.outHeight > screenp.y) {
            float scale = Math.max(options.outWidth / screenp.x, options.outHeight / screenp.y);
            options.inSampleSize = (int) Math.ceil(scale);
        }
        return BitmapFactory.decodeFile(fileAbsolutePath, options);
    }

    /**
     * @param p 屏幕长宽点
     * @return 获得一个屏幕中心裁剪框
     */
    public static RectF getCropRect(Point p) {
        float hblank = p.x * 0.15f;
        float vblank = (p.y - p.x) / 2;
        return new RectF(hblank, vblank, p.x - hblank, vblank + 0.7f * p.x);
    }

    //把bitmap保存成一个图片文件
    public static File bitmap2File(Bitmap b) {
        if (b == null) return null;
        try {
            File f = newImgFile();
            FileOutputStream out = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            return f;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File newImgFile() {
        File f = new File(Dir.SDCARD_APP_DIR, System.currentTimeMillis() + ".png");
        try {
            if (!f.exists()) f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public static Drawable getDrawableByRes(Resources res, int resid) {
        return new BitmapDrawable(res, BitmapFactory.decodeResource(res, resid));
    }

    public static Bitmap imageView2Bitmap(ImageView iv) {
        return ((BitmapDrawable) iv.getDrawable()).getBitmap();
    }

    /**
     * 按照规定形状图裁剪图片
     * 两张图合并后,把原图的(形状图非矢量)部分裁剪掉
     *
     * @param maskBitmap 形状图
     * @param picBitmap  原图
     * @return 合成之后的bitmap
     */
    public static Bitmap compose(Bitmap maskBitmap, final Bitmap picBitmap) {
        if (maskBitmap == null || picBitmap == null) return null;
        //前置的原图，并将其缩放到跟蒙板大小一直
//        picBitmap = Bitmap.createScaledBitmap(picBitmap, maskBitmap.getWidth(), maskBitmap.getHeight(), false);
        maskBitmap = Bitmap.createScaledBitmap(maskBitmap, picBitmap.getWidth(), picBitmap.getHeight(), false);

        int w = maskBitmap.getWidth();
        int h = maskBitmap.getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        //前置相片添加蒙板效果
        int[] picPixels = new int[w * h];
        int[] maskPixels = new int[w * h];
        picBitmap.getPixels(picPixels, 0, w, 0, 0, w, h);
        maskBitmap.getPixels(maskPixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < maskPixels.length; i++) {
            if (maskPixels[i] == 0xff000000) {
                picPixels[i] = 0;
            } else if (maskPixels[i] == 0) {
                //do nothing
            } else {
                //把mask的a通道应用与picBitmap
                maskPixels[i] &= 0xff000000;
                maskPixels[i] = 0xff000000 - maskPixels[i];
                picPixels[i] &= 0x00ffffff;
                picPixels[i] |= maskPixels[i];
            }
        }
        //生成前置图片添加蒙板后的bitmap:resultBitmap
        resultBitmap.setPixels(picPixels, 0, w, 0, 0, w, h);
        return resultBitmap;
    }
}
