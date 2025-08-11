# Consumer proguard rules for events module
# These rules will be applied to any module that depends on this module

# Keep public API
-keep public class com.eyedock.core.events.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
