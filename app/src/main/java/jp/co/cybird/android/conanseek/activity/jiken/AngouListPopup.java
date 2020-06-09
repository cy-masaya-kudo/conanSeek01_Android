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

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * 暗号一覧
 */
public class AngouListPopup extends BasePopup {

    private int totalAngou;
    private String kakushiAngou;
    private ArrayList<String> angouList = new ArrayList<>();

    public static AngouListPopup newInstance(int totalAngou, String kakushiAngou, ArrayList<String> angouList) {

        Bundle args = new Bundle();

        args.putInt("total", totalAngou);
        args.putString("kakushi", kakushiAngou);
        args.putStringArrayList("list", angouList);

        AngouListPopup fragment = new AngouListPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_angou_list, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            totalAngou = arg.getInt("total");
            this.kakushiAngou = arg.getString("kakushi");
            angouList = arg.getStringArrayList("list");
        }

        view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null)
                    buttonListener.pushedNegativeClick(AngouListPopup.this);
                removeMe();
            }
        });

        TextView dummy = (TextView) view.findViewById(R.id.angou_cell_dummy);

        ArrayList<String> kakushiArray = new ArrayList<>();
        if (this.kakushiAngou != null) {
            for (int i = 0; i < this.kakushiAngou.length(); i++) {
                kakushiArray.add(this.kakushiAngou.substring(i, i + 1));
            }
        }

        int kakushiIndex = 0;
        Common.logD("angouList:"+angouList);
        Common.logD("angouList:"+angouList);
        for (int row = 0; row < 3; row++) {

            int identifier = Common.myAppContext.getResources().getIdentifier("angou_row_" + (row + 1), "id", getActivity().getPackageName());
            LinearLayout parent = (LinearLayout) view.findViewById(identifier);

            for (int col = 0; col < 7; col++) {

                int index = row * 7 + col;

                if (totalAngou > index) {

                    TextView textView = new TextView(getContext());

                    textView.setLayoutParams(dummy.getLayoutParams());
                    textView.setTextColor(0xff000000);
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);

                    if (kakushiArray.size() > 0 ) {
                        textView.setBackgroundResource(R.mipmap.kakushiangou);
                        kakushiArray.remove(0);
                        kakushiIndex++;
                    } else {
                        textView.setBackgroundResource(R.mipmap.icon_cipher_sample);
                        if (angouList.size() > index - kakushiIndex) {
                            String str = angouList.get(index - kakushiIndex);
                            textView.setText(str);
                        }
                    }


                    parent.addView(textView);
                }

            }
        }

        dummy.setVisibility(View.GONE);

        return view;
    }


}
