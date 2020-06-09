package jp.co.cybird.android.conanseek.activity.top;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cybird.android.conanseek.activity.shop.CoinPopup;
import jp.co.cybird.android.conanseek.activity.shop.HeartPopup;
import jp.co.cybird.android.conanseek.activity.shop.MeganePopup;
import jp.co.cybird.android.conanseek.common.BaseActivity;
import jp.co.cybird.android.conanseek.common.BaseFragment;
import jp.co.cybird.android.conanseek.common.WebViewPopup;
import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SaveManager;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.co.cybird.android.conanseek.manager.UserInfoManager;
import jp.souling.android.conanseek01.R;
import jp.souling.android.conanseek01.Settings;

public class HeaderMenuFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private TextView coinLabel;
    private TextView ticketLabel;
    private TextView meganeLabel;
    private TextView presentLabel;

    private ArrayList<ImageView> meganeImages;
    private ArrayList<ImageView> heartImages;

    private Timer meganeTimer;
    private Timer heartTimer;
    private Handler timerHandler = new android.os.Handler();


    @Override
    public void onResume() {
        super.onResume();
        updateTsuuka(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (meganeTimer != null)
            meganeTimer.cancel();
        meganeTimer = null;
        if (heartTimer != null)
            heartTimer.cancel();
        heartTimer = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view;

        // トップ画面と他でレイアウト分ける
        if (getParentFragment().getClass() == TopFragment.class) {
            view = inflater.inflate(R.layout.fragment_header_menu_top, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_header_menu_regular, container, false);
        }

        View v;

        // back to top
        if ((v = view.findViewById(R.id.header_status_top)) != null)
            v.setOnClickListener(this);

        // top
        if ((v = view.findViewById(R.id.btn_gift)) != null)
            v.setOnClickListener(this);
        if ((v = view.findViewById(R.id.btn_notice)) != null)
            v.setOnClickListener(this);
        if ((v = view.findViewById(R.id.btn_setting)) != null)
            v.setOnClickListener(this);

        // plus
        if ((v = view.findViewById(R.id.header_status_coin_button)) != null)
            v.setOnClickListener(this);
        if ((v = view.findViewById(R.id.header_status_heart_button)) != null)
            v.setOnClickListener(this);
        if ((v = view.findViewById(R.id.header_status_megane_button)) != null)
            v.setOnClickListener(this);

        //coin
        coinLabel = (TextView) view.findViewById(R.id.header_status_coin_text);

        //ticket
        ticketLabel = (TextView) view.findViewById(R.id.header_status_ticket_text);

        //heart
        heartImages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int identifier = Common.myAppContext.getResources().getIdentifier("header_status_heart_" + i, "id", getActivity().getPackageName());
            heartImages.add((ImageView) view.findViewById(identifier));
        }

        //megane
        meganeImages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int identifier = Common.myAppContext.getResources().getIdentifier("header_status_megane_" + i, "id", getActivity().getPackageName());
            meganeImages.add((ImageView) view.findViewById(identifier));
        }

        meganeLabel = (TextView) view.findViewById(R.id.header_status_megane_text);

        //present
        presentLabel = (TextView) view.findViewById(R.id.present_count);


        updateTsuuka(false);

        //デバッグボタン

        if (Settings.isDebug) {

            Button button = new Button(getContext());
            button.setText("DBG");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseFragment)((BaseActivity)getActivity()).getCurrentViewController()).showPopup(DebugPopup.newInstance());
                }
            });
            ((ViewGroup) view).addView(button);

            ViewGroup.LayoutParams param = button.getLayoutParams();
            param.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            button.setLayoutParams(param);

        }

        return view;
    }


    private int meganeSolidCount = -1;
    private boolean meganeImageUpdateFlag = false;
    private long userInfoUpdateTime;
    private ArrayList<Integer> meganeTimerList;

    public void updateTsuuka(Boolean makeTimer) {

        ticketLabel.setText(String.valueOf(UserInfoManager.ticketCount()));

        coinLabel.setText(String.valueOf(UserInfoManager.coinCount()));

        meganeImageUpdateFlag = true;
        meganeSolidCount = UserInfoManager.meganeSolidCount();

        int heartCount = UserInfoManager.heartCount();
        for (int i = 0; i < 10; i++) {
            heartImages.get(i).setSelected((i < heartCount));
        }


        //UserInfo更新秒
        String thenStr = SaveManager.stringValue(SaveManager.KEY.USER_INFO_TIME__string, "");
        userInfoUpdateTime = Long.parseLong(thenStr);

        meganeTimerList = UserInfoManager.responseParam().item.tsuuka.megane.timer;


        if (makeTimer) {
            if (meganeTimer == null) {
                meganeTimer = new Timer();
                meganeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateTsuukaMeganeTimer();
                            }
                        });
                    }
                }, 1000, 250);
            }

            if (heartTimer == null) {

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, 1);

                heartTimer = new Timer();
                heartTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Common.logD("update timer  heart");
                                updateTsuukaHeartTimer();
                            }
                        });
                    }
                }, cal.getTime(), 1000 * 60);
            }
        }
        meganeImageUpdateFlag = true;
        updateTsuukaMeganeTimer();


        //プレゼント数
        if (presentLabel != null) {
            int count = UserInfoManager.responseParam().item.present;
            if (count > 0) {
                presentLabel.setVisibility(View.VISIBLE);
                presentLabel.setText("" + count);
            } else {
                presentLabel.setVisibility(View.GONE);
            }
        }
    }

    private void updateTsuukaMeganeTimer() {

        int nextTime = -1;

        //タイマー加算
        if (meganeTimerList != null && meganeTimerList.size() > 0) {

            //現在秒
            long currentTime = new Date().getTime();
            //更新してからの経過時間
            long gap = (currentTime - userInfoUpdateTime) / 1000;


            while (true) {

                if (meganeTimerList.size() <= 0) break;

                int num = meganeTimerList.get(0);
                long cNextTime = num - gap;

                if (cNextTime <= 0) {
                    meganeTimerList.remove(0);
                    meganeImageUpdateFlag = true;
                    meganeSolidCount++;
                    nextTime = (int)cNextTime;
                } else {
                    if (nextTime < 0) nextTime = (int)cNextTime;
                    break;
                }
            }
        }


        if (meganeImageUpdateFlag) {
            for (int i = 0; i < 5; i++) {
                meganeImages.get(i).setSelected((i < meganeSolidCount));
            }
            meganeImageUpdateFlag = false;
        }

        if (meganeSolidCount == 5) {
            meganeLabel.setText("-");
            if (meganeTimer != null)
                meganeTimer.cancel();
            meganeTimer = null;
        } else if (meganeSolidCount > 5) {
            meganeLabel.setText("+" + (meganeSolidCount - 5));
            if (meganeTimer != null)
                meganeTimer.cancel();
            meganeTimer = null;
        } else {
            meganeLabel.setText(Common.secondsToMinutes(nextTime));
        }
    }

    private void updateTsuukaHeartTimer() {

        //heart
        int heartSolidCount = UserInfoManager.heartSolidCount();
        int heartCount = UserInfoManager.heartCount();
        if (heartSolidCount != heartCount) {
            for (int i = 0; i < 10; i++) {
                heartImages.get(i).setSelected((i < heartCount));
            }
        }
    }

    @Override
    public void onClick(View v) {

        SeManager.play(SeManager.SeName.PUSH_BUTTON);

        switch (v.getId()) {

            //コイン
            case R.id.header_status_coin_button: {
                (((BaseActivity)getActivity()).getCurrentViewController()).showPopup(CoinPopup.newInstance());
                break;
            }

            //ハート
            case R.id.header_status_heart_button: {
                (((BaseActivity)getActivity()).getCurrentViewController()).showPopup(HeartPopup.newInstance());
                break;
            }

            //メガネ
            case R.id.header_status_megane_button: {
                (((BaseActivity)getActivity()).getCurrentViewController()).showPopup(MeganePopup.newInstance());
                break;
            }

            //プレゼント
            case R.id.btn_gift: {
                (((BaseActivity)getActivity()).getCurrentViewController()).showPopup(PresentBoxPopup.newInstance());
                break;
            }

            //お知らせ
            case R.id.btn_notice: {
                WebViewPopup webViewPopup = WebViewPopup.newInstance(
                        R.mipmap.title_oshirase,
                        getString(R.string.oshirase)
                );
                (((BaseActivity)getActivity()).getCurrentViewController()).showPopup(webViewPopup);
                break;
            }

            //設定
            case R.id.btn_setting: {
                (((BaseActivity)getActivity()).getCurrentViewController()).showPopup(ConfigPopup.newInstance());
                break;
            }

            //TOP戻る
            case R.id.header_status_top:
                ((BaseActivity) getActivity()).replaceViewController(TopFragment.newInstance());
                break;

        }
    }

}
