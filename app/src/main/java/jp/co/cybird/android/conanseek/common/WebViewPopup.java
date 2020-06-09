package jp.co.cybird.android.conanseek.common;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import jp.co.cybird.android.conanseek.manager.Common;
import jp.co.cybird.android.conanseek.manager.SeManager;
import jp.souling.android.conanseek01.R;

/**
 * ウェブビュー
 */
public class WebViewPopup extends BasePopup {

    private WebView webView;

    private boolean oshiraseFlag;

    private int titleImageResourceId;
    private String urlString;


    public static WebViewPopup newInstance(int titleImageResourceId, String urlString) {

        Bundle args = new Bundle();
        args.putInt("resource", titleImageResourceId);
        args.putString("url", urlString);

        WebViewPopup fragment = new WebViewPopup();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;

        Bundle args = getArguments();

        if (args != null) {
            titleImageResourceId = args.getInt("resource");
            urlString = args.getString("url");
        }

        if (titleImageResourceId == R.mipmap.title_oshirase) {
            //お知らせはウェブビュー大きめ表示
            view = inflater.inflate(R.layout.popup_webview_full, container, false);
            oshiraseFlag = true;

        } else {
            view = inflater.inflate(R.layout.popup_webview, container, false);
        }


        webView = (WebView) view.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        webView.loadUrl(urlString);

        webView.setBackgroundColor(0);




        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (oshiraseFlag) {
                    if (url.indexOf("new_oshirase") > 0) {
                        SeManager.play(SeManager.SeName.OSHIRASE);
                        return true;
                    }
                    Uri uri = Uri.parse(url);
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

        });


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.dialog_close || v.getId() == R.id.btn_cancel) {

                    SeManager.play(SeManager.SeName.PUSH_BACK);

                    if (oshiraseFlag) {

                        //詳細見せている際は一覧戻り
                        if (webView.getUrl().toString().indexOf("#") > 0) {
                            webView.loadUrl("javascript:r=showList();history.back();");
                        }
                        //一覧時は普通に消す
                        else {
                            if (buttonListener != null)
                                buttonListener.pushedNegativeClick(WebViewPopup.this);
                            removeMe();
                        }
                    } else {
                        if (buttonListener != null)
                            buttonListener.pushedNegativeClick(WebViewPopup.this);
                        removeMe();
                    }
                }
            }
        };
        view.findViewById(R.id.dialog_close).setOnClickListener(listener);
        ImageView imageView = (ImageView) view.findViewById(R.id.dialog_title);


        imageView.setImageResource(titleImageResourceId);

        return view;
    }

    boolean onstartFlag = false;

    @Override
    public void onStart() {
        super.onStart();
        if (!onstartFlag) {

            if (urlString.length() > 0) {
                onstartFlag = true;
                if (oshiraseFlag) {
                    //String customHtml = "<html><head><style>html,body,iframe{border:none;margin:0;padding:0;}iframe{margin:0%;width:100%;height:100%;position:absolute;}</style></head><body><iframe src='"+urlString+"'></iframe></body></html>";
                    //webView.loadData(customHtml, "text/html", "UTF-8");
                    //webView.loadUrl(urlString);
                } else {
                    //webView.loadUrl(urlString);
                }
            }
        }
    }

}
