# EON — Personal Future Simulator ProGuard / R8 rules

# Keep Room entities & DAOs (reflection-free but keep annotations)
-keep class com.eon.futuresimulator.data.database.entity.** { *; }

# Keep kotlinx.serialization models used for Export/Import
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.eon.futuresimulator.**$$serializer { *; }
-keepclassmembers class com.eon.futuresimulator.** {
    *** Companion;
}
-keepclasseswithmembers class com.eon.futuresimulator.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**

# WorkManager CoroutineWorker classes are reached via reflection
-keep class com.eon.futuresimulator.workers.** { *; }
