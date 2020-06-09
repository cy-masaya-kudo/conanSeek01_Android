package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * ログインボーナスパラメーター
 */
public class LoginbonusParam implements Serializable {

    public int dayCount;
    public String rewardName;
    public int rewardAmount;
}
