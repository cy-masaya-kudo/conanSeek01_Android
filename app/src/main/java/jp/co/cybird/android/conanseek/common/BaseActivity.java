package jp.co.cybird.android.conanseek.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.ViewPropertyAnimatorUpdateListener;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.List;

import jp.co.cybird.android.conanseek.activity.shop.CoinPopup;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;
import jp.souling.android.conanseek01.SplashFragment;


/**
 * アクティビティの基礎クラス
 */
public class BaseActivity extends AppCompatActivity {

    //レイアウト調整用ラッパー
    protected RelativeLayout wrapperLayout;

    //タップ阻止カバー
    protected FrameLayout notapLayout;
    //フェードアウト
    protected FrameLayout fadeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    // レイアウトサイズ調整
    protected void adjustActivitySize() {

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        //レイアウトxmlの２つ目のレイアウト
        ViewGroup.LayoutParams layoutSize = wrapperLayout.getLayoutParams();

        //２つ目のレイアウトのスケールを調整
        float scaleX = (float) screenSize.x / (float) layoutSize.width;
        float scaleY = (float) screenSize.y / (float) layoutSize.height;
        //小さい方に合わせる
        float scaleSmall = scaleX < scaleY ? scaleX : scaleY;
        //float scaleLarge = scaleX > scaleY ? scaleX : scaleY;

        wrapperLayout.setScaleX(scaleSmall);
        wrapperLayout.setScaleY(scaleSmall);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //BGM再開
        BgmManager.setBgm(BgmManager.BgmName.CONTINUE);
    }

    @Override
    protected void onResume() {
        super.onResume();


        //表示中フラグメントにレジューム通知
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getFragments() != null) {
            for (Fragment baseFragment : getSupportFragmentManager().getFragments()) {
                if (baseFragment instanceof BaseFragment) {
                    ((BaseFragment)baseFragment).fragmentRecieveActivityResume();
                }
            }
        }
    }

    protected void viewDidAppear() {
    }

    @Override
    protected void onStop() {
        super.onStop();

        //BGM止める
        BgmManager.setBgm(BgmManager.BgmName.PAUSE);

    }

    @Override
    protected void onPause() {
        super.onPause();

        //表示中フラグメントにポーズ通知
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getFragments() != null) {
            for (Fragment baseFragment : getSupportFragmentManager().getFragments()) {
                if (baseFragment instanceof BaseFragment) {
                    ((BaseFragment)baseFragment).fragmentRecieveActivityPause();
                }
            }
        }
    }

    //----- 画面遷移

    public BaseFragment getCurrentViewController() {

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BaseFragment) {
                return (BaseFragment) fragment;
            }
        }
        return null;
    }

    //ビューコントローラー切り替え
    public void replaceViewController(final BaseFragment fragment) {

        addNotap();

        //既に表示中確認
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getFragments() != null) {
            for (Fragment baseFragment : getSupportFragmentManager().getFragments()) {
                if (baseFragment instanceof BaseFragment) {
                    //Splashは再表示しない
                    if (baseFragment.getClass().equals(fragment.getClass()) && baseFragment instanceof SplashFragment) {
                        removeNotap();
                        return;
                    }
                }
            }
        }

        //表示中のポップアップの音を鳴らさない
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getFragments() != null) {
            for (Fragment baseFragment : getSupportFragmentManager().getFragments()) {
                if (baseFragment instanceof BaseFragment) {
                    if (baseFragment.getChildFragmentManager() != null && baseFragment.getChildFragmentManager().getFragments() != null) {
                        for (Fragment basePopup : baseFragment.getChildFragmentManager().getFragments()) {
                            if (basePopup instanceof BasePopup) {
                                ((BasePopup) basePopup).setPopupIsFront(false);
                            }
                        }
                    }
                }
            }
        }


        fadeLayout.setAlpha(1f);
        fadeLayout.setVisibility(View.VISIBLE);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1f);
        alphaAnimation.setDuration(500);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                //ft.setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit);

                fragment.onAddedFlag = true;
                ft.replace(R.id.viewcontroller_container, fragment);

                //ft.addToBackStack(null);
                ft.commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeLayout.startAnimation(alphaAnimation);

    }

    protected void removeFade () {
        fadeLayout.setAlpha(0f);
        fadeLayout.setVisibility(View.GONE);
    }

    //--タップ阻止

    public void removeNotap () {
        this.notapLayout.setClickable(false);
        //this.notapLayout.setBackgroundColor(0);
    }
    public void addNotap() {
        this.notapLayout.setClickable(true);
        //this.notapLayout.setBackgroundColor(Color.BLACK);
    }


    //---------

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Settings.RC_REQUEST) {

            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

            for (Fragment fragment : fragmentList) {
                if (fragment instanceof CoinPopup) {
                    CoinPopup coinPopup = (CoinPopup) fragment;
                    coinPopup.receiveOnActivityResult(requestCode, resultCode, data);
                    break;
                }
            }
        }
    }
}
