package jp.co.cybird.android.conanseek.common;


import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 画面遷移フラグメントクラス
 */
public class BaseCell extends Fragment {


    //-- メモリ解放

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanView(getView());
    }

    public void cleanView(View view) {

        if (view instanceof ViewGroup) {
            view.setBackground(null);
        }
        if (view instanceof TextView) {
            view.setBackground(null);
        }
        if (view instanceof ImageButton) {
            ImageButton b = (ImageButton) view;
            b.setImageDrawable(null);
            b.setBackground(null);
        } else if (view instanceof ImageView) {
            ImageView v = (ImageView) view;
            v.setImageDrawable(null);
            v.setBackground(null);
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            g.setBackground(null);
            int g_size = g.getChildCount();
            for (int i = 0; i < g_size; i++) {
                cleanView(g.getChildAt(i));
            }
        }


        System.gc();
        System.runFinalization();
    }


}
