/**
 * Copyright (c) 2015年 CYBIRD Co., Ltd.
 */

package jp.co.cybird.android.minors;

import android.webkit.WebView;

/**
 * サブクラスへのイベント通知用
 */
interface OnWebViewClientEventLinstener {
    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon);
    public void onPageFinished(WebView view, String url);
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl);
}
