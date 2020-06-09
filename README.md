# 名探偵コナン 仮想世界（バーチャルワールド）の名探偵

通称コナン物探し

開発は外注(SOULINGさん)

## 依存関係

### ローカルモジュール

|モジュール|概要|
|--|--|
|app|アプリの本体モジュール|
| CYCompliance | 親権者同意画面|
|cryptolib|暗号化|
|POPgate|旧暗号化|
|cy_cybirdid|UUID生成サポート(旧CybirdUtilityのサルベージ用)|
|cy_gencydid|UUID生成サポート|
|cy_gcm|Cybird共通基盤GCM。※2019年4月終了だが、クライアントの受信は引き続き可能|
|gcm_sub_toPUSH|Cybird共通基盤GCM用の便利クラス|
|gency|Cybird共通基盤|
|CybirdUtility_1.0.2|旧UUID生成※新規アプリでの利用は禁止|

- 課金はIAB(v3)です。

### 外部ライブラリ

|ライブラリ|概要|
|--|--|
|play-services-ads|恐らく広告ID取得用?無くても動作する模様なのでアップデートが必要になった際には削除で良いかも|
|play-services-gcm|GCM。2019年4月終了だが、クライアントの受信は引き続き可能|

## build variantについて

|build variant|サーバー向き先|用途|
|--|--|--|
|cybirdDebug|開発|開発中のデバッグ用。当たり判定表示のフラグなどが実行中に変更可能等、デバッグ用メニューあり|
|cybirdtRelease|本番|Playストアアップ用|

※Product Flavorの`sougling`は使用しないこと。

## リリース

### リリース前の確認事項

- 各種バージョン番号
  - デバッグ用になっていないか
  - 前回リリースより上げてあるか

### キーストア情報

外注開発であったため、キーストアを[別のブランチ](http://repo.sf.intra.cybird.co.jp/svn/SPMCO/branches/jp.co.cybird.android.app.conanseek01_Android_seek02/)に置いてあります。

- 1.[キーストア管理用のブランチ](http://repo.sf.intra.cybird.co.jp/svn/SPMCO/branches/jp.co.cybird.android.app.conanseek01_Android_seek02/)をチェックアウトする
- 2.`conanseek01.jks`を`app/下`に置く
- 3.`key.properties`をconanseekプロジェクトのルートに置く

### リリースビルド

- ビルド

	下記コマンドを実行すると`app/build/outputs/apk`フォルダにapkが生成されます。
	
	```
	$ ./gradlew assembleCybirdRelease
	```

- svnコミット
  - 署名情報ファイル(conanseek01.jks, key.properties)をコミットしないよう注意してください。
  
### タグ付け

リリース後は、svnにtagsを作成するのを忘れないでください。

タグの命名規則は以下の通りです。

`jp.co.cybird.android.app.conanseek01_Android_Version{$versionName}`

