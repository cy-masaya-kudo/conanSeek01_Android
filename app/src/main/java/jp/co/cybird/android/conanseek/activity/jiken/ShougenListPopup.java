package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.ShougenParam;
import jp.souling.android.conanseek01.R;

/**
 * 証言一覧
 */
public class ShougenListPopup extends BasePopup {

    ViewPager viewPager;
    int totalPages;
    int currentPage;
    boolean noSoundSwipe = false;

    TextView pageControl;

    ArrayList<ShougenParam> shougenList = new ArrayList<>();

    private String jikenID;
    private int pageIndex;

    public static ShougenListPopup newInstance(String jikenID, int pageIndex) {

        Bundle args = new Bundle();

        args.putString("jikenID", jikenID);
        args.putInt("pageIndex", pageIndex);

        ShougenListPopup fragment = new ShougenListPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_suiri_shougen, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            jikenID = arg.getString("jikenID");
            pageIndex = arg.getInt("pageIndex");
        }

        ArrayList<JikenParam> jikenMaster = CsvManager.jikenMaster();
        for (JikenParam param : jikenMaster) {
            if (param.jikenID.equals(jikenID)) {

                param = CsvManager.addJikenDetail(param);

                int shougenIndex = 0;
                ArrayList<Integer> kikikomizumiShougenIndexList = SaveManager.kikikomizumiShougenIndexList(jikenID);

                shougenList = new ArrayList<>();

                Common.logD("kikikomizumiShougenIndexList:" + kikikomizumiShougenIndexList);

                int truePageIndex = 0;

                for (ShougenParam shougenParam : param.shougenList) {
                    if (kikikomizumiShougenIndexList.contains(shougenIndex)) {
                        shougenList.add(shougenParam);
                        if (shougenIndex == pageIndex) {
                            pageIndex = truePageIndex;
                        }
                        truePageIndex++;
                    }
                    shougenIndex++;
                }

                break;
            }
        }
        Common.logD("shougen first index:" + pageIndex);


        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        currentPage = pageIndex;
        setMyPageAdapter();

        //ページコントロール
        pageControl = (TextView) view.findViewById(R.id.pagecontrol);
        updatePageControl();

        //タブボタン
        Button dummyButton = (Button) view.findViewById(R.id.btn_dummy);
        LinearLayout parentLayout = (LinearLayout) dummyButton.getParent();
        ArrayList<String> tabTexts = new ArrayList<>();

        for (ShougenParam shougenParam : shougenList) {

            if (!tabTexts.contains(shougenParam.hito)) {

                tabTexts.add(shougenParam.hito);

                Button button = new Button(getContext());
                button.setLayoutParams(dummyButton.getLayoutParams());
                button.setText(shougenParam.hito);
                button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                button.setBackground(null);
                button.setBackgroundResource(R.drawable.corner_radius_blue_wrapper);
                button.setTextColor(0xffffffff);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SeManager.play(SeManager.SeName.PUSH_BUTTON);
                        String name = (String) ((Button) v).getText();
                        scrollToNameTop(name);
                    }
                });

                parentLayout.addView(button);
            }
        }
        dummyButton.setVisibility(View.GONE);

        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                removeMe();
            }
        });

        return view;
    }


    private void setMyPageAdapter() {

        totalPages = shougenList.size();

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updatePageControl();

                if (!noSoundSwipe) {
                    SeManager.play(SeManager.SeName.SWIPE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        noSoundSwipe = true;
        viewPager.setCurrentItem(currentPage, false);
        noSoundSwipe = false;
    }


    private void updatePageControl() {

        if (pageControl != null) {

            String str = " ";
            for (int i = 0; i < totalPages; i++) {
                if (i == currentPage)
                    str += "<font color='#60ffffff'>●</font> ";
                else
                    str += "<font color='#60000000'>●</font> ";
            }
            pageControl.setText(Html.fromHtml(str));
        }
    }


    //発言者の項目までスクロール
    private void scrollToNameTop(String name) {

        int index = 0;
        for (ShougenParam shougenParam : shougenList) {
            if (shougenParam.hito.equals(name)) {
                viewPager.setCurrentItem(index);
                break;
            }
            index++;
        }

    }

    /**
     * カスタムアダプター
     */
    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            MyPageCell f = new MyPageCell();

            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getCount() {
            return totalPages;
        }
    }


    /**
     * セル
     */
    private class MyPageCell extends BaseCell {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.table_cell_suiri_shougen, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Bundle arg = getArguments();
            int position = 0;
            if (arg != null) {
                position = arg.getInt("position");
            }

            ShougenParam shougenParam = shougenList.get(position);

            //komado
            ((ImageView) view.findViewById(R.id.komado_image)).setImageBitmap(Common.decodedBitmap(
                    CsvManager.komadoBitmapPathFromName(shougenParam.hito),
                    90, 90
            ));

            String message = shougenParam.message;

            //message
            if (message.indexOf("[[[") > 0) {
                //暗号メッセージ

                int angouStart = message.indexOf("[[[");
                int angouEnd = message.indexOf("]]]");

                String angou = message.substring(angouStart + 3, angouEnd);

                message = message.replace("[[[" + angou + "]]]", "");

                String angouImage = "";

                //１文字ずつ画像を当てはめる
                for (int i = 0; i < angou.length(); i++) {
                    String x = angou.substring(i, i + 1);
                    angouImage += "<img src=\"angou_" + hiraganaToRoman(x) + "\">";
                }

                message = message.substring(0, angouStart)
                        + angouImage
                        + message.substring(angouStart, message.length());

            }

            ((TextView) view.findViewById(R.id.shougen_text)).setText(
                    Html.fromHtml(
                            message,
                            new Common.ResouroceImageGetter(getContext()), null)
            );
        }
    }


    private String hiraganaToRoman(String hiragana) {

        Map<String, String> m = new HashMap<>();

        m.put("あ", "a");
        m.put("い", "i");
        m.put("う", "u");
        m.put("え", "e");
        m.put("お", "o");
        m.put("か", "ka");
        m.put("き", "ki");
        m.put("く", "ku");
        m.put("け", "ke");
        m.put("こ", "ko");
        m.put("さ", "sa");
        m.put("し", "si");
        m.put("す", "su");
        m.put("せ", "se");
        m.put("そ", "so");
        m.put("た", "ta");
        m.put("ち", "ti");
        m.put("つ", "tu");
        m.put("て", "te");
        m.put("と", "to");
        m.put("な", "na");
        m.put("に", "ni");
        m.put("ぬ", "nu");
        m.put("ね", "ne");
        m.put("の", "no");
        m.put("は", "ha");
        m.put("ひ", "hi");
        m.put("ふ", "hu");
        m.put("へ", "he");
        m.put("ほ", "ho");
        m.put("ま", "ma");
        m.put("み", "mi");
        m.put("む", "mu");
        m.put("め", "me");
        m.put("も", "mo");
        m.put("や", "ya");
        m.put("ゆ", "yu");
        m.put("よ", "yo");
        m.put("ら", "ra");
        m.put("り", "ri");
        m.put("る", "ru");
        m.put("れ", "re");
        m.put("ろ", "ro");
        m.put("わ", "wa");
        m.put("を", "wo");
        m.put("ん", "n");
        m.put("が", "ga");
        m.put("ぎ", "gi");
        m.put("ぐ", "gu");
        m.put("げ", "ge");
        m.put("ご", "go");
        m.put("ざ", "za");
        m.put("じ", "zi");
        m.put("ず", "zu");
        m.put("ぜ", "ze");
        m.put("ぞ", "zo");
        m.put("だ", "da");
        m.put("ぢ", "di");
        m.put("づ", "du");
        m.put("で", "de");
        m.put("ど", "do");
        m.put("ば", "ba");
        m.put("び", "bi");
        m.put("ぶ", "bu");
        m.put("べ", "be");
        m.put("ぼ", "bo");
        m.put("ぱ", "pa");
        m.put("ぴ", "pi");
        m.put("ぷ", "pu");
        m.put("ぺ", "pe");
        m.put("ぽ", "po");

        return m.get(hiragana);

    }
}
