# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.

# Keep accessibility service
-keep class com.fred.assistente99.CorridaAccessibilityService { *; }
-keep class com.fred.assistente99.ConfigManager { *; }
-keep class com.fred.assistente99.FloatingOverlayService { *; }
-keep class com.fred.assistente99.MainActivity { *; }
