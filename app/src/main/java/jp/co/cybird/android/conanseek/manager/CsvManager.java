package jp.co.cybird.android.conanseek.manager;

import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.KaiwaParam;
import jp.co.cybird.android.conanseek.param.KunrenParam;
import jp.co.cybird.android.conanseek.param.LocationParam;
import jp.co.cybird.android.conanseek.param.LoginbonusParam;
import jp.co.cybird.android.conanseek.param.MonoParam;
import jp.co.cybird.android.conanseek.param.NanidoParam;
import jp.co.cybird.android.conanseek.param.ShougenParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;

/**
 * Csv/JSONなどのアセットの中のデータを取り扱う
 */
public class CsvManager {


    /**
     * スキル情報持ちカードデータ一覧
     */
    public static Map<Integer, CardParam> cardParamsWithSkillDetail() {

        ArrayList<Map<String, String>> cardList = readCSV("csv/card.csv", CardParam.card_csv_keys, "\t", null);
        Map<Integer, Map<String, String>> skillList = skillList();

        Map<Integer, CardParam> retMap = new HashMap<>();

        for (Integer i = 0; i < cardList.size(); i++) {
            Map<String, String> row = cardList.get(i);

            CardParam card = new CardParam();
            card.mapToParam(row);
            card.addSkillParam(skillList.get(card.skillID));

            retMap.put(card.cardID, card);
        }
        return retMap;
    }

    /**
     * カード個別情報
     */
    public static CardParam cardByCardID(Integer cardID) {

        ArrayList<Map<String, String>> list = readCSV("csv/card.csv", CardParam.card_csv_keys, "\t", null);

        for (Integer i = 0; i < list.size(); i++) {
            Map<String, String> row = list.get(i);
            if (Common.parseInt(row.get("cardID")) == cardID) {
                CardParam card = new CardParam();
                card.mapToParam(row);
                return card;
            }
        }
        return null;
    }


    /**
     * スキル情報一覧
     */
    public static Map<Integer, Map<String, String>> skillList() {

        String jsonString = readJSON("json/skill.json");

        if (jsonString == null) return null;

        Map<Integer, Map<String, String>> retMap = new HashMap<>();

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("kunren");

            for (Integer i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);

                Integer id = row.getInt("number");

                Map<String, String> map = new HashMap<>();

                map.put("skill_type", row.getString("skill_type"));
                map.put("skill_value", row.getString("skill_value"));

                retMap.put(id, map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return retMap;
    }

    /**
     * スキル詳細情報
     */
    public static Map<String, String> skillBySkillID(int skillID) {

        String jsonString = readJSON("json/skill.json");

        if (jsonString == null) return null;

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("kunren");

            for (Integer i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);

                int id = row.getInt("number");

                if (id == skillID) {

                    Map<String, String> map = new HashMap<>();

                    map.put("skill_type", row.getString("skill_type"));
                    map.put("skill_value", row.getString("skill_value"));

                    return map;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    //-------------------------------------- エリア

    /**
     * エリアID-エリア名対応表
     */
    public static Map<Integer, String> areaList() {

        String[] keys = {"id", "dummy", "name"};
        ArrayList<Map<String, String>> list = readCSV("csv/area.csv", keys, "\t", null);

        Map<Integer, String> retMap = new HashMap<>();

        for (Map<String, String> row : list) {
            int id = Common.parseInt(row.get("id"));
            if (id > 0) {
                String name = row.get("name");
                retMap.put(id, name);
            }
        }

        return retMap;
    }

    //----------------------- キャラクター

    public static String tachieIdFromCharacterName(String characterName) {

        String keys[] = {"name", "id"};
        ArrayList<Map<String, String>> list = readCSV("csv/story/character.csv", keys, ",", "SJIS");

        for (Map<String, String> row : list) {

            if (row.get("name").equals(characterName)) {
                return row.get("id");
            }
        }

        return null;
    }


    public static String komadoBitmapPathFromName(String komadoName) {

        String keys[] = {"name", "id"};
        ArrayList<Map<String, String>> list = readCSV("csv/story/wipe.csv", keys, ",", "SJIS");

        int komadoID = 0;

        komadoName = komadoName.replace("　", "");
        komadoName = komadoName.replace(" ", "");

        //完全一致
        for (Map map : list) {
            if (map.get("name").equals(komadoName)) {
                String identifier = (String) map.get("id");
                komadoID = Common.parseInt(identifier);
                break;
            }
        }
        //部分一致
        if (komadoID == 0) {
            for (Map map : list) {
                if (komadoName.indexOf(map.get("name").toString()) != -1) {
                    String identifier = (String) map.get("id");
                    komadoID = Common.parseInt(identifier);
                    break;
                }
            }
        }

        if (komadoID == 0) return null;

        int chouhenNumber = 0;

        if (komadoID > 10000) {
            chouhenNumber = (int) Math.floor((float) komadoID / 10000f);
            komadoID -= chouhenNumber * 10000;
        }

        return komadoBitmapPathFormId(komadoID, chouhenNumber);
    }

    public static String komadoBitmapPathFormId(int komadoID, int chouhenNumber) {
        return bitmapImagePath(
                "komado",
                (chouhenNumber > 0) ? String.valueOf(chouhenNumber) : "normal",
                String.valueOf(komadoID),
                "png"
        );
    }

    //----------------------- ログインボーナス


    /**
     * ログインボーナスマスタ
     */
    public static Map<Integer, LoginbonusParam> loginBonusMaster() {

        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        Map<Integer, LoginbonusParam> retMap = new HashMap<>();

        try {
            inputStream = Common.myAppContext.getResources().getAssets().open("csv/login.csv");
            streamReader = new InputStreamReader(inputStream, "SJIS");

            CSVReader reader = new CSVReader(streamReader, ',', '"', 0);

            String[] line;

            while ((line = reader.readNext()) != null) {

                LoginbonusParam param = new LoginbonusParam();
                param.dayCount = Common.parseInt(line[0]);
                param.rewardName = line[2];
                param.rewardAmount = Common.parseInt(line[3]);

                retMap.put(param.dayCount, param);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("FileNotFoundException:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Common.logD("UnsupportedEncodingException:" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Common.logD("IOException:" + e.toString());
        } finally {
            try {
                if (streamReader != null)
                    streamReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }

        return retMap;
    }

    //----------------------- 背景

    public static String areaNameFromAreaId(String areaID) {

        String keys[] = {"name", "areaID", "bgID"};
        ArrayList<Map<String, String>> list = readCSV("csv/area_bg.csv", keys, "\t", "UTF-8");


        for (Map<String, String> row : list) {

            if (row.get("areaID").equals(areaID)) {
                return row.get("name");
            }
        }

        return null;
    }

    public static String areaIdFromAreaName(String areaName) {

        String keys[] = {"name", "areaID", "bgID"};
        ArrayList<Map<String, String>> list = readCSV("csv/area_bg.csv", keys, "\t", "UTF-8");

        for (Map<String, String> row : list) {

            if (row.get("name").equals(areaName)) {
                return row.get("areaID");
            }
        }
        for (Map<String, String> row : list) {

            if (row.get("name").indexOf(areaName) != -1 || areaName.indexOf(row.get("name")) != -1) {
                return row.get("areaID");
            }
        }

        return null;
    }


    public static String haikeiIdFromHaikeiName(String areaName) {

        String keys[] = {"name", "areaID", "bgID"};
        ArrayList<Map<String, String>> list = readCSV("csv/area_bg.csv", keys, "\t", "UTF-8");

        for (Map<String, String> row : list) {

            if (row.get("name").equals(areaName)) {
                return row.get("bgID");
            }
        }
        for (Map<String, String> row : list) {

            if (row.get("name").indexOf(areaName) != -1 || areaName.indexOf(row.get("name")) != -1) {
                return row.get("bgID");
            }
        }

        return null;
    }

    /**
     * エリア背景
     */
    public static String areaImageFileFromAreaID(int areaID, Boolean small) {
        if (small)
            return "area/small/" + areaID + ".jpg";
        return "area/" + areaID + ".jpg";
    }


    //----------------------- 画像

    public static String bitmapImagePath(String category, String subCategory, String name, String ext) {

        String path = Common.myAppContext.getFilesDir() + "/" + category + "__";
        if (subCategory != null && subCategory.length() > 0) path += subCategory + "__";
        path += name + "." + ext;

        return path;
    }


    //----------------------- 物探し

    /**
     * 難易度一覧
     */
    public static ArrayList<NanidoParam> nanidoList() {

        String jsonString = readJSON("json/nanido-list.json");

        if (jsonString == null) return null;

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("nanido");


            ArrayList<NanidoParam> paramArrayList = new Gson().fromJson(array.toString(), new TypeToken<ArrayList<NanidoParam>>() {
            }.getType());

            return paramArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }


    /**
     * ロケーションマスタ
     */
    public static ArrayList<LocationParam> locationMaster(int areaID) {

        String jsonString = readJSON("json/location-" + areaID + ".json");

        if (jsonString == null) return null;

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("location");


            ArrayList<LocationParam> paramArrayList = new Gson().fromJson(array.toString(), new TypeToken<ArrayList<LocationParam>>() {
            }.getType());

            return paramArrayList;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * ものマスタ
     */
    public static Map<Integer, MonoParam> monoMaster(int areaID) {

        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        Map<Integer, MonoParam> retMap = new HashMap<>();

        try {
            inputStream = Common.myAppContext.getResources().getAssets().open("csv/mono/" + areaID + "-mono.csv");
            streamReader = new InputStreamReader(inputStream, "UTF-8");

            CSVReader reader = new CSVReader(streamReader, '\t', '"', 0);

            String[] line;

            while ((line = reader.readNext()) != null) {

                MonoParam param = new MonoParam();
                param.name = line[3];
                param.fileName = line[2];
                param.areaID = Common.parseInt(line[1]);

                int id = Common.parseInt(line[0]);

                retMap.put(id, param);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("FileNotFoundException:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Common.logD("UnsupportedEncodingException:" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Common.logD("IOException:" + e.toString());
        } finally {
            try {
                if (streamReader != null)
                    streamReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }

        return retMap;
    }


    //-------------------------------------- 訓練

    /**
     * 訓練一覧
     */
    public static ArrayList<KunrenParam> kunrenList() {

        String jsonString = readJSON("json/kunren-list.json");

        Map<Integer, String> areaMap = areaList();

        if (jsonString == null) return null;

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("kunren");

            ArrayList<KunrenParam> paramArrayList = new Gson().fromJson(array.toString(), new TypeToken<ArrayList<KunrenParam>>() {
            }.getType());

            for (KunrenParam param : paramArrayList) {
                param.areaName = areaMap.get(param.area);
            }

            return paramArrayList;


        } catch (JSONException e) {
            e.printStackTrace();
            Common.logD("kunrenList parse error:" + e.toString());
        }

        return null;
    }

    /**
     * 訓練開放条件
     * - 開放済みだとnullが返る
     */
    public static String kunrenKaihouJouken(int areaID, int level, String areaName) {

        //研究所の1は開放済み
        if (areaID == 1 && level == 1) return null;

        //level2以降は前レベルを30回以上クリア
        if (level > 1) {

            int clearCount = UserInfoManager.kunrenClearCount(areaID, level - 1);

            if (clearCount < 30) {
                return "Lv." + (level - 1) + "を30回クリア";
            } else {
                return null;
            }
        }

        //level1は特定の事件をクリア
        ArrayList<JikenParam> jikenMaster = jikenMaster();

        for (JikenParam jikenParam : jikenMaster) {

            if (jikenParam.kunrenKaihou != null && jikenParam.kunrenKaihou.equals(areaName)) {

                String jikenID = jikenParam.jikenID;

                if (UserInfoManager.jikenCleared(jikenID, true)) {
                    return null;
                }

                return "事件「" + jikenParam.jikenName + "」までクリア";
            }
        }
        return null;
    }


    //-------------------------------------- 事件

    /**
     * 事件マスタ
     */
    public static ArrayList<JikenParam> jikenMaster() {

        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        ArrayList<JikenParam> retList = new ArrayList<>();

        try {
            inputStream = Common.myAppContext.getResources().getAssets().open("csv/jiken/master.csv");
            streamReader = new InputStreamReader(inputStream, "SJIS");

            CSVReader reader = new CSVReader(streamReader, '\t', '"', 0);

            String[] line;

            while ((line = reader.readNext()) != null) {

                JikenParam param = new JikenParam();
                param.fromCsv(line);

                retList.add(param);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("FileNotFoundException:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Common.logD("UnsupportedEncodingException:" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Common.logD("IOException:" + e.toString());
        } finally {
            try {
                if (streamReader != null)
                    streamReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }

        return retList;
    }


    /**
     * 事件マスタに事件詳細情報を付加
     */
    public static JikenParam addJikenDetail(JikenParam jikenParam) {

        String csvFile;
        boolean deepRead = false;

        jikenParam.hazureYougishaList = null;
        jikenParam.hazureItemList = null;
        jikenParam.fuseikaiBusshouList = null;
        //jikenParam.shougenList = null;
        jikenParam.suiriMachigaiItem = null;
        jikenParam.hazureDoukiList = null;

        if (jikenParam.chouhenFlag) {
            if (jikenParam.suiriChouhenFlag || jikenParam.shougenChouhenFlag) {

                csvFile = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/description.csv";
            } else if (jikenParam.chouhenSeriesNumber == 5) {

                deepRead = true;
                jikenParam.suiriChouhenFlag = true;
                csvFile = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/evidence.csv";

            } else if (jikenParam.chouhenSeriesNumber == 3 || jikenParam.chouhenSeriesNumber == 8) {
                deepRead = true;
                jikenParam.shougenChouhenFlag = true;
                csvFile = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/" + jikenParam.chouhenEpisodeNumber + "/evidence.csv";

            } else {

                csvFile = "csv/chouhen/" + jikenParam.chouhenSeriesNumber + "/description.csv";

            }

        } else {
            csvFile = "csv/jiken/" + jikenParam.id + "/description.csv";
        }


        InputStream inputStream = null;
        InputStreamReader streamReader = null;

        try {
            inputStream = Common.myAppContext.getResources().getAssets().open(csvFile);
            streamReader = new InputStreamReader(inputStream, "SJIS");

            CSVReader reader = new CSVReader(streamReader, ',', '"', 0);
            String[] array;

            boolean chouhenDataSourceFlag = false;

            while ((array = reader.readNext()) != null) {

                if (array.length > 1) {

                    if (jikenParam.chouhenFlag) {

                        //推理型長編
                        if (jikenParam.suiriChouhenFlag) {

                            if (deepRead) {
                                if (array.length >= 7) {
                                    String key = array[1];
                                    String val = array[7];

                                    if (key.length() > 1) {
                                        if (jikenParam.shougenList == null)
                                            jikenParam.shougenList = new ArrayList<>();

                                        ShougenParam shougenParam = new ShougenParam();
                                        shougenParam.hito = key;
                                        shougenParam.message = val;

                                        jikenParam.shougenList.add(shougenParam);
                                    }
                                }
                            } else {

                                String key = array[0];
                                String val = array[1];

                                if (key.equals("依頼者")) {
                                    jikenParam.iraishaName = val;
                                } else if (key.indexOf("捜査現場") == 0) {
                                    jikenParam.sagashiStage = val;
                                } else if (key.indexOf("正解アイテム") == 0) {
                                    jikenParam.suiriSeikaiItem = val;
                                } else if (key.indexOf("はずれアイテム") == 0) {

                                    jikenParam.suiriMachigaiItem = new ArrayList<>();

                                    String v[] = val.split("、");
                                    for (int i = 0; i < v.length; i++) {
                                        jikenParam.suiriMachigaiItem.add(v[i]);
                                    }
                                }
                            }
                        }
                        //証言型
                        else if (jikenParam.shougenChouhenFlag) {
                            if (deepRead) {
                                if (array.length >= 7) {
                                    String key = array[1];
                                    String val = array[7];

                                    if (key.length() > 1) {
                                        if (jikenParam.shougenList == null)
                                            jikenParam.shougenList = new ArrayList<>();

                                        ShougenParam shougenParam = new ShougenParam();
                                        shougenParam.hito = key;
                                        shougenParam.message = val;
                                        jikenParam.shougenList.add(shougenParam);
                                    }
                                }
                            } else {

                                String key = array[0];
                                String val = array[1];

                                if (key.equals("依頼者")) {
                                    jikenParam.iraishaName = val;
                                } else if (key.indexOf("被害者") == 0) {
                                    jikenParam.higaishaName = val;
                                } else if (key.indexOf("犯人名") == 0) {
                                    jikenParam.hanninName = val;
                                } else if (key.indexOf("捜査現場") == 0) {
                                    jikenParam.sagashiStage = val;
                                } else if (key.indexOf("正解アイテム") == 0) {
                                    jikenParam.suiriSeikaiItem = val;
                                } else if (key.indexOf("はずれアイテム") == 0) {

                                    jikenParam.suiriMachigaiItem = new ArrayList<>();

                                    String v[] = val.split("、");
                                    for (int i = 0; i < v.length; i++) {
                                        jikenParam.suiriMachigaiItem.add(v[i]);
                                    }
                                } else if (key.indexOf("はずれ容疑者") == 0) {
                                    if (jikenParam.hazureYougishaList == null)
                                        jikenParam.hazureYougishaList = new ArrayList<>();

                                    jikenParam.hazureYougishaList.add(val);
                                } else if (key.indexOf("正解動機") == 0) {
                                    jikenParam.seikaiDouki = val;
                                } else if (key.indexOf("はずれ動機") == 0) {

                                    if (jikenParam.hazureDoukiList == null)
                                        jikenParam.hazureDoukiList = new ArrayList<>();

                                    jikenParam.hazureDoukiList.add(val);
                                }
                            }
                        }
                        //暗号型長編
                        else {

                            //対象エピソードの範囲調査
                            if (array[0].indexOf("ステージ") == 0) {
                                int episode = Common.parseInt(array[0].replace("ステージ", ""));
                                chouhenDataSourceFlag = episode == jikenParam.chouhenEpisodeNumber;
                            } else if (chouhenDataSourceFlag) {

                                if (array.length >= 7) {
                                    String key = array[5];
                                    String val = array[7];

                                    if (key.indexOf("探索場所") == 0) {
                                        jikenParam.sagashiStage = val;
                                    } else if (key.indexOf("暗号") == 0) {
                                        jikenParam.angouChouhenFlag = true;
                                        jikenParam.angouString = val;
                                    } else if (key.indexOf("隠し暗号") == 0) {
                                        jikenParam.kakushiAngouString = val;
                                    } else if (key.indexOf("正解物証") == 0) {
                                        jikenParam.seikaiBusshou = val;
                                    } else if (key.indexOf("不正解物証") == 0) {
                                        if (jikenParam.fuseikaiBusshouList == null)
                                            jikenParam.fuseikaiBusshouList = new ArrayList<>();
                                        jikenParam.fuseikaiBusshouList.add(val);
                                    } else if (key.indexOf("出題者") == 0) {
                                        jikenParam.iraishaName = val;
                                    }
                                }
                            }
                        }

                    }
                    //通常事件
                    else {

                        if (array[0].indexOf("証拠品ドロップアイテム") == 0) {
                            jikenParam.shoukohin = array[3];
                        } else if (array[0].indexOf("事件現場") == 0) {
                            jikenParam.sagashiStage = array[3];
                        } else if (array[0].indexOf("依頼者") == 0) {
                            jikenParam.iraishaName = array[3];
                        } else if (array[0].indexOf("犯人名") == 0) {
                            jikenParam.hanninName = array[3];
                        } else if (array[0].indexOf("はずれ容疑者") == 0) {
                            if (jikenParam.hazureYougishaList == null)
                                jikenParam.hazureYougishaList = new ArrayList<>();
                            if (jikenParam.hazureItemList == null)
                                jikenParam.hazureItemList = new ArrayList<>();

                            String str = array[3];
                            String[] split = str.split("：");

                            String splittedyougisha = split[0];
                            String splittedItem;
                            if (split.length > 1) {
                                splittedItem = split[1];
                            } else {
                                splittedItem = array[4].replace("：", "");
                            }

                            if (splittedyougisha.length() > 0 && splittedItem.length() > 0) {
                                jikenParam.hazureYougishaList.add(splittedyougisha);
                                jikenParam.hazureItemList.add(splittedItem);
                            }
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("FileNotFoundException:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Common.logD("UnsupportedEncodingException:" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Common.logD("IOException:" + e.toString());
        } finally {
            try {
                if (streamReader != null)
                    streamReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }


        if (deepRead) {
            return addJikenDetail(jikenParam);
        }

        return jikenParam;
    }


    /**
     * 事件難易度
     */
    public static KunrenParam jikenNanido(String jikenID) {

        String jsonString = readJSON("json/jiken-list.json");

        if (jsonString == null) return null;

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray array = json.getJSONArray("jiken");

            ArrayList<KunrenParam> paramArrayList = new Gson().fromJson(array.toString(), new TypeToken<ArrayList<KunrenParam>>() {
            }.getType());

            for (KunrenParam param : paramArrayList) {
                if (param.story_id.equals(jikenID)) {
                    return param;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Common.logD("kunrenList parse error:" + e.toString());
        }

        return null;
    }

    //---------------------------------- 会話

    /**
     * 会話データ
     */
    public static ArrayList<KaiwaParam> kaiwaData(String fileName) {

        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        ArrayList<KaiwaParam> retList = new ArrayList<>();

        try {
            inputStream = Common.myAppContext.getResources().getAssets().open(fileName);
            streamReader = new InputStreamReader(inputStream, "SJIS");

            CSVReader reader = new CSVReader(streamReader, ',', '"', 0);

            String[] line;

            while ((line = reader.readNext()) != null) {

                if (line.length > 0 && line[0].length() > 0 && line[1].length() > 0 && line[1].indexOf("<") == -1) {
                    KaiwaParam param = new KaiwaParam();
                    param.fromCsv(line);
                    retList.add(param);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("FileNotFoundException:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Common.logD("UnsupportedEncodingException:" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Common.logD("IOException:" + e.toString());
        } finally {
            try {
                if (streamReader != null)
                    streamReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }
        return retList;
    }


    /**
     * チュートリアル
     */
    public static ArrayList<TutorialParam> tutorialData(String fileName) {

        InputStream inputStream = null;
        InputStreamReader streamReader = null;
        ArrayList<TutorialParam> retList = new ArrayList<>();

        try {
            inputStream = Common.myAppContext.getResources().getAssets().open("csv/tutorial/tutorial_" + fileName + ".csv");
            streamReader = new InputStreamReader(inputStream, "SJIS");

            CSVReader reader = new CSVReader(streamReader, ',', '"', 0);

            String[] line;

            while ((line = reader.readNext()) != null) {

                if (line.length > 1 && line[0].length() > 0 && line[0].indexOf("クリック") != -1) {
                    TutorialParam param = new TutorialParam();
                    param.fromCsv(line);
                    retList.add(param);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("FileNotFoundException:" + e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Common.logD("UnsupportedEncodingException:" + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Common.logD("IOException:" + e.toString());
        } finally {
            try {
                if (streamReader != null)
                    streamReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }
        return retList;
    }


    //--------------------------------------- reader

    /**
     * JSON読み込み
     */
    public static String readJSON(String fileName) {

        AssetManager assetManager = Common.myAppContext.getResources().getAssets();

        InputStream inputStream = null;
        BufferedReader reader = null;

        String jsonString = null;
        try {
            inputStream = assetManager.open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            jsonString = stringBuilder.toString();
        } catch (IOException e) {
            Common.logD("e" + e);
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }

        return jsonString;
    }


    /**
     * CSV読込
     */
    public static ArrayList<Map<String, String>> readCSV(String fileName,
                                                         String[] mapKeys, String splitter,
                                                         String characterCode) {

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        ArrayList<Map<String, String>> retList = new ArrayList<>();
        if (characterCode == null) characterCode = "UTF-8";


        try {
            AssetManager assetManager = Common.myAppContext.getResources().getAssets();
            inputStream = assetManager.open(fileName);

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, characterCode));

            {
                // 最終行まで読み込む
                for (String line; (line = bufferedReader.readLine()) != null; ) {

                    String[] array = line.split(splitter);

                    Map<String, String> map = new HashMap<>();

                    if (array.length > 0) {
                        for (Integer k = 0; k < mapKeys.length; k++) {
                            if (array.length - 1 >= k) {
                                map.put(mapKeys[k], array[k]);
                            } else {
                                map.put(mapKeys[k], "");
                            }
                        }
                    }

                    retList.add(map);
                }
                bufferedReader.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Common.logD("" + e.toString());
        } catch (IOException e) {
            //e.printStackTrace();
            Common.logD("" + e.toString());
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Common.logD("Exception:" + e.toString());
            }
        }

        return retList;
    }

}
