package jp.co.cybird.android.conanseek.param;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * APIレスポンス
 */
public class APIResponseParam {

    /**
     * バージョン
     */
    public class Version {
        public String latest_version;
    }


    /**
     * 状態情報
     */
    public State state;

    public class State {
        /**
         * コード
         */
        public Integer code;
        /**
         * メッセージ
         */
        public String message;
    }

    /**
     * API情報
     */
    public API api;

    public class API {
        /**
         * サンドボックス
         */
        public boolean sandbox;
        /**
         * 実行スクリプト
         */
        public String script;
    }

    /**
     * レスポンスアイテム
     */
    public Item item;

    public class Item {

        /**
         * リザルト
         */
        public Object result;
        //public Boolean result;

        /**
         * トランザクション
         */
        public Object transaction;

        public class PresentFireResult {
            public int ticket;
            public int megane;
            public int card;
            public int coin;
            public int heart;
        }

        /**
         * 特典
         */
        public class Tokuten {
            public String present_key;
            public String present_value;
        }

        /**
         * 通貨
         */
        public Tsuuka tsuuka;

        public class Tsuuka {
            public int login_day_count;
            public String last_login_date;

            public Coin coin;

            public class Coin {
                public int count;
            }

            public Heart heart;

            public class Heart {
                public int count;
                public ArrayList<Integer> timer;
            }

            public Megane megane;

            public class Megane {
                public int count;
                public ArrayList<Integer> timer;
            }

            public Ticket ticket;

            public class Ticket {
                public int count;
            }
        }

        /**
         * ログイン
         */
        public Object login;

        public class Login {
            public int count;
            public int amount;
            public String reward;
        }

        /**
         * 事件
         */
        public ArrayList<String> jiken;

        /**
         * 事件クリア
         */

        public Object clear_jiken;

        public class JikenClear {
            public String jiken_id;
            public String reward;
        }


        /**
         * ミッションクリア
         */

        public MissionClear clear_kunren;

        public class MissionClear {
            public int area_id;
            public int level;
            public int clear_count;
            public MissionRewardParam reward;

            public class MissionRewardParam {
                public String reward;
                public String amount;
                public String kaihou_area;
                public String kaihou_level;
            }
        }

        /**
         * 訓練
         */
        public ArrayList<Kunren> kunren;

        public class Kunren {
            public Integer area_id;
            public Integer clear_count;
            public Integer level;
        }

        /**
         * カード
         */
        public ArrayList<Card> card;

        public class Card {
            public int card_id;
            public int id;
            public int rareInt;
        }

        /**
         * ガチャ結果
         */
        public ArrayList<Card> get_card;

        /**
         * ユーザー情報
         */
        public User user;

        public class User {
            public String ap_uuid;
        }

        /**
         * 図鑑
         */
        public ArrayList<Integer> zukan;

        /**
         * プレゼント
         */
        public Integer present;

        /**
         * プレゼントリスト
         */
        public ArrayList<PresentParam> present_list;

        public class PresentParam {
            public String fuyo_key;
            public Integer fuyo_value;
            public Integer kigen_nokori;
            public Integer present_id;
            public String present_type;
            public String proc_id;
        }

        /**
         * ミッション
         */
        public ArrayList<Mission> mission;

        public class Mission {
            public Integer amount_int;
            public Integer area_id;
            public Integer level;
            public String reward;
            public String reward_amount;
            public Boolean cleared;
        }

        /**
         * バナー
         */
        public ArrayList<Banner> banner;

        public class Banner {
            public String img;
            public String url;
        }

        /**
         * ガチャ
         */
        public ArrayList<GachaParam> gacha_list;

        public class GachaParam implements Serializable {
            public Integer delivery_id;
            public String delivery_name;
            public String delivery_type;
            public Display display;
            public ArrayList<Gacha> gacha;
            public String image_file;

            public class Display implements Serializable {
                public ArrayList<Integer> card;
                public Integer card_amount;
                public Integer consume_amount;
                public String consume_item;
                public String require_more;
                public String require_rarity;
                public Map<String, Integer> frequency;
                public Integer total_frequency;
                public Integer gacha_id;
                public Map<String, Integer> total_amount;
                public Map<String, Integer> total_gotten;
            }

            public class Gacha implements Serializable {
                int card_amount;
                int consume_amount;
                String consume_item;
                int gacha_id;
                String require_more;
                String require_rarity;
            }
        }
    }

}
