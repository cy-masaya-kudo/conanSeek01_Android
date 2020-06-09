package jp.co.cybird.android.conanseek.activity.jiken;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jp.co.cybird.android.conanseek.common.BasePopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.CsvManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.souling.android.conanseek01.R;

/**
 * 推理：物証確認
 */
public class SuiriBusshouConfirmPopup extends BasePopup {

    private String busshouName;

    public static SuiriBusshouConfirmPopup newInstance(String busshouName) {

        Bundle args = new Bundle();

        args.putString("busshou", busshouName);

        SuiriBusshouConfirmPopup fragment = new SuiriBusshouConfirmPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.popup_suiri_confirm, container, false);

        Bundle arg = getArguments();
        if (arg != null) {
            this.busshouName = arg.getString("busshou");
        }

        view.findViewById(R.id.dialog_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SeManager.play(SeManager.SeName.PUSH_BACK);
                if (buttonListener != null) {
                    buttonListener.pushedNegativeClick(SuiriBusshouConfirmPopup.this);
                }
                removeMe();
            }
        });
        view.findViewById(R.id.dialog_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null) {
                    buttonListener.pushedPositiveClick(SuiriBusshouConfirmPopup.this);
                }
            }
        });

        int meganeCount = UserInfoManager.meganeCount();

        String message = "物証はこちらで良いですか？<br>"
                + "虫眼鏡を1つ使用します。<br>"
                + "<img src=\"icon_megane\"> " + meganeCount + " → <img src=\"icon_megane\">" + (meganeCount - 1);


        //dialog_message_text
        ((TextView) view.findViewById(R.id.dialog_message_text)).setText(
                (Html.fromHtml(
                        message,
                        new Common.ResouroceImageGetter(getContext()), null)

                )
        );

        //komado_image
        ((ImageView) view.findViewById(R.id.komado_image)).setImageBitmap(Common.decodedBitmap(
                CsvManager.komadoBitmapPathFromName(busshouName),
                72, 72
        ));

        showSound = SeManager.SeName.SHOW_BUSSHOU;


        return view;
    }

}
