# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/l2/Android/Sdk/tools/proguard/proguard-android.txt
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


-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-useuniqueclassmembernames
-dontwarn
-dontnote

# ButterKnife 7
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Dagger
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class dagger.* { *; }
-keep class dagger.internal.* { *; }
-keep class javax.inject.* { *; }
-keep class * extends dagger.internal.Binding { *; }
-keep class * extends dagger.internal.ModuleAdapter { *; }
-keep class * extends dagger.internal.StaticInjection { *; }

-keep class com.asdf123.as3f.di.** { *; }
-keep class com.asdf123.as3f.log.** { *; }
-keep class com.asdf123.as3f.resource.** { *; }
-keep class com.asdf123.as3f.service.** { *; }
-keep class com.asdf123.as3f.ui.activity.** { *; }
-keep class com.asdf123.as3f.utils.** { *; }

# contrib
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }