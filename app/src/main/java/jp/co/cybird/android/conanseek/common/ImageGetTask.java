package jp.co.cybird.android.conanseek.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * HTTPで画像を読み込む
 */
public class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView image = null;

    private String cacheDir = null;
    private String cacheFile = null;

    public ImageGetTask(ImageView _image, String _cacheDir, String _cacheFile) {
        image = _image;
        cacheDir = _cacheDir;
        cacheFile = _cacheFile;
    }


    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap;
        InputStream imageIs = null;

        try {
            URL imageUrl = new URL(params[0]);
            imageIs = imageUrl.openStream();
            bitmap = BitmapFactory.decodeStream(imageIs);
            return bitmap;
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (imageIs != null) {
                    imageIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (cacheDir != null && cacheFile != null) {
            FileOutputStream outputStream = null;
            try {
                // キャッシュ領域にファイルを作成し、書き込む。
                File file = new File(cacheDir, cacheFile);
                file.createNewFile();
                if (file.exists()) {
                    outputStream = new FileOutputStream(file);
                    result.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                }
            } catch (IOException e) {
                //Common.logD("e:"+e.toString());
            }
            finally {
                try {
                    if(outputStream != null) { outputStream.close(); }
                }
                catch (IOException e){
                    //Common.logD("eee:"+e.toString());
                }
            }
        }
        if (image != null)
            image.setImageBitmap(result);
    }
}