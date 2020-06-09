package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * 暗号
 */
public class AngouPopup extends BasePopup {


    public static AngouPopup newInstance() {
        
        Bundle args = new Bundle();
        
        AngouPopup fragment = new AngouPopup();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_angou, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                removeMe();
            }
        });

        return view;
    }

}
