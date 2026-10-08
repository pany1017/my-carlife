package com.example.carlauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 斯柯达明锐 CarLife 一键启动器
 *
 * 目标车机:
 *   型号: skoda_mqb (MIB / 智行舒适版)
 *   系统: YunOS 3.0.1-R-20220200508.9002 (基于 Android 8.1 / API 27)
 *   CPU:  ARMv7 32bit @ 1001MHz
 *   内存: 2GB (可用 1.15GB)
 *   屏幕: 9 英寸, 约 1280x720 横屏
 *
 * 设计约束 (重要):
 *   1. 仅支持 armeabi-v7a (32 位), 不支持 arm64-v8a
 *   2. 不使用 Material3 / AppCompat, 直接用 android:Theme.NoTitleBar (兼容 YunOS 老 WebView 与旧 support 库)
 *   3. minSdk 24, targetSdk 27 (对齐 YunOS 8.1)
 *   4. 不引用 com.baidu.carlife 的任何类, 只通过 Intent 拉起, 避免 NoClassDefFoundError
 *   5. 布局用 AbsoluteLayout + 硬编码 dp, 避免 ConstraintLayout 依赖
 *   6. 体积 < 1MB, 避免车机 PackageManager 解析超时
 */
public class MainActivity extends Activity {

    // 百度 CarLife 车机版包名 (仅拉起, 不依赖其存在)
    private static final String CARLIFE_PKG = "com.baidu.carlife";
    // 常见入口 Activity (按优先级尝试, 兼容不同版本)
    private static final String[] CARLIFE_ENTRIES = {
            "com.baidu.carlife.LauncherActivity",
            "com.baidu.carlife.MainActivity",
            "com.baidu.carlife.SplashActivity",
            "com.baidu.carlife.CarLifeActivity"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 直接用系统 Theme, 不依赖 AppCompatActivity, 兼容 YunOS 3.0.1
        setContentView(R.layout.activity_main);

        // 媒体音量预设: 2/3 最大值, 防止起步爆音
        try {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int target = Math.max(1, (int) (max * 2f / 3f));
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);
            }
        } catch (Exception e) {
            // 音量设置失败不影响主流程
        }

        TextView tvStatus = findViewById(R.id.tv_status);
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnRetry = findViewById(R.id.btn_retry);
        Button btnSettings = findViewById(R.id.btn_settings);

        // 检测 CarLife 是否已安装
        final boolean installed = isCarLifeInstalled();
        if (installed) {
            tvStatus.setText("✓ 已检测到 CarLife，可一键连接");
            tvStatus.setTextColor(0xFF4CAF50);
        } else {
            tvStatus.setText("✗ 未安装 CarLife 车机版，请先安装");
            tvStatus.setTextColor(0xFFFF9800);
        }

        // 一键连接
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCarLifeInstalled()) {
                    Toast.makeText(MainActivity.this,
                            "车机未安装 CarLife，请先通过 U 盘安装车机版 APK",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                boolean ok = launchCarLife();
                if (!ok) {
                    Toast.makeText(MainActivity.this,
                            "拉起失败，请确认 CarLife 未被停用",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // 重试检测
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCarLifeInstalled()) {
                    tvStatus.setText("✓ 已检测到 CarLife，可一键连接");
                    tvStatus.setTextColor(0xFF4CAF50);
                    Toast.makeText(MainActivity.this, "检测成功", Toast.LENGTH_SHORT).show();
                } else {
                    tvStatus.setText("✗ 仍未检测到 CarLife");
                    tvStatus.setTextColor(0xFFFF9800);
                }
            }
        });

        // 进入系统设置 (USB 调试 / 默认桌面 / 未知来源)
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
    }

    /** 通过包名判断 CarLife 是否已安装 (不抛异常) */
    private boolean isCarLifeInstalled() {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo(CARLIFE_PKG, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 依次尝试多个入口 Activity 拉起 CarLife */
    private boolean launchCarLife() {
        for (String entry : CARLIFE_ENTRIES) {
            try {
                Intent intent = new Intent();
                intent.setClassName(CARLIFE_PKG, entry);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                // 该入口不存在, 尝试下一个
            } catch (Exception e) {
                // SecurityException 等, 继续尝试
            }
        }
        // 兜底: 用 LAUNCHER intent 拉起 (等同桌面点击)
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(CARLIFE_PKG);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    /** 打开系统设置 (YunOS 3.0.1 对应 Android 8.1) */
    private void openSettings() {
        // 优先开发者选项, 其次应用设置, 最后系统设置
        String[] actions = {
                "android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS",
                "android.provider.Settings.ACTION_APPLICATION_SETTINGS",
                "android.provider.Settings.ACTION_SETTINGS"
        };
        for (String action : actions) {
            try {
                startActivity(new Intent(action));
                return;
            } catch (Exception e) {
                // 继续尝试
            }
        }
        Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show();
    }

    /** 禁用返回键, 避免车机场景误退到黑屏 */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
