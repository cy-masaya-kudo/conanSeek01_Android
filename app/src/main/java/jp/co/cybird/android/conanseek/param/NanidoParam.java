package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;

import jp.co.cybird.android.conanseek.manager.Common;

/**
 * 難易度パラメーター
 */
public class NanidoParam implements Serializable {

    public boolean isKunren;

    private String area;
    private String card;
    private String color;
    private String jun;
    private String level;
    private String mono;
    private String number;
    private String rotate;
    private String time;

    public int getArea() {
        return Common.parseInt(area);
    }

    public int getCard() {
        return Common.parseInt(card);
    }

    public int getColor() {
        return Common.parseInt(color);
    }

    public boolean getJun() {
        return jun.equals("true");
    }

    public int getLevel() {
        return Common.parseInt(level);
    }

    public int getMono() {
        return Common.parseInt(mono);
    }

    public int getNumber() {
        return Common.parseInt(number);
    }

    public int getRotate() {
        return Common.parseInt(rotate);
    }

    public int getTime() {
        return Common.parseInt(time);
    }
}
