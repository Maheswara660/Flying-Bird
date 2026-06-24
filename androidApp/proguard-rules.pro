# Flying Bird ProGuard rules

# General Android & Kotlin Keep rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Preserve SQLDelight DB Schema and generated Database classes
-keep class com.maheswara660.flyingbird.db.** { *; }
-keep class com.squareup.sqldelight.** { *; }

# Preserve Koin Dependency Injection framework classes
-keep class io.insertkoin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# Preserve Jetpack Compose & Compose Multiplatform
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# Preserve Flying Bird Presentation & Business Logic
-keep class com.maheswara660.flyingbird.android.** { *; }
-keep class com.maheswara660.flyingbird.presentation.** { *; }
-keep class com.maheswara660.flyingbird.game.** { *; }
-keep class com.maheswara660.flyingbird.physics.** { *; }
-keep class com.maheswara660.flyingbird.audio.** { *; }
