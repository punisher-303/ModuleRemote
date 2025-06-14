# Keep classes for AndroidX Window API (used by Flutter plugins)
-keep class androidx.window.** { *; }
-dontwarn androidx.window.**

# Keep classes for OneSignal push notifications
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**

# Keep classes for the flutter_bluetooth_serial plugin from punisher-303
-keep class io.github.punisher303.flutter_bluetooth_serial.** { *; }
-dontwarn io.github.punisher303.flutter_bluetooth_serial.**
