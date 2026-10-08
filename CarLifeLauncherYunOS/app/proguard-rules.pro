# 保留 CarLife 包名字符串 (在代码中通过反射式 Intent 使用, 会被 R8 误判为无用)
-keepclassmembers class com.example.carlauncher.MainActivity {
    <fields>;
}
-keepnames class com.example.carlauncher.MainActivity

# 保留所有 View 的 onClick 引用 (本工程用代码 setOnClickListener, 此项保险)
-keepclassmembers class * extends android.view.View {
    void set*(...);
}
