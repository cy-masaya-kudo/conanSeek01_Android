package jp.co.cybird.android.conanseek.common;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

public class CustomHorizonalScroll extends HorizontalScrollView {

    public interface CustomHorizonalScrollListener {
        void onCustomHorizonalScrollToBottom(CustomHorizonalScroll scrollView);
        void onCustomHorizonalScrollFree(CustomHorizonalScroll scrollView);
        void onCustomHorizonalScrollToTop(CustomHorizonalScroll scrollView);
    }
    private CustomHorizonalScrollListener customHorizonalScrollListener;
    public void setOnCustomHorizonalScrollListener (CustomHorizonalScrollListener listener) {
        customHorizonalScrollListener = listener;
    }


    public CustomHorizonalScroll(Context context) {
        super(context);
    }

    public CustomHorizonalScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomHorizonalScroll(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        View content = getChildAt(0);
        if (customHorizonalScrollListener == null) return;
        if (content == null) return;
        if (l + this.getWidth() >= content.getWidth()) {
            customHorizonalScrollListener.onCustomHorizonalScrollToBottom(this);
        }
        else if (l <= 0) {
            customHorizonalScrollListener.onCustomHorizonalScrollToTop(this);
        }
        else {
            customHorizonalScrollListener.onCustomHorizonalScrollFree(this);
        }
    }
}