package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;

import jp.co.cybird.android.conanseek.common.BaseButton;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * 推理：動機
 */
public class SuiriDoukiPopup extends BasePopup {


    private Button[] doukiButtons = new Button[3];
    private int selectedButtonIndex;
    public String selectedDoukiText;
    private BaseButton nextButton;

    private ArrayList<String> doukiList;

    public static SuiriDoukiPopup newInstance(ArrayList<String> hazureList, String seikaiItem) {

        Bundle args = new Bundle();

        ArrayList<String> doukiList = new ArrayList<>();
        doukiList.addAll(hazureList);
        doukiList.add(seikaiItem);
        Collections.shuffle(doukiList);

        args.putStringArrayList("list", doukiList);

        SuiriDoukiPopup fragment = new SuiriDoukiPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_suiri_douki, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            this.doukiList = arg.getStringArrayList("list");
        }

        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null) {
                    buttonListener.pushedNegativeClick(SuiriDoukiPopup.this);
                }
                removeMe();
            }
        });

        view.findViewById(R.id.btn_shougen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null) {
                    buttonListener.pushedPositiveClick(SuiriDoukiPopup.this);
                }
            }
        });
        view.findViewById(R.id.btn_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null) {
                    buttonListener.pushedNegativeClick(SuiriDoukiPopup.this);
                }
                removeMe();
            }
        });
        nextButton = (BaseButton) view.findViewById(R.id.btn_next);
        nextButton.setEnabled(false);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null) {
                    buttonListener.pushedPositiveClick(SuiriDoukiPopup.this);
                }
            }
        });


        doukiButtons[0] = (Button) view.findViewById(R.id.douki_button1);
        doukiButtons[1] = (Button) view.findViewById(R.id.douki_button2);
        doukiButtons[2] = (Button) view.findViewById(R.id.douki_button3);

        for (int i = 0; i < 3; i++) {
            doukiButtons[i].setText(doukiList.get(i));
            final int finalI = i;
            doukiButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    SeManager.play(SeManager.SeName.PUSH_CONTENT);

                    doukiButtons[0].setBackgroundResource(R.drawable.corner_suiri_not_selected);
                    doukiButtons[1].setBackgroundResource(R.drawable.corner_suiri_not_selected);
                    doukiButtons[2].setBackgroundResource(R.drawable.corner_suiri_not_selected);

                    selectedDoukiText = (String) doukiButtons[finalI].getText();

                    if (v.getId() == R.id.douki_button2) {
                        selectedButtonIndex = 1;
                    } else if (v.getId() == R.id.douki_button3) {
                        selectedButtonIndex = 2;
                    } else {
                        selectedButtonIndex = 0;
                    }

                    doukiButtons[selectedButtonIndex].setBackgroundResource(R.drawable.corner_suiri_selected);
                    nextButton.setEnabled(true);
                }
            });
        }


        return view;
    }


}
