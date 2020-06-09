package jp.co.cybird.android.conanseek.activity.sagashi;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import java.util.EventListener;

import jp.co.cybird.android.conanseek.manager.Common;


/**
 * 探しパレット
 */
public class SagashiPaletteFrame extends FrameLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;

    private Mode mode = Mode.NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;

    private float startX = 0f;
    private float startY = 0f;

    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;

    private int zoomDirectionX = -1;
    private int zoomDirectionY = -1;

    private boolean firstTouch = true;

    private float start_moveX = 0f;
    private float start_moveY = 0f;
    private static final float MAX_MOVE = 50.0f;
    private boolean tapTouch = false;

    private int rotationValue = 0;


    //------- リスナー
    public interface PaletteTapListener extends EventListener {
        void tappedPalette(float posX, float posY);

        void zoomedPalette(boolean zoomed);
    }

    protected PaletteTapListener myListener = null;

    public void setTapListener(PaletteTapListener l) {
        myListener = l;
    }


    public void updateRotation(int value) {
        rotationValue = value;
    }


    public SagashiPaletteFrame(Context context) {
        super(context);
        init(context);
    }

    public SagashiPaletteFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SagashiPaletteFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {


        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                float meX = motionEvent.getX() - SagashiPaletteFrame.this.getLeft();
                float meY = motionEvent.getY() - SagashiPaletteFrame.this.getTop();

                float rawX = motionEvent.getRawX();// - SagashiPaletteFrame.this.getLeft();
                float rawY = motionEvent.getRawY();// - SagashiPaletteFrame.this.getTop();


                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (scale > MIN_ZOOM) {
                            mode = Mode.DRAG;
                            startX = meX - prevDx;
                            startY = meY - prevDy;
                        }
                        start_moveX = rawX;
                        start_moveY = rawY;

                        tapTouch = true;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == Mode.DRAG) {
                            dx = meX - startX;
                            dy = meY - startY;
                        }
                        float mx = rawX - start_moveX;
                        float my = rawY - start_moveY;
                        if (Math.abs(mx) + Math.abs(my) > MAX_MOVE) {
                            tapTouch = false;
                        }
                        //Common.logD("motionEvent.getY()|"+motionEvent.getY());

                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        tapTouch = false;
                        mode = Mode.ZOOM;

                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = Mode.NONE;
                        zoomDirectionX = -1;
                        zoomDirectionY = -1;
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = Mode.NONE;
                        prevDx = dx;
                        prevDy = dy;
                        firstTouch = true;
                        //回転加味
                        if (rotationValue == 1) prevDx = -prevDx;
                        if (rotationValue == 2) prevDy = -prevDy;
                        if (tapTouch) {
                            if (myListener != null) {

                                float sx = meX;
                                float sy = meY;

                                //回転加味
                                if (rotationValue == 1) sx = getWidth() / scale - sx / scale;
                                else sx = sx / scale;
                                if (rotationValue == 2) sy = getHeight() / scale - sy / scale;
                                else sy = sy / scale;
                                //xy座標
                                float px = getWidth() / 2.0f - getWidth() / scale / 2.0f - dx / scale + sx;
                                float py = getHeight() / 2.0f - getHeight() / scale / 2.0f - dy / scale + sy;


                                //Common.logD("ppp:" + px + " " + py + " " + sy);
                                myListener.tappedPalette(px, py);
                            }
                        }
                        break;
                }
                scaleDetector.onTouchEvent(motionEvent);

                if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
                    float maxDy = (child().getHeight() - (child().getHeight() / scale)) / 2 * scale;
                    dx = Math.min(Math.max(dx, -maxDx), maxDx);
                    dy = Math.min(Math.max(dy, -maxDy), maxDy);
                    if (mode == Mode.DRAG) {
                        if (rotationValue == 1) dx = -dx;
                        if (rotationValue == 2) dy = -dy;
                    }
                    applyScaleAndTranslation();
                }

                return true;
            }
        });
    }

    public void resetPalette() {
        scale = 1;
        dx = 0;
        dy = 0;
        firstTouch = true;

        child().setScaleX(scale);
        child().setScaleY(scale);
        child().setTranslationX(dx);
        child().setTranslationY(dy);
    }

    // ScaleGestureDetector

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleDetector) {
        float scaleFactor = scaleDetector.getScaleFactor();
        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            scale *= scaleFactor;
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            lastScaleFactor = scaleFactor;
        } else {
            lastScaleFactor = 0;
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleDetector) {
    }

    private int zoomTutorialStep = 0;

    private void applyScaleAndTranslation() {

        if (firstTouch) {
            firstTouch = false;
            return;
        }

        if (zoomDirectionX == -1) {
            zoomDirectionX = dx < 0 ? 1 : 2;
            return;
        }
        if (zoomDirectionY == -1) {
            zoomDirectionY = dy < 0 ? 1 : 2;
            return;
        }
        if (mode == Mode.ZOOM) {
            if (zoomDirectionX == 1) {
                if (dx > 0) return;
            } else if (zoomDirectionX == 2) {
                if (dx < 0) return;
            }
            if (zoomDirectionY == 1) {
                if (dy > 0) return;
            } else if (zoomDirectionY == 2) {
                if (dy < 0) return;
            }
        }

        if (mode == Mode.ZOOM) {
            child().setScaleX(scale);
            child().setScaleY(scale);
        }

        child().setTranslationX(dx);
        child().setTranslationY(dy);
        //Common.logD("zoomDirectionY" + zoomDirectionY + " dy" + dy + " " + mode.toString());


        if (zoomTutorialStep == 0 && scale > 1.1) {
            zoomTutorialStep = 1;
            if (myListener != null)
                myListener.zoomedPalette(true);
        } else if (scale <= 1 && zoomTutorialStep == 1) {
            zoomTutorialStep = 2;
            if (myListener != null)
                myListener.zoomedPalette(false);
        }
    }

    private View child() {
        return getChildAt(0);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {

        if (rotationValue == 1) {
            canvas.translate(getWidth(), 0);
            canvas.scale(-1, 1);
        } else if (rotationValue == 2) {
            canvas.translate(0, getHeight());
            canvas.scale(1, -1);
        }

        super.dispatchDraw(canvas);
    }

}
