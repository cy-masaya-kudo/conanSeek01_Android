# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/n01008/Developer/adt-bundle-mac-x86_64-20140624/sdk/tools/proguard/proguard-android.txt
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

# com.gency.gcm
-keep class com.gency.gcm.GencyAgreementDialog { public protected *; }
-keep class com.gency.gcm.GencyCustomDialogActivity { public protected *; }
-keep class com.gency.gcm.GencyDismissHooker { public protected *; }
-keep class com.gency.gcm.GencyGCMConst { *; }
-keep class com.gency.gcm.GencyGCMIntermediateActivity { public protected *; }
-keep class com.gency.gcm.GencyGcmListenerService { public protected *; }
-keep class com.gency.gcm.GencyGCMTokenRegister { public protected *; }
-keep class com.gency.gcm.GencyGCMUnityProxyActivity { public protected *; }
-keep class com.gency.gcm.GencyGCMUtilitiesE { public protected *; }
-keep class com.gency.gcm.GencyInstanceIDListenerService { public protected *; }
-keep class com.gency.gcm.GencyPrefsActivity { public protected *; }
-keep class com.gency.gcm.GencyRegistrationIntentService { public protected *; }

# com.gency.version
-keep class com.gency.version.GencyGCMVersion { public protected *; }
