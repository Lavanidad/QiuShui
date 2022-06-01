package com.lavanidad.qiushui.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author : fzy
 * @Date : 2022/4/25
 * @Description :
 */
public class ImageUtils {

    private static final String MAGIC = "P5";
    private static final char COMMENT = '#';
    private static final int MAXVAL = 255;

    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void decoderBase64File(String base64Code, String savePath) throws Exception {
        //byte[] buffer = new BASE64Decoder().decodeBuffer(base64Code);
        byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
        FileOutputStream out = new FileOutputStream(savePath);
        out.write(buffer);
        out.close();
    }

    public String bitmaptoString(Bitmap bitmap) {
        //将Bitmap转换成字符串
        String string = null;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    private static String next(final InputStream stream) throws IOException {
        final List<Byte> bytes = new ArrayList<Byte>();
        while (true) {
            final int b = stream.read();

            if (b != -1) {

                final char c = (char) b;
                if (c == COMMENT) {
                    int d;
                    do {
                        d = stream.read();
                    } while (d != -1 && d != '\n' && d != '\r');
                } else if (!Character.isWhitespace(c)) {
                    bytes.add((byte) b);
                } else if (bytes.size() > 0) {
                    break;
                }
            } else {
                break;
            }
        }
        final byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytesArray.length; ++i)
            bytesArray[i] = bytes.get(i);
        return new String(bytesArray);
    }

    public static int[][] getPgmArray(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[][] imIndex = new int[h][w];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int p = bitmap.getPixel(i, j);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;
                int avg = (r + g + b) / 3;
                imIndex[j][i] = avg;
            }
        }
        Log.e("getPgmArray", "w:" + w + ",h:" + h);
        return imIndex;
    }

    public static String write(final int[][] image, final File file, final int maxval) throws IOException {
        String res = "";
        if (maxval > MAXVAL)
            throw new IllegalArgumentException("The maximum gray value cannot exceed " + MAXVAL + ".");
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        try {
            stream.write(MAGIC.getBytes(StandardCharsets.UTF_8));
            stream.write("\n".getBytes());
            stream.write(Integer.toString(image[0].length).getBytes());
            stream.write(" ".getBytes());
            stream.write(Integer.toString(image.length).getBytes());
            stream.write("\n".getBytes());
            stream.write(Integer.toString(maxval).getBytes());
            stream.write("\n".getBytes());

            for (int i = 0; i < image.length; i++) {
                for (int j = 0; j < image[0].length; j++) {
                    final int p = image[i][j];
                    if (p < 0 || p > maxval)
                        throw new IOException("Pixel value " + p + " outside of range [0, " + maxval + "].");
                    stream.write(image[i][j]);
                }
            }
//            try {
//                FileInputStream fin = new FileInputStream(file);
//                int length = fin.available();
//                byte[] buffer = new byte[length];
//                fin.read(buffer);
//                //res = EncodingUtils.getString(buffer, "UTF-8");
//                res = new String(buffer, StandardCharsets.UTF_8);
//                fin.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            Log.d("test", "PGM create successful, file name :" + file.getName() + "\nwidth:" + image[0].length + ",height:" + image.length);
        } catch (Exception e) {
            Log.e("test", "" + e.getMessage());
        } finally {
            Log.d("test", ", file name :" + file.getName() + "\nwidth:" + image[0].length + ",height:" + image.length);
            stream.close();
        }
        return res;
    }
}
