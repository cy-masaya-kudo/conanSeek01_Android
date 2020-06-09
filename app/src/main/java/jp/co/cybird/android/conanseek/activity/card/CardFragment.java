package jp.co.cybird.android.conanseek.activity.card;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import jp.co.cybird.android.conanseek.activity.gacha.GachaFragment;
import jp.co.cybird.android.conanseek.activity.jiken.JikenFragment;
import jp.co.cybird.android.conanseek.activity.kunren.KunrenFragment;
import jp.co.cybird.android.conanseek.activity.kunren.KunrenLevelPopup;
import jp.co.cybird.android.conanseek.activity.kunren.MissionListPopup;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.BgmManager;
import jp.co.cybird.android.conanseek.manager.CacheManager;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.co.cybird.android.conanseek.param.APIResponseParam;
import jp.co.cybird.android.conanseek.param.JikenParam;
import jp.co.cybird.android.conanseek.param.KunrenParam;
import jp.co.cybird.android.conanseek.param.TutorialParam;
import jp.souling.android.conanseek01.R;

/**
 * カード
 */
public class CardFragment extends BaseFragment {

    //最初に出すフラグメント
    private CardContentFragment rootFragment;

    //事件のデッキ編集に戻る際の事件詳細パラメーター
    private JikenParam jikenParam;

    //戻り先名
    private String returnName;


    public static CardFragment newInstance(CardContentFragment rootFragment, String returnName, JikenParam jikenParam) {

        Bundle args = new Bundle();

        CacheManager.instance().cardRootFragment = rootFragment;
        args.putString("return",returnName);
        args.putSerializable("jikenParam", jikenParam);

        CardFragment fragment = new CardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card, container, false);

        bgmName = BgmManager.BgmName.MAIN;

        Bundle arg = getArguments();

        if (arg != null) {

            returnName = arg.getString("return");
            jikenParam = (JikenParam)arg.getSerializable("jikenParam");
        }

        rootFragment = CacheManager.instance().cardRootFragment;
        CacheManager.instance().cardRootFragment = null;

        if (rootFragment == null) {
            rootFragment = CardTopFragment.newInstance();
        }

        addContentFragment(rootFragment);

        return view;
    }


    @Override
    protected void fragmentDidAppear() {
        super.fragmentDidAppear();

        if (rootFragment.getClass() == CardDeckFragment.class) {
            ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
            if (!tutorialList.contains("jiken_3") && tutorialList.contains("jiken_2")) {
                startTutorial("jiken_3");
            }
        } else if (rootFragment.getClass() == CardTopFragment.class) {

            ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
            if (!tutorialList.contains("card")) {
                startTutorial("card");
            }
        } else if (rootFragment.getClass() == CardShojiDetailFragment.class) {

            ArrayList<String> tutorialList = SaveManager.stringArray(SaveManager.KEY.TUTORIAL__stringList);
            if (!tutorialList.contains("gacha_2")) {
                startTutorial("gacha_2");
            }
        }

    }

    //---tutorial

    @Override
    public void didEndTutorial() {
        super.didEndTutorial();
    }

    @Override
    public void pushedTarget(TutorialParam param) {
        super.pushedTarget(param);

        if (param.sousaCode.indexOf("デッキ編集") != -1) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            addContentFragment(CardDeckFragment.newInstance());
        } else if (param.sousaCode.indexOf("所持カードタッチ") != -1) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            addContentFragment(CardShojiFragment.newInstance());
        } else if (param.sousaCode.indexOf("図鑑") != -1) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            addContentFragment(CardZukanFragment.newInstance());
        } else if (param.sousaCode.indexOf("プラスボタン") != -1) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);
            addContentFragment(CardSelectFragment.newInstance(false, SaveManager.deckListByDeckIndex(-1), 0));
        } else if (param.sousaCode.indexOf("所持カード") != -1) {
            SeManager.play(SeManager.SeName.PUSH_BUTTON);

            ArrayList<Integer> selectedSerials = SaveManager.deckListByDeckIndex(-1);
            ArrayList<APIResponseParam.Item.Card> mySortedCardList = UserInfoManager.mySortedCardList(-1);
            APIResponseParam.Item.Card card = mySortedCardList.get(0);
            selectedSerials.remove(0);
            selectedSerials.add(0, card.id);
            SaveManager.updateDeck(selectedSerials);

            Common.logD("selectedSerials:"+selectedSerials);

            for (Fragment f : getChildFragmentManager().getFragments()) {
                if (f instanceof CardDeckFragment) {
                    ((CardDeckFragment) f).updateTable();
                }
            }
            for (Fragment f : getChildFragmentManager().getFragments()) {
                if (f instanceof CardSelectFragment) {
                    popContentFragment((CardContentFragment) f);
                }
            }


        } else if (param.sousaCode.indexOf("戻るボタン") != -1) {

            if (rootFragment.getClass() == CardDeckFragment.class) {

                SeManager.play(SeManager.SeName.PUSH_BACK);

                for (Fragment f : getChildFragmentManager().getFragments()) {
                    if (f instanceof CardDeckFragment) {
                        popContentFragment((CardContentFragment) f);
                        break;
                    }
                }

            } else if (rootFragment.getClass() == CardTopFragment.class) {

                SeManager.play(SeManager.SeName.PUSH_BACK);

                Fragment lastFragment = null;
                for (Fragment f : getChildFragmentManager().getFragments()) {
                    if (f instanceof CardContentFragment) {
                        lastFragment = f;
                    }
                }
                popContentFragment((CardContentFragment) lastFragment);
            } else if (rootFragment.getClass() == CardShojiDetailFragment.class) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                ((BaseActivity) getActivity()).replaceViewController(GachaFragment.newInstance(null));
            }
        }
    }

    //--------


    //コンテンツフラグメント追加
    public void addContentFragment(CardContentFragment fragment) {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        ft.add(R.id.card_content_frame, fragment);

        ft.commit();

        //これまでのフラグメント全て非表示
        for (Fragment f : getChildFragmentManager().getFragments()) {
            if (f instanceof CardContentFragment && !fragment.equals(f)) {
                ft.hide(f);
            }
        }
    }

    //一つ前のコンテンツフラグメント参照
    public CardContentFragment getPrevContentFragment(CardContentFragment myFragment) {

        Fragment lastFragment = null;

        for (Fragment f : getChildFragmentManager().getFragments()) {
            if (f instanceof CardContentFragment) {

                if (myFragment.equals(f)) {
                    break;
                }
                lastFragment = f;
            }
        }
        return (CardContentFragment) lastFragment;
    }

    //コンテンツフラグメント戻り
    public void popContentFragment(CardContentFragment fragment) {

        android.support.v4.app.FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        ft.remove(fragment);

        ft.commit();


        CardContentFragment lastFragment = null;

        for (Fragment f : getChildFragmentManager().getFragments()) {
            if (f instanceof CardContentFragment && !fragment.equals(f)) {
                lastFragment = (CardContentFragment)f;
            }
        }

        if (lastFragment != null) {
            //一番上のフラグメント表示
            ft.show(lastFragment);
            lastFragment.willShowAgain();


        } else if (returnName != null) {
            //何もなければ前のビューコントローラーフラグメントへ戻る
            if (returnName.equals("kunren")) {
                ((BaseActivity) getActivity()).replaceViewController(KunrenFragment.newInstance());
            } else if (returnName.equals("jiken")) {
                if (jikenParam != null)
                    ((BaseActivity) getActivity()).replaceViewController(JikenFragment.newInstance(jikenParam));
                else
                    ((BaseActivity) getActivity()).replaceViewController(JikenFragment.newInstance(null));
            } else if (returnName.equals("gacha")) {
                ((BaseActivity) getActivity()).replaceViewController(GachaFragment.newInstance(null));
            }
        }
    }
}
