package jp.co.cybird.android.conanseek.activity.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import jp.co.cybird.android.conanseek.activity.card.CardFragment;
import jp.co.cybird.android.conanseek.activity.gacha.GachaFragment;
import jp.co.cybird.android.conanseek.activity.jiken.JikenFragment;
import jp.co.cybird.android.conanseek.activity.kunren.KunrenFragment;
import jp.co.cybird.android.conanseek.activity.shop.ShopFragment;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * 左メニューボタン
 */
public class LeftMenuFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_left_menu, container, false);

        ImageButton cardButton = (ImageButton) view.findViewById(R.id.leftMenuCard);
        ImageButton kunrenButton = (ImageButton) view.findViewById(R.id.leftMenuKunren);
        ImageButton jikenButton = (ImageButton) view.findViewById(R.id.leftMenuJiken);
        ImageButton gachaButton = (ImageButton) view.findViewById(R.id.leftMenuGacha);
        ImageButton shopButton = (ImageButton) view.findViewById(R.id.leftMenuShop);

        gachaButton.setOnClickListener(this);
        shopButton.setOnClickListener(this);
        cardButton.setOnClickListener(this);
        jikenButton.setOnClickListener(this);
        kunrenButton.setOnClickListener(this);

        //表示中フラグメントのボタンをチェック状態に
        cardButton.setSelected((getParentFragment().getClass() == CardFragment.class));
        kunrenButton.setSelected((getParentFragment().getClass() == KunrenFragment.class));
        jikenButton.setSelected((getParentFragment().getClass() == JikenFragment.class));
        gachaButton.setSelected((getParentFragment().getClass() == GachaFragment.class));
        shopButton.setSelected((getParentFragment().getClass() == ShopFragment.class));

        return view;
    }

    @Override
    public void onClick(View v) {

        CacheManager.instance().gachaParamArrayList = null;
        CacheManager.instance().gachaSelectedIndex = 0;

        CacheManager.instance().jikenSousaParam = null;


        SeManager.play(SeManager.SeName.PUSH_BUTTON);

        if (v.getId() == R.id.leftMenuCard)
            ((BaseActivity) getActivity()).replaceViewController(CardFragment.newInstance(null, null, null));
        else if (v.getId() == R.id.leftMenuKunren)
            ((BaseActivity) getActivity()).replaceViewController(KunrenFragment.newInstance());
        else if (v.getId() == R.id.leftMenuJiken)
            ((BaseActivity) getActivity()).replaceViewController(JikenFragment.newInstance(null));
        else if (v.getId() == R.id.leftMenuShop)
            ((BaseActivity) getActivity()).replaceViewController(ShopFragment.newInstance());
        else if (v.getId() == R.id.leftMenuGacha)
            ((BaseActivity) getActivity()).replaceViewController(GachaFragment.newInstance(null));
    }
}
