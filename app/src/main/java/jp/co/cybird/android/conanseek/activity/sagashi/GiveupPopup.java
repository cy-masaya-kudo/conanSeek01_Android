package jp.co.cybird.android.conanseek.activity.sagashi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * ギブアップ
 */
public class GiveupPopup extends BasePopup implements View.OnClickListener {

    public static GiveupPopup newInstance() {
        
        Bundle args = new Bundle();
        
        GiveupPopup fragment = new GiveupPopup();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.popup_giveup, container, false);

        view.findViewById(R.id.dialog_close).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_yes).setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close || v.getId() == R.id.btn_cancel) {
            SeManager.play(SeManager.SeName.PUSH_BACK);

            if (buttonListener != null)
                buttonListener.pushedNegativeClick(GiveupPopup.this);

            removeMe();
        }
        else if (v.getId() == R.id.btn_yes) {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            if (buttonListener != null)
                buttonListener.pushedPositiveClick(GiveupPopup.this);
        }
    }
}
