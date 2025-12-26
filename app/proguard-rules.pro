-dontwarn kotlin.**
-dontwarn kotlinx.**

-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

-keep class androidx.navigation.compose.** { *; }

-dontwarn coil.**

-keep class androidx.navigation.NavBackStackEntry { *; }
-keep class coil.decode.** { *; }
-dontwarn some.package.**

-keep class com.google.android.filament.** { *; }
-dontwarn com.google.android.filament.**

-dontwarn com.google.ar.sceneform.**

-keep class com.google.flatbuffers.** { *; }
