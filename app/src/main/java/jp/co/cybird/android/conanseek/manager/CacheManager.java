package jp.co.cybird.android.conanseek.manager;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.card.CardContentFragment;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.JikenParam;

/**
 * パラメーターの簡易キャッシュ
 */
public class CacheManager {

    private static CacheManager instance;

    public static CacheManager instance() {
        if (instance == null)
            instance = new CacheManager();
        return instance;
    }

    public int jikenMapPage = 0;
    //public JikenParam jikenShownDetailParam = null;
    public JikenParam jikenSousaParam = null;
    public boolean jikenNewAlert = false;
    public String jikenClearedID = null;

    public int kunrenScorollPage = 0;
    public int kunrenLevelAreaID = 0;
    public String kunrenLevelAreaName = null;
    public int kunrenKakuninInLevel = 0;
    public String kunrenKakuninTitle = null;

    public int gachaCurrentPage = 0;
    public int gachaFirstDelivery = 0;
    public int gachaSelectedIndex = 0;
    public ArrayList<APIResponseParam.Item.GachaParam> gachaParamArrayList = null;

    public CardContentFragment cardRootFragment;

}
