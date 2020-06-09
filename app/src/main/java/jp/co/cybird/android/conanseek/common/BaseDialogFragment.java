package jp.co.cybird.android.conanseek.common;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.EventListener;

import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * ダイアログフラグメント
 */
public class BaseDialogFragment extends android.support.v4.app.DialogFragment {

    //音を鳴らさないse
    public boolean silent;

    public static BaseDialogFragment newInstance(int identifier) {

        Bundle args = new Bundle();
        args.putInt("layout", identifier);

        BaseDialogFragment fragment = new BaseDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), R.style.Theme_TransparentDialog);

        //枠外タップ閉じ無効
        dialog.setCancelable(false);

        //バックボタン、サーチボタン閉じ無効
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_SEARCH:
                        return true;
                    default:
                        return false;
                }
            }
        });
        dialog.setOnShowListener(new Dialog.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (!(BaseDialogFragment.this instanceof APIDialogFragment)) {
                    if (!silent) {
                        SeManager.play(SeManager.SeName.SHOW_POPUP);
                        silent = false;
                    }
                }
                if (coverListener != null)
                    coverListener.shownCoveredDialog(BaseDialogFragment.this);
            }
        });


        return dialog;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean flag = false;

            @Override
            public void onGlobalLayout() {
                if (!flag) {

                    ViewGroup wrapper = (ViewGroup) view.findViewById(R.id.wrapper_inner);

                    if (wrapper != null) {

                        Point screenSize = new Point();
                        getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(screenSize);


                        float ratioActivity = (float) screenSize.x / 640.0f;
                        float width = ratioActivity * 540.0f;
                        float scale = (float) wrapper.getWidth() / width;

                        wrapper.setScaleX(1.0f / scale);
                        wrapper.setScaleY(1.0f / scale);

                        // Once data has been obtained, this listener is no longer needed, so remove it...
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                    }
                }
            }
        });
    }

    //------ arrow

    @Override
    public void onStart() {
        super.onStart();

        //レイアウトをスクリーン全体に
        if (getActivity() != null && getDialog() != null) {
            Point screenSize = new Point();
            getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(screenSize);
            getDialog().getWindow().setLayout(screenSize.x, screenSize.y);
        }
    }


    @Override
    public void dismiss() {
        super.dismiss();

        /*
        if (!(BaseDialogFragment.this instanceof APIDialogFragment)) {
            if (!silent) {
                SeManager.play(getContext(), SeManager.SeName.HIDE_POPUP);
            }
        }
        */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanView(getView());
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


    //------- リスナー

    public interface DialogCoverListener extends EventListener {
        public void shownCoveredDialog(BaseDialogFragment dialogFragment);
    }

    protected DialogCoverListener coverListener = null;

    public void setDialogCoverListener(DialogCoverListener l) {
        coverListener = l;
    }



}
