package jp.co.cybird.android.conanseek.manager;

import android.media.MediaPlayer;

import jp.souling.android.conanseek01.R;

/**
 * BGM管理
 */
public class BgmManager {

    private static MediaPlayer MP_PLAYER;
    private static int CURRENT_BGM_ID;
    private static boolean BGM_PAUSED = false;

    public enum BgmName {
        PAUSE,
        CONTINUE,
        CONFIG_ON,
        SPLASH,
        MAIN,
        JIKEN,
        KUNREN,
        SUIRI,
        KAIWA_NORMAL,
        KAIWA_CHOUHEN,
        SOUSA,
        SOUSA_LIMIT,
        JIKEN_CLEAR,
        JIKEN_FAIL,
        SOUSA_CLEAR,
        SOUSA_FAIL,
        GACHA,
    }


    public static void setBgm(BgmName bgmName) {

        int identifier = 0;
        boolean repeatFlag = true;


        switch (bgmName) {

            case PAUSE:
                identifier = 0;
                break;
            case CONTINUE:
            case CONFIG_ON:
                identifier = -1;
                break;
            case SPLASH:
                identifier = R.raw.bgm1;
                break;
            case MAIN:
                identifier = R.raw.bgm2;
                break;
            case JIKEN:
                identifier = R.raw.bgm3;
                break;
            case KUNREN:
                identifier = R.raw.bgm4;
                break;
            case SUIRI:
                identifier = R.raw.bgm5;
                break;
            case KAIWA_NORMAL:
                identifier = R.raw.bgm6;
                break;
            case KAIWA_CHOUHEN:
                identifier = R.raw.bgm7;
                break;
            case SOUSA:
                identifier = R.raw.bgm8;
                break;
            case SOUSA_LIMIT:
                identifier = R.raw.bgm9;
                break;
            case JIKEN_CLEAR:
                identifier = R.raw.bgm10;
                repeatFlag = false;
                break;
            case JIKEN_FAIL:
                identifier = R.raw.bgm11;
                repeatFlag = false;
                break;
            case SOUSA_CLEAR:
                identifier = R.raw.bgm13;
                repeatFlag = false;
                break;
            case SOUSA_FAIL:
                identifier = R.raw.bgm11;
                repeatFlag = false;
                break;
            case GACHA:
                identifier = R.raw.bgm14;
                break;
        }

        boolean canPlay = !SaveManager.boolValue(SaveManager.KEY.SOUND_BGM_DISABLE__boolean, false);

        Common.logD("canPlay:"+canPlay);

        //BGM停止
        if (identifier == 0) {

            if (MP_PLAYER != null) {
                MP_PLAYER.pause();
                BGM_PAUSED = true;
            }
        }
        //BGM復帰
        else if (identifier == -1) {

            if (BGM_PAUSED) {
                if (canPlay) {
                    if (MP_PLAYER.isLooping()) {
                        MP_PLAYER.start();
                    }
                }
            }
            BGM_PAUSED = false;
            CURRENT_BGM_ID = -1;
        }
        //BGM変更
        else {

            if (identifier != CURRENT_BGM_ID) {

                if (MP_PLAYER != null) {
                    MP_PLAYER.stop();
                    MP_PLAYER = null;
                }
                Common.logD("MP_PLAYER:"+MP_PLAYER);
                if (identifier > 0) {
                    MP_PLAYER = MediaPlayer.create(Common.myAppContext, identifier);
                    MP_PLAYER.setLooping(repeatFlag);
                    MP_PLAYER.setVolume(0.15f, 0.15f);
                    if (canPlay)
                        MP_PLAYER.start();
                }
                CURRENT_BGM_ID = identifier;
                BGM_PAUSED = false;
            }
        }

    }
}
