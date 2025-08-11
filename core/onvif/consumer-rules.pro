# Consumer proguard rules for onvif module
# These rules will be applied to any module that depends on this module

# Keep public API
-keep public class com.eyedock.core.onvif.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
