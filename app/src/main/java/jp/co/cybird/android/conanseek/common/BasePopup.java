package jp.co.cybird.android.conanseek.common;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.EventListener;

import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.SeManager;

public class BasePopup extends android.support.v4.app.Fragment {

    //登場音
    public SeManager.SeName showSound = SeManager.SeName.SHOW_POPUP;

    //退場音
    public SeManager.SeName hideSound = SeManager.SeName.HIDE_POPUP;

    //登場アニメーション省略
    public boolean noShowAnimation = false;

    //すでに登場したフラグ
    protected boolean firstShownFlag = false;

    //最前面状態
    private boolean popupIsFront = false;

    //下の階層にタッチ通さないダミー
    //private Button dummy;

    //矢印
    protected ArrowButton leftArrow;
    protected ArrowButton rightArrow;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setPopupIsFront(boolean front) {
        if (popupIsFront != front) {
            popupIsFront = front;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!firstShownFlag) {
            firstShownFlag = true;
            popupDidAppear();
        }
    }


    protected void popupDidAppear() {

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ((BaseActivity) getActivity()).removeNotap();
            }
        }, 150);

        if (showSound != null)
            SeManager.play(showSound);

        if (displayListner != null)
            displayListner.didShowPopup(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (hideSound != null && popupIsFront)
            SeManager.play(hideSound);

        if (displayListner != null)
            displayListner.didClosePopup(this);

        cleanView(getView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    protected void showPopupFromPopup(BasePopup popup) {
        showPopupFromPopup(popup, false);
    }

    protected void showPopupFromPopup(BasePopup popup, boolean allow) {
        BaseFragment baseFragment = ((BaseActivity) getActivity()).getCurrentViewController();
        baseFragment.showPopup(popup, allow);
    }

    public void removeMe() {
        BaseFragment baseFragment = ((BaseActivity) getActivity()).getCurrentViewController();
        baseFragment.removePopup(this);
    }

    protected void fireApiFromPopup(APIDialogFragment apiDialogFragment) {
        BaseFragment baseFragment = ((BaseActivity) getActivity()).getCurrentViewController();
        baseFragment.fireApi(apiDialogFragment);
    }






    //----- イベントリスナー

    public interface PopupButtonListener extends EventListener {
        void pushedPositiveClick(BasePopup popup);

        void pushedNegativeClick(BasePopup popup);


    }

    protected PopupButtonListener buttonListener = null;

    public void setPopupButtonListener(PopupButtonListener l) {
        buttonListener = l;
    }


    public interface PopupDisplayListener extends EventListener {
        void didShowPopup(BasePopup popup);
        void didClosePopup(BasePopup popup);
    }


    protected PopupDisplayListener displayListner = null;

    public void setPopupDisplayListener(PopupDisplayListener l) {
        displayListner = l;
    }




    public void cleanView(View view) {

        if (view instanceof ViewGroup) {
            view.setBackground(null);
        }
        if (view instanceof TextView) {
            view.setBackground(null);
        }
        if (view instanceof ImageButton) {
            ImageButton b = (ImageButton) view;
            b.setImageDrawable(null);
        } else if (view instanceof ImageView) {
            ImageView v = (ImageView) view;
            v.setImageDrawable(null);
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            int g_size = g.getChildCount();
            for (int i = 0; i < g_size; i++) {
                cleanView(g.getChildAt(i));
            }
        }


        System.gc();
        System.runFinalization();
    }

}

