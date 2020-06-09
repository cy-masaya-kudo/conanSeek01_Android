package jp.co.cybird.android.conanseek.param;

import java.util.Map;

/**
 * カードパラメーター
 */
public class CardParam {

    public String name;
    public int cardID;
    public String rareString;
    public int rareInt;
    public int skillID;

    public String skillName;
    public String skillDetail;
    public String skillDetailLong;
    public int skillValue;
    private int skillTypeValue;
    public SkillType skillType;

    public enum SkillType {
        Time,
        Direction,
        Color,
        Target,
        Number
    }


    public static String[] card_csv_keys = {"cardID", "zukanID", "rare", "fileName", "name", "imageName", "skillID"};

    /**
     * mapから値代入
     *
     * @param map
     */
    public void mapToParam(Map<String, String> map) {
        cardID = Integer.parseInt(map.get("cardID"));
        //serialID = Integer.parseInt(map.get("serialID"));
        rareString = map.get("rare");
        rareInt = 0;
        if (rareString.equals("HN")) rareInt = 1;
        else if (rareString.equals("R")) rareInt = 2;
        else if (rareString.equals("SR")) rareInt = 3;
        else if (rareString.equals("SSR")) rareInt = 4;
        name = map.get("name");
        skillID = Integer.parseInt(map.get("skillID"));
    }

    public void addSkillParam(Map<String, String> map) {

        skillTypeValue = Integer.parseInt(map.get("skill_type"));
        skillValue = Integer.parseInt(map.get("skill_value"));

        switch (skillTypeValue) {
            case 1:
                skillType = SkillType.Time;
                skillName = "時間延長";
                skillDetail = "時間を" + skillValue + "秒延長";
                skillDetailLong = "捜査の時間を" + skillValue + "秒延長する。捜査時間内にいつでも使うことができる。";
                break;

            case 2:
                skillType = SkillType.Direction;
                skillName = "背景反転";
                skillDetail = "背景を" + skillValue + "秒反転";
                skillDetailLong = "上下・左右にひっくり返った背景を" + skillValue + "秒間正常な状態にする。背景がひっくり返っていない時は効果は発揮されない。";
                break;

            case 3:
                skillType = SkillType.Color;
                skillName = "モノクロ解除";
                skillDetail = "モノクロを" + skillValue + "秒解除";
                skillDetailLong = "モノクロ状態（白黒状態）の背景に" + skillValue + "秒間色をつける。モノクロ状態ではない捜査では効果は発揮されない。";
                break;

            case 4:
                skillType = SkillType.Target;
                skillName = "ターゲット";
                skillDetail = "物証に" + skillValue + "つ印をつける";
                skillDetailLong = "" + skillValue + "つの物証に赤いターゲットマークを表示し、物証の位置を示す。";
                break;

            case 5:
                skillType = SkillType.Number;
                skillName = "順番解除";
                skillDetail = "物証の？状態を解除";
                skillDetailLong = "物証名が？で表示されている時に使うと、物証名がすべて表示される。？で表示されない捜査では効果を発揮しない。";
                break;

        }

    }


}
