package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;

/**
 * ロケーションパラメーター
 */
public class LocationParam implements Serializable {

    //{"location":[{"location_id":"1","area_id":"1","target_id":"1","x":"101","y":"394","width":"0.5","height":"0.5","degree":"0","obstacle_id":"127"},

    //ものID
    public int mono_id;
    //もの名前
    public String mono_name;
    //ものファイル名
    public String mono_file;

    //jsonから得られる値たち
    public int location_id;
    public int area_id;
    public int target_id;
    public float x;
    public float y;
    public float width;
    public float height;
    public float image_width;
    public float image_height;
    public float degree;
    public int obstacle_id;

    //障害物かどうか
    public boolean obstacle_flag;
    //障害物の場合のファイル名
    public String obstacle_file;

    //エフェクトターゲット発動済み
    public boolean targettedFlag;
}
