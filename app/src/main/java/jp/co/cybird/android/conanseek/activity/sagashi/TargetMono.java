package jp.co.cybird.android.conanseek.activity.sagashi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.param.LocationParam;
import jp.souling.android.conanseek01.Settings;

/**
 * ターゲットのモノ
 */
public class TargetMono extends ImageView {

    boolean xMirror;
    boolean yMirror;

    public LocationParam param;

    public TargetMono(Context context) {
        super(context);
        init();
    }

    public TargetMono(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TargetMono(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }

    private void init() {
        setScaleType(ScaleType.FIT_XY);
    }


    public LocationParam setImage(LocationParam param_, float ratio) {

        param = param_;

        String fileName = param.mono_file;
        if (param.obstacle_flag) {
            fileName = param.obstacle_file;
        }

        String extension = "png";
        if (Settings.isDebug && SaveManager.boolValue(SaveManager.KEY.DEBUG_REDOWNLOAD__boolean, false)) {
            extension = "error";
        }

        Bitmap bitmap = Common.decodedBitmap(
                CsvManager.bitmapImagePath("mono", String.valueOf(param.area_id), fileName, extension),
                0, 0
        );

        if (bitmap != null) {

            this.setImageBitmap(bitmap);

            param.image_width = bitmap.getWidth();
            param.image_height = bitmap.getHeight();
            float width = param.image_width * Math.abs(param.width);
            float height = param.image_height * Math.abs(param.height);

            xMirror = param.width < 0;
            yMirror = param.height < 0;

            this.setLayoutParams(new ViewGroup.LayoutParams(
                    (int) (width * ratio),
                    (int) (height * ratio)
            ));

            this.setX((param.x + (xMirror ? -width / 2f : -width / 2f)) * ratio);
            this.setY((param.y + (yMirror ? -height / 2f : -height / 2f)) * ratio);

            if (param.degree != 0) {
                this.setRotation(param.degree * 1);
            }


        } else {
            Common.logD("null :" + param.mono_file);
            return null;
        }

        return param;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.translate(xMirror ? getWidth() : 0, yMirror ? getHeight() : 0);
        canvas.scale(xMirror ? -1 : 1, yMirror ? -1 : 1);
        super.onDraw(canvas);
    }

}
