package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;

import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.CustomHorizonalScroll;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * 推理：物証選択
 */
public class SuiriBusshouPopup extends BasePopup {

    private BaseButton nextButton;
    public String selectedItem;

    private ArrayList<String> busshouList;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;

    public static SuiriBusshouPopup newInstance(ArrayList<String> hazureList, String seikaiItem, boolean noShuffle) {

        Bundle args = new Bundle();

        ArrayList<String> busshouList = new ArrayList<>();
        busshouList.addAll(hazureList);
        busshouList.add(seikaiItem);
        if (!noShuffle) {
            Collections.shuffle(busshouList);
        }

        args.putStringArrayList("list", busshouList);

        SuiriBusshouPopup fragment = new SuiriBusshouPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_suiri_busshou, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            busshouList = arg.getStringArrayList("list");
        }

        view.findViewById(R.id.dialog_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null)
                    buttonListener.pushedNegativeClick(SuiriBusshouPopup.this);
                removeMe();
            }
        });
        nextButton = (BaseButton) view.findViewById(R.id.dialog_right);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null)
                    buttonListener.pushedPositiveClick(SuiriBusshouPopup.this);
                if (busshouSelectListenerListener != null)
                    busshouSelectListenerListener.selectBusshou(selectedItem, SuiriBusshouPopup.this);
            }
        });
        nextButton.setEnabled(false);

        CustomHorizonalScroll scrollView = (CustomHorizonalScroll) view.findViewById(R.id.scrollView);
        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setEnabled(false);
        rightArrow.setEnabled(false);

        if (busshouList.size() <= 3) {
            leftArrow.setVisibility(View.GONE);
            rightArrow.setVisibility(View.GONE);
        } else {
            scrollView.setOnCustomHorizonalScrollListener(new CustomHorizonalScroll.CustomHorizonalScrollListener() {
                @Override
                public void onCustomHorizonalScrollToTop(CustomHorizonalScroll scrollView) {
                    leftArrow.setBlink(false);
                }

                @Override
                public void onCustomHorizonalScrollFree(CustomHorizonalScroll scrollView) {
                    leftArrow.setBlink(true);
                    rightArrow.setBlink(true);
                }

                @Override
                public void onCustomHorizonalScrollToBottom(CustomHorizonalScroll scrollView) {
                    rightArrow.setBlink(false);
                }
            });
            leftArrow.setBlink(false);
            rightArrow.setBlink(true);
        }

        BaseButton dummyButton = (BaseButton) view.findViewById(R.id.btn_dummy);
        final LinearLayout parentView = (LinearLayout) dummyButton.getParent();

        for (final String str : busshouList) {

            BaseButton button = new BaseButton(getContext());

            button.setLayoutParams(dummyButton.getLayoutParams());
            button.setBackground(null);
            button.setPadding(
                    dummyButton.getPaddingLeft(),
                    dummyButton.getPaddingTop(),
                    dummyButton.getPaddingRight(),
                    dummyButton.getPaddingBottom()
            );
            button.setScaleType(ImageView.ScaleType.FIT_XY);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    SeManager.play(SeManager.SeName.PUSH_CONTENT);
                    //全てのアイテムの選択状態をリセット
                    for (int i = 0; i < parentView.getChildCount(); i++) {
                        BaseButton child = (BaseButton) parentView.getChildAt(i);
                        child.setBackground(null);
                    }

                    //今回のアイテムを選択
                    v.setBackgroundResource(R.mipmap.frame_suspectselect);

                    //今回のアイテムの文字情報を更新
                    selectedItem = str;

                    //次へボタン有効
                    nextButton.setEnabled(true);
                }
            });

            button.setImageBitmap(Common.decodedBitmap(
                    CsvManager.komadoBitmapPathFromName(str),
                    100, 100
            ));

            if (str.equals("注射器")) {
                tutorialChoiceButton = button;
            }

            parentView.addView(button);
        }

        dummyButton.setVisibility(View.GONE);

        return view;
    }

    private BaseButton tutorialChoiceButton = null;

    protected void tutorialChoice() {

        tutorialChoiceButton.setBackgroundResource(R.mipmap.frame_suspectselect);

        //今回のアイテムの文字情報を更新
        selectedItem = "注射器";

        //次へボタン有効
        nextButton.setEnabled(true);
    }



    public interface BusshouSelectListener extends EventListener {
        void selectBusshou(String selectedBusshou, SuiriBusshouPopup busshouDialogFragment);
    }

    protected BusshouSelectListener busshouSelectListenerListener = null;

    public void setBusshouSelectListener(BusshouSelectListener l) {
        busshouSelectListenerListener = l;
    }

}
