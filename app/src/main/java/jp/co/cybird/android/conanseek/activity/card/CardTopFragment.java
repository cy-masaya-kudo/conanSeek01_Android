package jp.co.cybird.android.conanseek.activity.card;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * カード：メニュー
 */
public class CardTopFragment extends CardContentFragment implements View.OnClickListener {

    public static CardTopFragment newInstance() {
        
        Bundle args = new Bundle();
        
        CardTopFragment fragment = new CardTopFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card_content_top, container, false);

        view.findViewById(R.id.btn_card_deck).setOnClickListener(this);
        view.findViewById(R.id.btn_card_shoji).setOnClickListener(this);
        view.findViewById(R.id.btn_card_zukan).setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {

        SeManager.play(SeManager.SeName.PUSH_BUTTON);

        if (v.getId() == R.id.btn_card_deck)
            ((CardFragment)getParentFragment()).addContentFragment(CardDeckFragment.newInstance());
        else if (v.getId() == R.id.btn_card_shoji)
            ((CardFragment)getParentFragment()).addContentFragment(CardShojiFragment.newInstance());
        else if (v.getId() == R.id.btn_card_zukan)
            ((CardFragment)getParentFragment()).addContentFragment(CardZukanFragment.newInstance());
    }
}
