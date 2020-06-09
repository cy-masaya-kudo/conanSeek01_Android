package jp.co.cybird.android.conanseek.activity.sagashi;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.param.LocationParam;
import jp.souling.android.conanseek01.R;

public class SagashiHeaderFragment extends android.support.v4.app.Fragment {

    //タイマー
    private Timer timer;
    private Handler timerHandler = new android.os.Handler();
    //開始秒
    private long startDate;
    //ポーズ開始秒
    private long pauseDate;
    //ポーズ累積秒
    private long pauseTime;
    //ペナルティ秒
    private long penaltyTime;
    //モノクロ解除秒
    private long monchromeTime;
    //向き戻る秒
    private long rotateBackTime;
    //リミット秒
    private long limitTime;
    //ラベル
    private TextView timerLabel;
    //タイムリミット間近中フラグ
    private boolean timeLimitAlertFlag;



    //メーター
    private ImageView meterImage;
    private ImageView meterFrame;



    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.fragment_sagashi_header, container, false);

        timerLabel = (TextView) view.findViewById(R.id.timer_text);
        meterImage = (ImageView) view.findViewById(R.id.timer_meter);
        meterFrame = (ImageView) view.findViewById(R.id.timer_blank);

        return view;
    }

    //----------------------- ターゲットラベル

    /**
     * ヘッダーラベル更新
     */
    public void updateHeaderTargets(ArrayList<LocationParam> thisTargetList, boolean targetOrder) {

        //8個掲載。足りない部分は非表示
        for (int i = 0; i < 8; i++) {

            int identifier = Common.myAppContext.getResources().getIdentifier("target_" + (i + 1), "id", getActivity().getPackageName());
            TextView textView = (TextView) getView().findViewById(identifier);

            if (thisTargetList.size() > i) {
                if (i > 0 && targetOrder) {
                    //順番指定時は????
                    textView.setText("????");
                } else {
                    textView.setText(thisTargetList.get(i).mono_name);
                }
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.INVISIBLE);
            }
        }
    }

    //----------------------- リスナー


    //エフェクトリスナー:エフェクト終わりを知る
    protected SagashiTimerListener eListener = null;

    public void setSagashiTimerListener(SagashiTimerListener l) {
        eListener = l;
    }

    public interface SagashiTimerListener extends EventListener {
        void monochromeOver();

        void roteteBackOver();

        void timeLimitAlert();

        void timeLimitAlertClear();

        void timeLimitOver();
    }

    //----------------------- タイマー

    /**
     * タイマーリセット
     */
    public void resetTimer(int limit) {
        penaltyTime = pauseTime = startDate = pauseDate = 0;
        limitTime = limit * 1000;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timerLabel.setText(Common.secondsToMinutes(limit));

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) meterImage.getLayoutParams();
        layoutParams.width = meterFrame.getWidth();
        meterImage.setLayoutParams(layoutParams);
    }

    public boolean isNearTimeLimit() {
        return timeLimitAlertFlag;
    }

    // モノクロ解除秒数
    public void setMonochromeTime(int time) {
        monchromeTime += time * 1000;
    }


    // 向き解除秒数
    public void setRotateBackTime(int time) {
        rotateBackTime += time * 1000;
    }


    /**
     * タイマースタート
     */
    public void startTimer() {

        //タイマー開始時間
        startDate = System.currentTimeMillis();

        buildTimer();
    }

    /**
     * タイマーボーナス
     */
    public void addTimer(int value) {
        if (value > 0) {
            limitTime += value * 1000;
        } else {
            penaltyTime += value * 1000;
        }
    }

    /**
     * タイマーポーズ
     */
    public void pauseTimer() {
        if (timer != null) {
            pauseDate = System.currentTimeMillis();
            timer.cancel();
            timer = null;
        } else {
            pauseDate = 0;
        }
    }

    /**
     * タイマーリスタート
     */
    public void restartTimer() {
        if (pauseDate > 0) {
            //ポーズ累積秒加算
            pauseTime += System.currentTimeMillis() - pauseDate;
            Common.logD("update PauseTime:" + pauseTime);
            //タイマー再開
            buildTimer();
        }
    }

    /**
     * タイマー生成
     */
    private void buildTimer() {

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        long keikaTime = System.currentTimeMillis() - startDate - pauseTime;

                        float nokoriTime = limitTime - keikaTime + penaltyTime;

                        int nokoriSeconds = (int) Math.ceil(nokoriTime / 1000.0f);

                        if (nokoriSeconds <= 0) nokoriSeconds = 0;

                        timerLabel.setText(Common.secondsToMinutes(nokoriSeconds));

                        float percent = nokoriTime / (float) limitTime;

                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) meterImage.getLayoutParams();

                        if (percent > 1) percent = 1;

                        layoutParams.width = (int) ((float) meterFrame.getWidth() * percent);

                        meterImage.setLayoutParams(layoutParams);

                        //モノクロ解除
                        if (monchromeTime > 0 && keikaTime > monchromeTime) {
                            monchromeTime = 0;
                            if (eListener != null) {
                                eListener.monochromeOver();
                            }
                        }

                        //向き戻り　
                        if (rotateBackTime > 0 && keikaTime > rotateBackTime) {
                            rotateBackTime = 0;
                            if (eListener != null) {
                                eListener.roteteBackOver();
                            }
                        }

                        //時間切まぢか
                        if (!timeLimitAlertFlag && nokoriTime <= 10 * 1000) {
                            timeLimitAlertFlag = true;
                            if (eListener != null) {
                                eListener.timeLimitAlert();
                            }
                        }
                        //時間切まぢか改善
                        else if (timeLimitAlertFlag && nokoriTime > 10 * 1000) {
                            timeLimitAlertFlag = false;
                            if (eListener != null) {
                                eListener.timeLimitAlertClear();
                            }
                        }

                        //時間ぎれ
                        if (nokoriTime <= 0) {
                            pauseTimer();
                            if (eListener != null) {
                                eListener.timeLimitOver();
                            }
                        }
                    }
                });
            }
        }, 250, 250);
    }

}
