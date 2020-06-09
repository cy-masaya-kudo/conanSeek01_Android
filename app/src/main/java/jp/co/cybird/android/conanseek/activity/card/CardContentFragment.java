package jp.co.cybird.android.conanseek.activity.card;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.Serializable;

import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.app.android.lib.commons.security.popgate.Codec;

/**
 * カード：コンテンツフラグメント
 */
public class CardContentFragment extends Fragment implements Serializable {

    protected void showPopupFromCardContent(BasePopup popup) {
        ((BaseActivity)getActivity()).getCurrentViewController().showPopup(popup);
    }

    public void willShowAgain() {
    }
}
