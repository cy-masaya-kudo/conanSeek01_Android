package jp.co.cybird.android.conanseek.activity.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

public class ShopFragment extends BaseFragment implements View.OnClickListener {


    public static ShopFragment newInstance() {

        Bundle args = new Bundle();
        
        ShopFragment fragment = new ShopFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        bgmName = BgmManager.BgmName.MAIN;

        view.findViewById(R.id.btn_shop_coin).setOnClickListener(this);
        view.findViewById(R.id.btn_shop_heart).setOnClickListener(this);
        view.findViewById(R.id.btn_shop_megane).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        SeManager.play(SeManager.SeName.PUSH_BUTTON);

        if (v.getId() == R.id.btn_shop_coin) {
            showPopup(CoinPopup.newInstance());
        } else if (v.getId() == R.id.btn_shop_heart) {
            showPopup(HeartPopup.newInstance());
        } else if (v.getId() == R.id.btn_shop_megane) {
            showPopup(MeganePopup.newInstance());
        }
    }

    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
        if (!tutorialList.contains("shop")) {
            startTutorial("shop");
        }
    }
}
