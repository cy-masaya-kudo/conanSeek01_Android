package com.gency.gcm;

/**
 * <h3>AgreementDialogの制御を行う</h3>
 *
 * AgreementDialogが戻るボタン押下などで非表示になった場合にカスタマイズ処理を行う場合に継承して使用する
 */
public interface GencyDismissHooker {
    void handleDismiss();  
}