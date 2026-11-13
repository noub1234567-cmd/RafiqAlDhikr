-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep class com.rafiqaldhikr.app.data.backup.** { *; }
-dontwarn kotlinx.serialization.**
