package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.common.ArrowButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.common.CustomHorizonalScroll;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * 暗号取得
 */
public class AngouRewardPopup extends BasePopup {

    private ArrayList<String> angouList;

    private ArrowButton leftArrow;
    private ArrowButton rightArrow;

    public static AngouRewardPopup newInstance(ArrayList<String> angouList) {

        Bundle args = new Bundle();

        args.putStringArrayList("list", angouList);

        AngouRewardPopup fragment = new AngouRewardPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_angou_reward, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            angouList = arg.getStringArrayList("list");
        }

        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null)
                    buttonListener.pushedPositiveClick(AngouRewardPopup.this);
                removeMe();
            }
        });

        TextView motoView = (TextView) view.findViewById(R.id.angou_cell);

        LinearLayout parentView = (LinearLayout) motoView.getParent();


        CustomHorizonalScroll scrollView = (CustomHorizonalScroll) view.findViewById(R.id.scrollView);
        leftArrow = (ArrowButton) view.findViewById(R.id.arrowLeft);
        rightArrow = (ArrowButton) view.findViewById(R.id.arrowRight);
        leftArrow.setEnabled(false);
        rightArrow.setEnabled(false);

        if (angouList.size() <= 3) {
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



        for (String str : angouList) {

            TextView textView = new TextView(getContext());

            textView.setLayoutParams(motoView.getLayoutParams());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(0xff000000);
            textView.setBackgroundResource(R.mipmap.icon_cipher_sample);
            textView.setText(str);

            parentView.addView(textView);
        }

        motoView.setVisibility(View.GONE);

        return view;
    }

}
