package jp.co.cybird.android.conanseek.activity.gacha;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BaseCell;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.CardParam;
import jp.souling.android.conanseek01.R;

/**
 * 出現カード
 */
public class ShutsugenPopup extends BasePopup {

    private ViewPager viewPager;
    private int totalPages;
    private int currentPage;

    private TextView pageControl;

    private APIResponseParam.Item.GachaParam gachaParam;


    public static ShutsugenPopup newInstance(APIResponseParam.Item.GachaParam gachaParam) {

        Bundle args = new Bundle();
        args.putSerializable("gachaParam", gachaParam);

        ShutsugenPopup fragment = new ShutsugenPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_shutsugen, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            gachaParam = (APIResponseParam.Item.GachaParam) arg.getSerializable("gachaParam");
        }

        //ページビュー
        viewPager = (ViewPager) view.findViewById(R.id.card_view_pager);
        totalPages = (int) (Math.ceil((float) gachaParam.display.card.size() / 10.0F));
        currentPage = 0;
        setMyPageAdapter();

        //ページコントロール
        pageControl = (TextView) view.findViewById(R.id.pagecontrol);
        updatePageControl();


        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BACK);
                removeMe();
            }
        });


        //取得済みメーター
        TextView subtitle = (TextView) view.findViewById(R.id.shutsugen_subtitle);
        TextView wariLeft = (TextView) view.findViewById(R.id.shutsugen_text_left);
        TextView wariRight = (TextView) view.findViewById(R.id.shutsugen_text_right);
        String[] list = {"SSR", "SR", "R", "HN", "N"};

        if (gachaParam.delivery_type.equals("ボックス")) {

            subtitle.setText("カード総数");
            wariLeft.setVisibility(View.INVISIBLE);
            wariRight.setVisibility(View.INVISIBLE);

            //ViewGroup parent = (ViewGroup) wariLeft.getParent();
            wariLeft.setVisibility(View.GONE);
            wariRight.setVisibility(View.GONE);

            float meterWidth = Common.myAppContext.getResources().getDimension(R.dimen.dialog_shutsugen_meters_width);
            meterWidth -= Common.myAppContext.getResources().getDimension(R.dimen.dialog_shutsugen_meters_padding) * 2.0F;

            for (String slug : list) {

                int identifier = Common.myAppContext.getResources().getIdentifier("shutsugen_" + slug.toLowerCase(), "id", getActivity().getPackageName());
                View v = view.findViewById(identifier);

                if (gachaParam.display.total_amount.get(slug) == null) {

                    //ないレアリティのビューは排除
                    v.setVisibility(View.GONE);

                } else {

                    TextView titleText = (TextView) v.findViewById(R.id.shutsugen_bar_title);
                    titleText.setText("[" + slug + "]カード");

                    TextView rateText = (TextView) v.findViewById(R.id.shutsugen_bar_rate);
                    String rate =
                            gachaParam.display.total_gotten.get(slug)
                                    + "/"
                                    + gachaParam.display.total_amount.get(slug);
                    rateText.setText(rate);

                    View meterView = v.findViewById(R.id.shutsugen_bar_meter);
                    float percent = (float) gachaParam.display.total_gotten.get(slug) / (float) gachaParam.display.total_amount.get(slug);
                    ViewGroup.LayoutParams layoutParams = meterView.getLayoutParams();
                    layoutParams.width = (int) Math.ceil(meterWidth * percent);
                    meterView.setLayoutParams(layoutParams);
                }
            }
        } else {

            subtitle.setText("レアリティ別\n出現確率");
            wariLeft.setVisibility(View.VISIBLE);
            wariRight.setVisibility(View.VISIBLE);

            String leftText = "";
            String rightText = "";
            int totalRate = 0;

            for (String slug : list) {

                int identifier = Common.myAppContext.getResources().getIdentifier("shutsugen_" + slug.toLowerCase(), "id", getActivity().getPackageName());
                View v = view.findViewById(identifier);
                //ViewGroup parent = (ViewGroup) v.getParent();
                v.setVisibility(View.GONE);

                if (gachaParam.display.total_amount.get(slug) != null) {

                    int rate = (int) Math.ceil((float) gachaParam.display.frequency.get(slug) / (float) gachaParam.display.total_frequency * 100.0F);
                    totalRate += rate;

                    if (totalRate > 98) {
                        rate = rate + (100 - totalRate);
                    }

                    leftText += slug + "\n";
                    wariLeft.setText(leftText);

                    rightText += rate + "%\n";
                    wariRight.setText(rightText);
                }
            }
        }

        return view;
    }


    private void setMyPageAdapter() {

        viewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                updatePageControl();
                SeManager.play(SeManager.SeName.SWIPE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    private void updatePageControl() {

        String str = " ";
        for (int i = 0; i < totalPages; i++) {
            if (i == currentPage)
                str += "<font color='#98654a'>●</font> ";
            else
                str += "<font color='#d09a78'>●</font> ";
        }
        pageControl.setText(Html.fromHtml(str));
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
            bundle.putIntegerArrayList("data", gachaParam.display.card);
            bundle.putInt("position", position);
            f.setArguments(bundle);

            return f;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
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

            return inflater.inflate(R.layout.fragment_gacha_shutsugen_list, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);


            Bundle arg = getArguments();
            ArrayList<Integer> cardList = new ArrayList<>();
            int position = 0;
            if (arg != null) {
                cardList = arg.getIntegerArrayList("data");
                position = arg.getInt("position");
            }

            Map<Integer, CardParam> cardParamMap = CsvManager.cardParamsWithSkillDetail();

            for (int i = 0; i < 10; i++) {

                int identifier = Common.myAppContext.getResources().getIdentifier("list_cell_" + i, "id", getActivity().getPackageName());
                ViewGroup cell = (ViewGroup) view.findViewById(identifier);

                if (cardList.size() > i + position * 10) {
                    cell.setVisibility(View.VISIBLE);
                } else {
                    cell.setVisibility(View.INVISIBLE);
                    continue;
                }

                final Integer number = cardList.get(i + position * 10);
                CardParam cardParam = cardParamMap.get(number);

                cell.findViewById(R.id.cell_new).setVisibility(View.INVISIBLE);
                cell.findViewById(R.id.cell_fav).setVisibility(View.INVISIBLE);

                BaseButton cardFace = (BaseButton) cell.findViewById(R.id.cell_card);

                cardFace.setImageBitmap(Common.decodedAssetBitmap("egara/180x254/" + number + ".jpg", 60, 90));


                ImageView skillImage = (ImageView) cell.findViewById(R.id.cell_skill);


                switch (cardParam.skillType) {

                    case Time:
                        skillImage.setImageResource(R.mipmap.skill_time_icon);
                        break;
                    case Direction:
                        skillImage.setImageResource(R.mipmap.skill_direction);
                        break;
                    case Color:
                        skillImage.setImageResource(R.mipmap.skill_color);
                        break;
                    case Target:
                        skillImage.setImageResource(R.mipmap.skill_target);
                        break;
                    case Number:
                        skillImage.setImageResource(R.mipmap.skill_order);
                        break;
                }
            }
        }
    }
}
