package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * チュートリアルパラメーター
 */
public class TutorialParam implements Serializable {

    public String extra_tutorialCode;

    public String serifuText;
    public int serifuPos;

    public String characterName;
    public int characterHyoujou;
    public int characterIdentifier;
    public int characterPos;

    public String sousaCode;

    public int wait;

    public int pointer;

    public String effectCode;

    public String clickme;

    public void fromCsv(String[] arrow) {

        if (arrow.length < 2) return;

        this.clickme = arrow[0];
        if (this.clickme.indexOf("クリック") == -1) return;

        this.characterName = arrow[1];

        this.serifuText = arrow[2];

        if (arrow[4].length() > 0 || arrow[10].length() > 0) {

            this.characterPos = arrow[4].length() > 0 ? 0 : 1;

            int gap = this.characterPos == 0 ? 0 : 6;

            this.characterHyoujou = Common.parseInt(arrow[3 + gap]);
            this.characterIdentifier = Common.parseInt(arrow[5 + gap]);
        }

        this.sousaCode = arrow[18];

        String code = arrow[19];
        if (code.indexOf("上") == 0)
            this.serifuPos = 1;
        else if (code.indexOf("下") == 0)
            this.serifuPos = 2;
        else
            this.serifuPos = 0;

        this.wait = Common.parseInt(arrow[20]);

        code = arrow[21];
        if (code.indexOf("左上") != -1) pointer = 1;
        else if (code.indexOf("右上") != -1) pointer = 2;
        else pointer = 0;


        effectCode = arrow[22];


    }


}
