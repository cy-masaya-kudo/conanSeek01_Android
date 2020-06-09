package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * 事件パラメーター
 */
public class JikenParam implements Serializable {

    public int id;

    //Integer sort;
    public String jikenID;
    public String chouhenJikenID;
    public boolean chouhenFlag;
    public boolean chouhenFirstFlag;
    public boolean chouhenLastFlag;
    public int chouhenEpisodeNumber;
    public int chouhenSeriesNumber;
    public String kaihouJoukenJikenID;
    public String kaihouJoukenCardRare;

    public int kaihouJoukenCardCount;
    public String jikenName;
    //String story_image;
    //String summary_floder_name;
    //Srring summary_file_name;

    //String scenario_floder_name;
    //String scenario_file_name;
    public int target_amount;
    public int secretcode_amount;
    public int reason;

    public int suspect;
    public boolean storyOnly;
    //int comsume_glass;
    //int retry_consume_glass;
    public String kunrenKaihou;

    public int map_number;
    public int map_x;
    public int map_y;
    public String houshuu;
    public int houshuu_amount;


    public void fromCsv(String[] array) {

        id = Common.parseInt(array[0]);
        jikenID = array[2];

        //Common.logD("jikenID:" + jikenID + " :" + array.length);

        chouhenFlag = array[3].equals("長編");
        chouhenFirstFlag = (chouhenFlag && jikenID.indexOf("-OP") > 0);
        chouhenLastFlag = (chouhenFlag && jikenID.indexOf("-ED") > 0);

        if (chouhenFlag) {
            chouhenJikenID = jikenID.substring(0, jikenID.lastIndexOf("-"));
            if (!chouhenFirstFlag && !chouhenLastFlag) {
                chouhenEpisodeNumber = Common.parseInt(jikenID.replace(chouhenJikenID + "-", ""));
            }
            chouhenSeriesNumber = Common.parseInt(chouhenJikenID.replace("L-", ""));
        }

        kaihouJoukenJikenID = null;
        if (!array[4].equals("なし")) kaihouJoukenJikenID = array[4];


        kaihouJoukenCardRare = null;
        kaihouJoukenCardCount = 0;
        if (!array[5].equals("なし")) {
            kaihouJoukenCardRare = array[5];
            String str = array[6].replace("枚以上所持", "");
            kaihouJoukenCardCount = Common.parseInt(str);
        }

        jikenName = array[7];

        target_amount = Common.parseInt(array[13]);
        secretcode_amount = Common.parseInt(array[14]);
        reason = Common.parseInt(array[15]);
        suspect = Common.parseInt(array[16]);

        storyOnly = (array[17].indexOf("ストーリー") == 0);

        kunrenKaihou = null;
        if (!array[20].equals("なし")) {
            kunrenKaihou = array[20];
        }

        if (array.length > 21) {

            map_number = Common.parseInt(array[21]);
            map_x = Common.parseInt(array[22]);
            map_y = Common.parseInt(array[23]);

            houshuu = null;
            houshuu_amount = 0;

            if (array.length > 24) {
                String str = array[24];
                if (str.length() > 0) houshuu = str;
                houshuu_amount = Common.parseInt(array[25]);
            }
        }
    }


    public String shoukohin;
    public String sagashiStage;
    public String iraishaName;
    public String hanninName;
    public ArrayList<String> hazureYougishaList;
    public ArrayList<String> hazureItemList;

    public boolean angouChouhenFlag;
    public String angouString;
    public String kakushiAngouString;
    public String seikaiBusshou;
    public ArrayList<String> fuseikaiBusshouList;


    public boolean suiriChouhenFlag;
    public ArrayList<ShougenParam> shougenList;

    public String suiriSeikaiItem;
    public ArrayList<String> suiriMachigaiItem;

    public boolean shougenChouhenFlag;
    public String higaishaName;
    public String seikaiDouki;
    public ArrayList<String> hazureDoukiList;



}
