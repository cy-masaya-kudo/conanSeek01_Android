package jp.co.cybird.android.conanseek.common;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import jp.co.cybird.android.conanseek.activity.effect.BaseEffect;
import jp.co.cybird.android.conanseek.activity.top.HeaderMenuFragment;
import jp.co.cybird.android.conanseek.manager.APIDialogFragment;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;

/**
 * 画面遷移フラグメントクラス
 */
public class BaseFragment extends android.support.v4.app.Fragment implements BaseTutorial.TutoriaSteplListener {

    //フラグメントのBGM
    protected BgmManager.BgmName bgmName;

    //すでに登場したフラグ
    private boolean firstShownFlag = false;

    //ポップアップ表示カウント
    private int totalPopups = 0;

    //addされた初回処理用のフラグs
    public boolean onAddedFlag = false;

    // ヘッダー情報更新
    public void updateMyHeaderStatus() {

        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.headerMenu);
        if (fragment instanceof HeaderMenuFragment) {
            //スタータス値更新
            ((HeaderMenuFragment)fragment).updateTsuuka(true);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        Common.logD("on resume"+firstShownFlag);
        if (!firstShownFlag) {
            firstShownFlag = true;
            fragmentDidAppear();
        }

        if (isShownPopups()) {
            getView().findViewById(R.id.popup_container).setBackgroundColor(0xaa000000);
        } else {
            getView().findViewById(R.id.popup_container).setBackgroundColor(0);
        }
    }


    protected void fragmentDidAppear() {

        //BGM操作
        if (this.bgmName != null) {
            BgmManager.setBgm(this.bgmName);
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (getActivity() != null) {
                    ((BaseActivity)getActivity()).removeNotap();
                    ((BaseActivity)getActivity()).removeFade();
                }
            }
        }, 350);
    }

    protected void fragmentRecieveActivityResume() {

    }

    protected void fragmentRecieveActivityPause() {

    }


    //-------- popup

    public boolean isShownPopups () {

        if (getChildFragmentManager() != null && getChildFragmentManager().getFragments() != null) {
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof BasePopup) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showPopup(BasePopup popup, FragmentTransaction ft, boolean allowingStateLoss) {

        ((BaseActivity)getActivity()).addNotap();

        //他のポップアップを最前面状態から排除
        if (getChildFragmentManager() != null && getChildFragmentManager().getFragments() != null) {
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof BasePopup) {
                    ((BasePopup) fragment).setPopupIsFront(false);
                }
            }
        }

        if (!popup.noShowAnimation) {
            ft.setCustomAnimations(R.anim.popup_enter, R.anim.popup_exit);
        }

        ft.add(R.id.popup_container, popup);

        popup.setPopupIsFront(true);

        if (allowingStateLoss) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }

        totalPopups++;

        //1つ目のポップアップ
        if (totalPopups == 1) {
            popupsWillAppear();
        }
        if (getView() != null) {
            getView().findViewById(R.id.popup_container).setBackgroundColor(0xaa000000);
        }
    }
    public void showPopup(BasePopup popup, boolean allowingStateLoss) {

        //getActivity().getSupportFragmentManager().beginTransaction();

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        showPopup(popup, ft, allowingStateLoss);
    }
    public void showPopup(BasePopup popup) {

        showPopup(popup, false);
    }

    public void fireApi(APIDialogFragment apiDialogFragment) {
        apiDialogFragment.show(getChildFragmentManager(), null);
    }

    protected void removePopup(BasePopup popup) {

        popup.setPopupIsFront(false);

        //1つ前のポップアップを表示状態に更新
        int index = 0;
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof BasePopup) {
                index++;
                if (index == totalPopups)
                    ((BasePopup) fragment).setPopupIsFront(true);
            }
        }

        Common.logD("removePopup:"+totalPopups);

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        ft.remove(popup);

        //ft.addToBackStack(null);
        ft.commit();

        //消した後にポップアップがない
        totalPopups--;
        if (totalPopups == 0) {
            popupsDidDisappear();
            getView().findViewById(R.id.popup_container).setBackgroundColor(0x00000000);
        }
    }
    protected void removeAllPopup() {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        while (true) {
            boolean flag = false;
            for (Fragment fragment : getChildFragmentManager().getFragments()) {
                if (fragment instanceof BasePopup) {
                    ft.remove(fragment);
                    flag = true;
                    break;
                }
            }
            if (flag) continue;
            break;
        }

        ft.commit();
        totalPopups = 0;
        popupsDidDisappear();
        getView().findViewById(R.id.popup_container).setBackgroundColor(0x00000000);
    }

    //ポップアップが１つ以上出現
    protected void popupsWillAppear() {

    }

    //ポップアップ全て無くなる
    protected void popupsDidDisappear() {

    }

    //-------- effect

    public void showEffect(Fragment fragment) {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.popup_container, fragment);
        ft.commit();
    }

    public void removeEffect(BaseEffect effect) {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        ft.remove(effect);

        //ft.addToBackStack(null);
        ft.commit();
    }

    //-------- チュートリアル

    //開始
    protected void startTutorial (String tutorialCode) {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        BaseTutorial tutorial = new BaseTutorial(tutorialCode);
        tutorial.setTutoriaSteplListener(this);

        ft.replace(R.id.tutorial_container, tutorial);

        ft.commit();
    }

    //終了
    protected void stopTutorial (BaseTutorial tutorial) {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        ft.remove(tutorial);

        ft.commit();
    }

    //nextステップ遷移
    protected void stepNextTutorial() {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof BaseTutorial) {
                ((BaseTutorial)fragment).stepNext();
                break;
            }
        }
    }


    //---tutorial

    @Override
    public void didEndTutorial() {

    }

    @Override
    public void pushedTarget(TutorialParam param) {
    }


    //-- メモリ解放

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


}
