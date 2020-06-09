package jp.co.cybird.android.conanseek.activity.effect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.EventListener;

import jp.co.cybird.android.conanseek.common.BaseActivity;

/**
 * エフェクト基礎
 */
public class BaseEffect extends android.support.v4.app.Fragment {

    //エフェクトリスナー:エフェクト終わりを知る
    protected EffectListener eListener = null;

    public void setEffectListener(EffectListener l) {
        eListener = l;
    }

    public interface EffectListener extends EventListener {
        void effectFinished();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!startFlag) {
            startFlag = true;
            startAnimation();
        }
    }

    private boolean startFlag = false;
    protected void startAnimation() {
    }



    protected void removeMe() {
        ((BaseActivity)getActivity()).getCurrentViewController().removeEffect(this);
    }
}
