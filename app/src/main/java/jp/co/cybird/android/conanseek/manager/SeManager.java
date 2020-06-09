package jp.co.cybird.android.conanseek.manager;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.util.Inventory;

/**
 * BGM管理
 */
public class SeManager {

    private static SoundPool SE_PLAYER;
    private static int OnlyPrepareCount = 0;
    private static Map<SeName, Integer> SE_NAME_ID_MAP = new HashMap<SeName, Integer>();

    public enum SeName {
        SPLAH_SHOW_A,
        SPLAH_SHOW_B,
        SPLSH_END,
        OSHIRASE,
        LOGINBONUS,
        PRESENT_ALERT,

        PUSH_BUTTON,
        PUSH_CONTENT,
        SWIPE,
        PUSH_BACK,
        SHOW_POPUP,

        HIDE_POPUP,
        STEP_KAIWA,
        START_SOUSA,
        PAUSE_SOUSA,
        RESTART_SOUSA,

        CLEAR_SOUSA,
        FAIL_SOUSA,
        SHOW_BUSSHOU,
        CLEAR_JIKEN,
        FAIL_JIKEN,

        NEW_JIKEN,
        NEW_CHOUHEN,
        REWARD_KUNREN,
        REWARD_MISSION,
        SHOP_COIN,

        SHOP_MEGANE,
        SHOP_HEART,
        ERROR_NETWORK,
        ERROR_MENTE,
        GACHA_KICK,

        GACHA_RARITY_N,
        GACHA_RARITY_HN,
        GACHA_RARITY_R,
        GACHA_RARITY_SR,
        GACHA_RARITY_SSR,

        SKILL_TIME_PLUS,
        SKILL_TIME_MINUS,
        SKILL_ROTATION,
        SKILL_COLORED,
        SKILL_MONOKURO,

        SKILL_TARGET,
        GACHA_DOOR_OPEN
    }

    //private static Integer soundIds[];

    private static void initPlayer() {
        SE_PLAYER = new SoundPool(43, AudioManager.STREAM_MUSIC, 0);
        SE_PLAYER.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    OnlyPrepareCount--;
                    if (OnlyPrepareCount < 0) {

                        float volume = 1.0f;

                        for (SeName key : SE_NAME_ID_MAP.keySet()) {
                            if (SE_NAME_ID_MAP.get(key) == sampleId) {
                                volume = getVolume(key);
                                break;
                            }
                        }
                        soundPool.play(sampleId, volume, volume, 1, 0, 1.0F);
                    }
                }
            }
        });
        //soundIds = new Integer[43];
    }

    public static void resetPrepareCount() {
        OnlyPrepareCount = 0;
    }

    public static void onlyPrepare(SeName seName){
        OnlyPrepareCount++;
        prepareSound(seName);
    }

    public static Integer prepareSound(SeName seName){

        if (SE_PLAYER == null)
            initPlayer();

        int index = 0;
        int identifier = 0;

        switch (seName) {
            case SPLAH_SHOW_A:
                index = 0;
                identifier = R.raw.se_1_1;
                break;
            case SPLAH_SHOW_B:
                index = 1;
                identifier = R.raw.se_1_2;
                break;
            case SPLSH_END:
                index = 2;
                identifier = R.raw.se_2;
                break;
            case OSHIRASE:
                index = 3;
                identifier = R.raw.se_3;
                break;
            case LOGINBONUS:
                index = 4;
                identifier = R.raw.se_4;
                break;
            case PRESENT_ALERT:
                index = 5;
                identifier = R.raw.se_5;
                break;
            case PUSH_BUTTON:
                index = 6;
                identifier = R.raw.se_6;
                break;
            case PUSH_CONTENT:
                index = 7;
                identifier = R.raw.se_7;
                break;
            case SWIPE:
                index = 8;
                identifier = R.raw.se_8;
                break;
            case PUSH_BACK:
                index = 9;
                identifier = R.raw.se_9;
                break;
            case SHOW_POPUP:
                index = 10;
                identifier = R.raw.se_10;
                break;
            case HIDE_POPUP:
                index = 11;
                identifier = R.raw.se_11;
                break;
            case STEP_KAIWA:
                index = 12;
                identifier = R.raw.se_12;
                break;
            case START_SOUSA:
                index = 13;
                identifier = R.raw.se_13;
                break;
            case PAUSE_SOUSA:
                index = 14;
                identifier = R.raw.se_14;
                break;
            case RESTART_SOUSA:
                index = 15;
                identifier = R.raw.se_15;
                break;
            case CLEAR_SOUSA:
                index = 16;
                identifier = R.raw.se_16;
                break;
            case FAIL_SOUSA:
                index = 17;
                identifier = R.raw.se_17;
                break;
            case SHOW_BUSSHOU:
                index = 18;
                identifier = R.raw.se_18;
                break;
            case CLEAR_JIKEN:
                index = 19;
                identifier = R.raw.se_19;
                break;
            case FAIL_JIKEN:
                index = 20;
                identifier = R.raw.se_20;
                break;
            case NEW_JIKEN:
                index = 21;
                identifier = R.raw.se_21;
                break;
            case NEW_CHOUHEN:
                index = 22;
                identifier = R.raw.se_22;
                break;
            case REWARD_KUNREN:
                index = 23;
                identifier = R.raw.se_23;
                break;
            case REWARD_MISSION:
                index = 24;
                identifier = R.raw.se_24;
                break;
            case SHOP_COIN:
                index = 25;
                identifier = R.raw.se_25;
                break;
            case SHOP_MEGANE:
                index = 26;
                identifier = R.raw.se_26;
                break;
            case SHOP_HEART:
                index = 27;
                identifier = R.raw.se_27;
                break;
            case ERROR_NETWORK:
                index = 28;
                identifier = R.raw.se_28;
                break;
            case ERROR_MENTE:
                index = 29;
                identifier = R.raw.se_29;
                break;
            case GACHA_KICK:
                index = 30;
                identifier = R.raw.se_30;
                break;
            case GACHA_RARITY_N:
                index = 31;
                identifier = R.raw.se_31;
                break;
            case GACHA_RARITY_HN:
                index = 32;
                identifier = R.raw.se_32;
                break;
            case GACHA_RARITY_R:
                index = 33;
                identifier = R.raw.se_33;
                break;
            case GACHA_RARITY_SR:
                index = 34;
                identifier = R.raw.se_34;
                break;
            case GACHA_RARITY_SSR:
                index = 35;
                identifier = R.raw.se_35;
                break;
            case SKILL_TIME_PLUS:
                index = 36;
                identifier = R.raw.se_36;
                break;
            case SKILL_TIME_MINUS:
                index = 37;
                identifier = R.raw.se_37;
                break;
            case SKILL_ROTATION:
                index = 38;
                identifier = R.raw.se_38;
                break;
            case SKILL_COLORED:
                index = 39;
                identifier = R.raw.se_39;
                break;
            case SKILL_MONOKURO:
                index = 40;
                identifier = R.raw.se_40;
                break;
            case SKILL_TARGET:
                index = 41;
                identifier = R.raw.se_41;
                break;
            case GACHA_DOOR_OPEN:
                index = 42;
                identifier = R.raw.se_42;
                break;
        }

        if (SE_NAME_ID_MAP.containsKey(seName)) {
            return SE_NAME_ID_MAP.get(seName);
        } else {
            SE_NAME_ID_MAP.put(seName, SE_PLAYER.load(Common.myAppContext, identifier, 1));
        }

        return 0;
    }

    public static void play(SeName seName) {

        if (!SaveManager.boolValue(SaveManager.KEY.SOUND_SE_DISABLE__boolean, false)) {
            int identifier = prepareSound(seName);
            if (identifier > 0) {
                float volume = getVolume(seName);
                SE_PLAYER.play(identifier, volume, volume, 1, 0, 1.0F);
            }
        }
    }

    private static float getVolume(SeName seName) {
        switch (seName) {
            case PAUSE_SOUSA:
                return 1.0f;
            case GACHA_DOOR_OPEN:
                return 1.0f;
            default:
                return 0.5f;
        }
    }

}
