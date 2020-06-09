# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/n01019/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5
-dontusemixedcaseclassnames

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Deprecated

# com.gency.aid
-keep class com.gency.aid.GencyAID { public protected *; }
-keep class com.gency.aid.GencyAIDConst { public protected *; }
# com.gency.commons.dialog
-keep class com.gency.commons.dialog.GencyBaseAgreementDialog { public protected *; }
-keep class com.gency.commons.dialog.GencyGCMAgreementDialog { public protected *; }
# com.gency.commons.file
-keep class com.gency.commons.file.GencyFileUtil { public protected *; }
-keep class com.gency.commons.file.GencyDownloadHelper { public protected *; }
-keep public enum com.gency.commons.file.GencyDownloadHelper$** {
    **[] $VALUES;
    public *;
}
-keep class com.gency.commons.file.json.JSON { public protected *; }
# com.gency.commons.http
-keep class com.gency.commons.http.GencyRequestParams { public protected *; }
-keep class com.gency.commons.http.GencyThreadHttpClient { public protected *; }
-keep class com.gency.commons.http.GencyAsyncHttpClient { public protected *; }
-keep class com.gency.commons.http.GencyAsyncHttpResponseHandler { public protected *; }
-keep class com.gency.commons.http.GencyJsonHttpResponseHandler { public protected *; }
# com.gency.commons.log
-keep class com.gency.commons.log.GencyApplicationLog { public protected *; }
-keep class com.gency.commons.log.GencyApplicationLogDB { public protected *; }
-keep class com.gency.commons.log.GencyApplicationLogManager { public protected *; }
-keep class com.gency.commons.log.GencyDLog { public protected *; }
# com.gency.commons.misc
-keep class com.gency.commons.misc.GencyPackageUtil { public protected *; }
-keep class com.gency.commons.misc.GencyRunCheck { public protected *; }
# com.gency.commons.net
-keep class com.gency.commons.net.GencyNetworkUtil { public protected *; }
# com.gency.commons.time
-keep class com.gency.commons.net.GencyTimeUtil { public protected *; }
# com.gency.util
-keep class com.gency.util.Utils { public protected *; }
# com.gency.version
-keep class com.gency.version.GencyVersion { *; }
-keep class com.gency.BuildConfig { *; }
