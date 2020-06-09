package jp.co.cybird.android.conanseek.activity.card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

import static jp.co.cybird.android.conanseek.manager.SaveManager.KEY;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_FAV;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_GET;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_NUM;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_RARE;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_SCOL;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_SDIR;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_SNUM;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_STIME;
import static jp.co.cybird.android.conanseek.manager.SaveManager.ORDER_TGT;
import static jp.co.cybird.android.conanseek.manager.SaveManager.integerValue;
import static jp.co.cybird.android.conanseek.manager.SaveManager.updateInegerValue;

/**
 * ソート
 */
public class SortPopup extends BasePopup implements View.OnClickListener {

    private ImageButton getButton;
    private ImageButton numButton;
    private ImageButton rareButton;
    private ImageButton favButton;
    private ImageButton stgtButton;
    private ImageButton stimeButton;
    private ImageButton sdirButton;
    private ImageButton snumButton;
    private ImageButton scolButton;

    public static SortPopup newInstance() {
        
        Bundle args = new Bundle();
        
        SortPopup fragment = new SortPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.popup_sort, container, false);


        view.findViewById(R.id.dialog_close).setOnClickListener(this);

        getButton = (ImageButton) view.findViewById(R.id.btn_get);
        getButton.setOnClickListener(this);
        numButton = (ImageButton) view.findViewById(R.id.btn_number);
        numButton.setOnClickListener(this);
        rareButton = (ImageButton) view.findViewById(R.id.btn_rarity);
        rareButton.setOnClickListener(this);
        favButton = (ImageButton) view.findViewById(R.id.btn_favorite);
        favButton.setOnClickListener(this);
        stgtButton = (ImageButton) view.findViewById(R.id.btn_skill_target);
        stgtButton.setOnClickListener(this);
        stimeButton = (ImageButton) view.findViewById(R.id.btn_skill_time);
        stimeButton.setOnClickListener(this);
        sdirButton = (ImageButton) view.findViewById(R.id.btn_skill_dir);
        sdirButton.setOnClickListener(this);
        snumButton = (ImageButton) view.findViewById(R.id.btn_skill_num);
        snumButton.setOnClickListener(this);
        scolButton = (ImageButton) view.findViewById(R.id.btn_skill_color);
        scolButton.setOnClickListener(this);

        updateButtonsState();

        return view;
    }

    /**
     * ボタン押下状態更新
     */
    public void updateButtonsState() {

        Integer current = integerValue(KEY.CARD_ORDER__integer, 0);

        getButton.setSelected( (current == SaveManager.ORDER_GET) );
        numButton.setSelected( (current == SaveManager.ORDER_NUM) );
        rareButton.setSelected( (current == SaveManager.ORDER_RARE) );
        favButton.setSelected( (current == SaveManager.ORDER_FAV) );
        stgtButton.setSelected( (current == SaveManager.ORDER_TGT));
        stimeButton.setSelected( (current == SaveManager.ORDER_STIME) );
        sdirButton.setSelected( (current == SaveManager.ORDER_SDIR) );
        snumButton.setSelected( (current == SaveManager.ORDER_SNUM) );
        scolButton.setSelected( (current == SaveManager.ORDER_SCOL) );
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.dialog_close) {
            SeManager.play(SeManager.SeName.PUSH_BACK);
            removeMe();
        } else {

            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            int order = ORDER_GET;

            switch (v.getId()) {
                case R.id.btn_get:
                    order = ORDER_GET;
                    break;
                case R.id.btn_number:
                    order = ORDER_NUM;
                    break;
                case R.id.btn_rarity:
                    order = ORDER_RARE;
                    break;
                case R.id.btn_favorite:
                    order = ORDER_FAV;
                    break;
                case R.id.btn_skill_target:
                    order = ORDER_TGT;
                    break;
                case R.id.btn_skill_time:
                    order = ORDER_STIME;
                    break;
                case R.id.btn_skill_dir:
                    order = ORDER_SDIR;
                    break;
                case R.id.btn_skill_num:
                    order = ORDER_SNUM;
                    break;
                case R.id.btn_skill_color:
                    order = ORDER_SCOL;
                    break;
                default:
                    break;
            }
            //ソート方法記録更新
            updateInegerValue(KEY.CARD_ORDER__integer, order);

            if (SortPopup.this.buttonListener != null) {
                SortPopup.this.buttonListener.pushedPositiveClick(SortPopup.this);
            }
            removeMe();
        }
    }
}
