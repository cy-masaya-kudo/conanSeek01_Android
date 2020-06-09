package jp.co.cybird.android.conanseek.common;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;

import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * メッセージダイアログ
 */
public class MessagePopup extends BasePopup implements Serializable {

    private String messageText;
    private String negativeText;
    private String positiveText;

    //ポジティブ押下で勝手に閉じない
    public boolean positiveNoClose = false;

    public static MessagePopup newInstance(String messageText, String negativeText, String positiveText) {

        Bundle args = new Bundle();

        args.putString("message", messageText);
        args.putString("negative", negativeText);
        args.putString("positive", positiveText);

        MessagePopup fragment = new MessagePopup();
        fragment.setArguments(args);
        return fragment;
    }

    public static MessagePopup newInstance(String messageText) {
        return newInstance(messageText, null, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_message, container, false);



        Bundle args = getArguments();
        if (args != null) {
            messageText = args.getString("message", null);
            negativeText = args.getString("negative", null);
            positiveText = args.getString("positive", null);
        }



        TextView textView = (TextView) view.findViewById(R.id.dialog_message_text);
        textView.setText(Html.fromHtml(
                        messageText,
                        new Common.ResouroceImageGetter(getContext()), null)
        );

        view.findViewById(R.id.dialog_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeManager.play(SeManager.SeName.PUSH_BUTTON);
                if (buttonListener != null)
                    buttonListener.pushedNegativeClick(MessagePopup.this);
                removeMe();
            }
        });

        if (negativeText != null) {

            BaseButton button = (BaseButton) view.findViewById(R.id.dialog_left);
            button.setImageResource(R.mipmap.btn_plain);

            TextView label = (TextView) view.findViewById(R.id.dialog_left_text);
            label.setText(negativeText);
        }

        if (positiveText == null) {
            View dialogRight = view.findViewById(R.id.dialog_right);
            ((ViewGroup)dialogRight.getParent()).setVisibility(View.GONE);

        } else {

            BaseButton button = (BaseButton) view.findViewById(R.id.dialog_right);
            button.setImageResource(R.mipmap.btn_plain);

            TextView label = (TextView) view.findViewById(R.id.dialog_right_text);
            label.setText(positiveText);

            view.findViewById(R.id.dialog_right).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SeManager.play(SeManager.SeName.PUSH_BUTTON);
                    if (buttonListener != null)
                        buttonListener.pushedPositiveClick(MessagePopup.this);
                    if (!positiveNoClose)
                        removeMe();
                }
            });
        }


        return view;
    }


}