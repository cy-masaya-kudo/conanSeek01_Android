package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * 会話パラメーター
 */
public class KaiwaParam implements Serializable {

    public String hatsugensha;
    public String serifu;

    public KaiwaCharaParam leftChara;
    public KaiwaCharaParam centerChara;
    public KaiwaCharaParam rightChara;

    public class KaiwaCharaParam {
        public int hyoujouID;
        public String name;
        public int charaID;
        public boolean komadoFlag;
    }

    public String haikeiName;
    public int haikeiID;

    public boolean fadeIn;
    public boolean fadeOut;

    public String effectCode;

    public void fromCsv(String[] arrow) {

        this.hatsugensha = arrow[1];
        this.serifu = arrow[2];
        if (this.serifu.length() > 0) {
            this.serifu = this.serifu.replaceAll("<.+?>", " ");
        }

        this.leftChara = new KaiwaCharaParam();
        this.centerChara = new KaiwaCharaParam();
        this.rightChara = new KaiwaCharaParam();

        this.leftChara.hyoujouID = Common.parseInt(arrow[3]);
        this.leftChara.name = arrow[4];
        this.leftChara.charaID = Common.parseInt(arrow[5]);

        this.centerChara.hyoujouID = Common.parseInt(arrow[6]);
        this.centerChara.name = arrow[7];
        this.centerChara.charaID = Common.parseInt(arrow[8]);

        this.rightChara.hyoujouID = Common.parseInt(arrow[9]);
        this.rightChara.name = arrow[10];
        this.rightChara.charaID = Common.parseInt(arrow[11]);


        if (arrow[14].length() > 0) {
            if (arrow[14].indexOf("左") == 0) {
                if (this.leftChara.hyoujouID == 0) {
                    this.leftChara.komadoFlag = true;
                    if (arrow[14].indexOf("非表示") > 0) {
                        this.leftChara.name = "非表示";
                        this.leftChara.komadoFlag = false;
                    } else {
                        this.leftChara.name = arrow[12];
                        this.leftChara.charaID = Common.parseInt(arrow[13]);
                    }
                }
            } else if (arrow[14].indexOf("右") == 0) {
                if (this.rightChara.hyoujouID == 0) {
                    this.rightChara.komadoFlag = true;
                    if (arrow[14].indexOf("非表示") > 0) {
                        this.rightChara.name = "非表示";
                        this.rightChara.komadoFlag = false;
                    } else {
                        this.rightChara.name = arrow[12];
                        this.rightChara.charaID = Common.parseInt(arrow[13]);
                    }
                }
            } else {
                if (this.centerChara.hyoujouID == 0) {
                    this.centerChara.komadoFlag = true;
                    if (arrow[14].indexOf("非表示") > 0) {
                        this.centerChara.name = "非表示";
                        this.centerChara.komadoFlag = false;
                    } else {
                        this.centerChara.name = arrow[12];
                        this.centerChara.charaID = Common.parseInt(arrow[13]);
                    }
                }
            }
        }


        this.haikeiName = arrow[15];
        this.haikeiID = Common.parseInt(arrow[16]);

        this.effectCode = arrow[17];
        this.fadeIn = this.effectCode.indexOf("IN") == 0;
        this.fadeOut = this.effectCode.indexOf("OUT") == 0;
    }
}
