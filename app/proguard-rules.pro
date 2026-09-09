# Enlisted Planner V2 Full
# Keep model field names stable for Gson JSON backup/restore compatibility.
-keepattributes Signature
-keep class com.afomsteam.enlistedplanner.data.** { *; }
-dontwarn sun.misc.**

# Preserve useful crash line numbers in release builds.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
