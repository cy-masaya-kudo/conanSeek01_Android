package jp.co.cybird.android.conanseek.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.souling.android.conanseek01.Settings;

/**
 * 共通クラス
 * - 主に汎用処理
 * - アプリの設定に関するパラメーターはSeetingsに置いた
 */
public class Common {

    public static Context myAppContext;

    /**
     * ログ用のタグ
     */
    public static final String TAG = "ConanSeek01";

    public static void logD(String s) {
        if (Settings.isDebug) {
            Log.e(TAG, s);
        }
    }

    public static void apiLog(String s) {
        if (Settings.isDebug) {
            Log.e(TAG, s);
        }
    }

    public static void tjLog(String s) {
        if (Settings.isDebug) {
            Log.e(TAG+"TapJoy", s);
        }
    }


    /**
     * バージョン名
     */
    public static String getVersionName() {
        PackageManager pm = Common.myAppContext.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(Common.myAppContext.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    /**
     * 文字列数値化
     */
    public static int parseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public static long parseLong(String val) {
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 秒数->分秒
     */
    public static String secondsToMinutes(int seconds) {

        int min = seconds / 60;
        int sec = seconds % 60;

        if (min < 0) {
            min = 0;
        }
        if (sec < 0) {
            sec = 1;
        }

        return min + ":" + String.format("%1$02d", sec);

    }


    /**
     * HTML画像
     */
    public static class ResouroceImageGetter implements Html.ImageGetter {

        public ResouroceImageGetter(Context context) {
        }


        public Drawable getDrawable(String source) {
            Resources res = Common.myAppContext.getResources();

            int id = res.getIdentifier(source, "mipmap", Common.myAppContext.getPackageName());

            float ratio = 0.3f;
            if (source.indexOf("angou") != -1) {
                ratio = .5f;
            }

            if (res != null) {

                Drawable d = res.getDrawable(id);

                if (d != null) {
                    int w = d.getIntrinsicWidth();
                    int h = d.getIntrinsicHeight();

                    w = (int) (w * ratio);
                    h = (int) (h * ratio);

                    d.setBounds(0, 0, w, h);
                }

                return d;
            }
            return null;
        }
    }


    public static Bitmap decodedBitmap(String imagePath, int width, int height, float ratio) {
        return decodedBitmap(imagePath, (int) ((float) width * ratio), (int) ((float) height * ratio));
    }
    public static Bitmap decodedBitmap(String imagePath, int width, int height) {

        InputStream is = null;
        Bitmap bitmap = null;
        try {
            //is = resources.getAssets().open(imagePath);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            //BitmapFactory.decodeStream(is, null, options);

            if (width == 0) width = options.outWidth;
            if (height == 0) height = options.outHeight;

            options.inSampleSize = calculateInSampleSize(options, width, height);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(imagePath, options);


            //} catch (IOException e) {
            //Common.logD("e:"+e.toString());
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }

    public static Bitmap cachedBitmap(String cacheFile) {

        Bitmap bitmap = null;

        FileInputStream inputStream = null;

        try {
            File file = new File(Common.myAppContext.getCacheDir(), cacheFile);
            if (file.exists()) {
                inputStream = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            Common.logD("IOException"+e.toString());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Common.logD("IOException"+e.toString());
            }
        }

        return bitmap;
    }

    public static Bitmap decodedAssetBitmap(String imagePath, int width, int height, float ratio) {

        return decodedAssetBitmap(imagePath, (int) ((float) width * ratio), (int) ((float) height * ratio));
    }

    public static Bitmap decodedAssetBitmap(String imagePath, int width, int height) {

        InputStream is = null;
        Bitmap bitmap = null;
        try {

            is = Common.myAppContext.getResources().getAssets().open(imagePath);

            final BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            //ここから追加---
            try {
                is.reset();
            } catch (IOException e) {
                return null;
            }
            //追加ここまで---
            options.inSampleSize = calculateInSampleSize(options, width, height);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(is, null, options);


        } catch (IOException e) {
            //Common.logD("e:"+e.toString());
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }


    public static Bitmap decodedResource(int identifier, int width, int height, float ratio) {
        return decodedResource(identifier, (int) ((float) width * ratio), (int) ((float) height * ratio));
    }
    public static Bitmap decodedResource(int identifier, int width, int height) {

        Bitmap bitmap = null;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(Common.myAppContext.getResources(), identifier, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeResource(Common.myAppContext.getResources(), identifier, options);
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // 画像の元サイズ
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        reqWidth *= (3.0 / Common.myAppContext.getResources().getDisplayMetrics().density);
        reqHeight *= (3.0 / Common.myAppContext.getResources().getDisplayMetrics().density);

        Common.logD("density:"+Common.myAppContext.getResources().getDisplayMetrics().density);

        reqWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reqWidth, Common.myAppContext.getResources().getDisplayMetrics());
        reqHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, reqHeight, Common.myAppContext.getResources().getDisplayMetrics());

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

}
