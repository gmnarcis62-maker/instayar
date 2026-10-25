# Room Proguard Rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi Proguard Rules
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json(name = *) <fields>;
}

# Farasource & Myket In-App Billing Proguard Rules
-keep class com.farasource.billing.** { *; }
-keep interface com.farasource.billing.** { *; }
-keep class com.android.vending.billing.** { *; }
-keep interface com.android.vending.billing.** { *; }
-keep class ir.mservices.market.** { *; }

# App Models & Entities Proguard Rules
-keep class red.line.instayar.data.model.** { *; }
-keep class red.line.instayar.data.local.** { *; }
-keep class red.line.instayar.billing.** { *; }
-keep class red.line.instayar.security.** { *; }

# Kotlin Coroutines and Reflection
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
