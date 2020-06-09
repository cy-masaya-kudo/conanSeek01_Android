package jp.co.cybird.android.conanseek.manager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import jp.co.cybird.android.conanseek.common.BaseDialogFragment;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

/**
 * ダウンロード画面
 */
public class APIDownloadingFragment extends BaseDialogFragment {

    //プログレスバー
    private ImageView progressMeter;

    //プログレスバー最大長さ
    private int progressMaxWidth = 0;

    //ダウンロード終了時のプログレスバー幅
    private float downloadMeterWidthPercent = 50.0f / 100.0f;

    //カード群の親
    private ViewGroup nagareruFrame;

    //カードのデータ
    private ArrayList<Integer> cardIdList = new ArrayList<Integer>();

    //カードデータのindex
    private int cardIndex = 0;

    //ダウンロード中テキスト
    private TextView downloadingText;

    public static APIDownloadingFragment newInstance() {
        return new APIDownloadingFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_api_unzip, container);

        progressMeter = (ImageView) view.findViewById(R.id.unzip_bar);
        progressMeter.setVisibility(View.INVISIBLE);

        nagareruFrame = (ViewGroup) view.findViewById(R.id.nagareru_card);

        downloadingText = (TextView) view.findViewById(R.id.download_text);

        //カードID配列
        for (int i = 1; i <= Settings.totalCards; i++) {
            cardIdList.add(i);
        }
        Collections.shuffle(cardIdList);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        typingHandler.sendEmptyMessage(1);
        addNextCard();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        nagareruFrame.setAnimation(null);

        while (nagareruFrame.getChildCount() > 0){
            ImageView v = (ImageView)nagareruFrame.getChildAt(0);
            v.setAnimation(null);
            v.setImageBitmap(null);
            v.setImageDrawable(null);
            nagareruFrame.removeView(v);
        }


    }


    /**
     * カード追加
     * １枚のカードが右画面から画面内右にまで流れる。画面内右から左画面外まで流れるの２つのアニメーションを組み合わせる。
     * 画面内右まで流れた所で次のカードを作る。
     */
    public void addNextCard() {

        //絵柄
        int currentCardIndex = cardIndex % Settings.totalCards;
        int cardID = cardIdList.get(currentCardIndex);
        cardIndex++;

        //通常解像度 220, 300 横の動きなので横解像度を下げてもわかりにくい
        Bitmap newImage = Common.decodedAssetBitmap("egara/326x460/" + cardID + ".jpg", 140, 300, 0.3f);

        //枠
        View view = getView().findViewById(R.id.card_frame_0);

        ImageView imageView = new ImageView(getActivity().getApplicationContext());
        imageView.setLayoutParams(view.getLayoutParams());
        imageView.setImageBitmap(newImage);

        nagareruFrame.addView(imageView);

        new CardAnimation(imageView).start();
    }

    private int trueDp(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    //アニメーション
    private class CardAnimation {

        //アニメーション
        private TranslateAnimation translateAnimationRightToCenter;
        private TranslateAnimation translateAnimationCenterToLeft;

        private ImageView cardView;

        public CardAnimation (ImageView cardView) {
            this.cardView = cardView;
        }

        protected void start() {

            //アニメ1
            //640+120 => 640 - 120 - 10 => 0 - 120
            int duration1 = 120 * 15;
            int duration2 = 660 * 15;
            translateAnimationRightToCenter = new TranslateAnimation(trueDp(540+120), trueDp(540), 0, 0);
            translateAnimationRightToCenter.setDuration(duration1);
            translateAnimationRightToCenter.setFillAfter(true);
            translateAnimationRightToCenter.setInterpolator(new LinearInterpolator());
            translateAnimationRightToCenter.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    addNextCard();
                    cardView.setAnimation(translateAnimationCenterToLeft);
                    translateAnimationCenterToLeft.start();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            translateAnimationCenterToLeft = new TranslateAnimation(trueDp(540), trueDp(0-120), 0, 0);
            translateAnimationCenterToLeft.setDuration(duration2);
            translateAnimationCenterToLeft.setFillAfter(true);
            translateAnimationCenterToLeft.setInterpolator(new LinearInterpolator());
            translateAnimationCenterToLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ViewGroup parent = ((ViewGroup) cardView.getParent());
                    cardView.setImageBitmap(null);
                    cardView.setImageDrawable(null);
                    if (parent != null) {
                        parent.removeView(cardView);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

            });

            cardView.setAnimation(translateAnimationRightToCenter);
            translateAnimationRightToCenter.start();
        }



    }



    //メーター更新
    public void updateMeter(boolean downloading, int percent) {


        if (progressMaxWidth == 0) {
            progressMeter.setVisibility(View.VISIBLE);
            progressMaxWidth = progressMeter.getWidth();
        }

        int width = (int) (((float) progressMaxWidth * downloadMeterWidthPercent) * ((float) percent / 100.0F));
        if (!downloading) {
            width += (int) ((float) progressMaxWidth * downloadMeterWidthPercent);
        }

        ViewGroup.LayoutParams layout = progressMeter.getLayoutParams();
        layout.width = width;
        progressMeter.setLayoutParams(layout);


    }


    //ダウンロード中テキスト
    private Handler typingHandler = new Handler() {

        private int index = 0;
        final private String bodyString = "データダウンロード中";
        final private String extraString = "...";
        private String putString = "";

        @Override
        public void dispatchMessage(Message message) {

            char data[] = extraString.toCharArray();

            int arr_num = data.length;

            if (index < arr_num) {
                if (message.what == 1) {
                    String word = String.valueOf(data[index]);
                    putString += word;

                    downloadingText.setText(bodyString + putString);
                    typingHandler.sendEmptyMessageDelayed(1, 1030);
                    index++;
                } else {
                    super.dispatchMessage(message);
                }
            } else {
                putString = "";
                index = 0;
                typingHandler.sendEmptyMessage(1);
            }
        }
    };
}
