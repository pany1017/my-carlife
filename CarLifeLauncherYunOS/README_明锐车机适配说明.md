# 明锐 CarLife 启动器（YunOS 3.0.1 / ARMv7 车机专用版）

## 一、为什么这版能跑在你的车机上

| 车机实际情况 | 本 APK 对策 |
|---|---|
| CPU: ARMv7 32bit @ 1001MHz | 仅编 `armeabi-v7a`，**不含 arm64-v8a**，不会 `INSTALL_FAILED_NO_MATCHING_ABIS` |
| 系统: YunOS 3.0.1-R (基于 Android 8.1 / API 27) | `minSdk 24` / `targetSdk 27`，**不使用 AndroidX AppCompat / Material3**（YunOS 的 support 库版本老旧，引用新库必 `ClassNotFoundException`） |
| 内存: 2GB，可用 1.15GB | 零第三方依赖，R8 全量混淆 + 资源裁剪，**APK < 1MB**，避免 `PackageManager` 解析超时 |
| 屏幕: 9 英寸横屏，约 1280×720 | 强制横屏 `landscape`，大热区按钮（140dp 高），深色高对比 |
| 未 root、未解锁 | 仅用公开 API：`PackageManager.getLaunchIntentForPackage` + `AudioManager` + `Settings` Intent，**不篡改、不反编译 `com.baidu.carlife`** |

## 二、相对上一版的 6 处关键修改

1. **删掉 AppCompatActivity** → 改用原生 `android.app.Activity` + `@android:style/Theme.NoTitleBar.Fullscreen`
2. **删掉 Material3 主题** → 改用 ShapeDrawable 自绘按钮（btn_primary.xml / btn_secondary.xml）
3. **删掉 ConstraintLayout** → 改用 LinearLayout + ScrollView（YunOS 自带 ConstraintLayout 版本过旧）
4. **CarLife 入口兼容** → 依次尝试 4 个常见 Launcher Activity，全部失败则用 `getLaunchIntentForPackage` 兜底（不同车机版 CarLife 入口类名不同）
5. **权限收敛** → 只保留 `MODIFY_AUDIO_SETTINGS`（普通权限），去掉 `USB_PERMISSION`/`INTERNET`/`WRITE_EXTERNAL_STORAGE`（YunOS 8.1 上会弹二次确认，且车机用不到）
6. **降级 AGP + Gradle** → AGP 3.1.4 + Gradle 4.10.3 + build-tools 27.0.3（Windows 上最稳组合，不会撞 `Unsupported class file major version`）

## 三、编译

### 环境（一次性）
- JDK 8（u202+，[Adoptium 下载](https://github.com/adoptium/temurin8-binaries/releases)）
- Android SDK：`sdkmanager "platforms;android-27" "build-tools;27.0.3"`

### 步骤
```
1. 解压本文件夹到 D:\carlife\  (路径不要有中文和空格)
2. 双击 build.bat
3. 首次运行会下载 Gradle 4.10.3 (约 80MB)，稍等
4. 成功后得到: 明锐CarLife启动器_v1.0.1.apk
```

## 四、车机安装

1. U 盘格式化为 **FAT32**（车机不认 exFAT/NTFS）
2. 先装 **百度 CarLife 车机版 APK**（本启动器不含 CarLife 代码）
3. 再装 **明锐CarLife启动器_v1.0.1.apk**
4. 车机需开启：`设置 → 关于 → 连点版本号 7 次 → 开发者选项 → USB 调试` + `允许未知来源`
5. **设为默认桌面**：按 Home 键 → 选"明锐CarLife启动器" → 始终（通电即启动）

## 五、YunOS 3.0.1 特有坑点

| 现象 | 原因 | 解决 |
|---|---|---|
| 装了但图标不显示 | YunOS 桌面不读 LAUNCHER 标签 | 进"应用管理"手动打开一次，或设为默认桌面 |
| 打开闪退 | 装了 arm64 版本 | 确认 APK 只含 armeabi-v7a（本版已处理） |
| "检测失败" | CarLife 未装或被停用 | 先装 CarLife 车机版，点"重新检测" |
| 无声音 / 无法语音 | USB 模式为"仅充电" | 开发者选项 → 选 USB 配置 → MTP |
| 无法安装 | 安装白名单 | 工程模式（长按 MENU 或设置连点）关闭 App Installation Restriction |

## 六、合法性

✅ 不反编译、不重打包、不篡改 `com.baidu.carlife`
✅ 仅用 Android 公开 API（Intent / AudioManager / Settings）
✅ 不 root、不刷写、不改系统分区
✅ 与 CarLife 完全解耦，可独立卸载
