package jp.co.cybird.android.conanseek.common;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.EventListener;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * チュートリアルのクリック制御と
 * まどあき
 */
public class TutorialCover extends View {

    private ArrayList<RectF> rectFs;

    private TutorialState state;

    enum TutorialState {
        //会話読み進め
        KAIWA_STEP,
        //自由操作
        FREE,
        //穴あき箇所のみタップ可能
        HOLE_CLICK,
        //操作不可
        NOTOUCH
    }

    public TutorialCover(Context context) {
        super(context);
        init();
    }

    public TutorialCover(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TutorialCover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(coverTouchListener);
        setOnClickListener(coverClickListener);
    }

    //------

    public interface TutoriaCoverlListener extends EventListener {
        void onClickStepNext();
        void onClickTarget();
    }

    protected TutoriaCoverlListener tutorialCoverListener = null;

    public void setTutoriaCoverlListener(TutoriaCoverlListener l) {
        tutorialCoverListener = l;
    }


    //------


    public TutorialState getState() {
        return state;
    }

    public void setState(TutorialState state) {
        this.state = state;

        switch (state) {

            case KAIWA_STEP:
            case NOTOUCH:
            case HOLE_CLICK:
                this.setVisibility(View.VISIBLE);
                break;
            case FREE:
                this.setVisibility(View.GONE);
                break;
        }
    }


    private Point point = new Point();

    private View.OnTouchListener coverTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            point.x = (int) event.getX();
            point.y = (int) event.getY();

            return false;
        }
    };

    private View.OnClickListener coverClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Common.logD("tutorial clicked:" + point.x + "," + point.y);

            switch (state) {

                case NOTOUCH:
                case FREE:
                    break;
                case KAIWA_STEP:
                    if (tutorialCoverListener != null)
                        tutorialCoverListener.onClickStepNext();
                    break;
                case HOLE_CLICK:

                    if (rectFs != null) {
                        for (RectF rectF : rectFs) {
                            if (rectF.left <= point.x && rectF.right >= point.x
                                    && rectF.top <= point.y && rectF.bottom >= point.y) {

                                if (tutorialCoverListener != null)
                                    tutorialCoverListener.onClickTarget();

                            }
                        }
                    }

                    break;
            }

        }
    };



    public void clearCanvas() {
        rectFs = null;
    }

    public void addRect(int left, int top, int right, int bottom) {

        if (rectFs == null)
            rectFs = new ArrayList<RectF>();

        rectFs.add(new RectF(dp2Px(left), dp2Px(top), dp2Px(right), dp2Px(bottom)));

    }

    public int dp2Px(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rectFs == null) {

            Common.logD("tutorial cover CLEAR");
            this.setAlpha(0f);
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        } else {

            Common.logD("tutorial cover HOLED");
            this.setAlpha(1f);

            Paint paint = new Paint();
            paint.setColor(0xdd000000);

            Path path = new Path();
            path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);

            for (RectF rectF : rectFs) {
                path.addRect(rectF, Path.Direction.CCW);
            }

            canvas.drawPath(path, paint);
        }

    }
}
