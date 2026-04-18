package com.enzo.decyanyuscript;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.enzo.decyanyuscript.databinding.ActivityMainBinding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("decyanyuscript");
    }

    private static final String TAG = "XorEncryptApp";
    private ActivityMainBinding binding;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private interface NativeAction {
        void execute(String input, String output);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showToast("权限已授予，请再次点击执行");
                } else {
                    showToast("未获得存储权限，无法操作文件");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initListeners();
    }

    private void initListeners() {
        binding.btnAction1.setOnClickListener(v ->
                checkPermissionAndRun(() -> performTask(new String[]{"lua"}, this::callDecScript, "解密脚本"))
        );

        binding.btnAction2.setOnClickListener(v ->
                checkPermissionAndRun(() -> performTask(new String[]{"lua"}, this::callEncScript, "加密脚本"))
        );
    }

    /**
     * 权限检查核心逻辑
     */
    private void checkPermissionAndRun(Runnable task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                task.run();
            } else {
                showToast("请开启“所有文件访问权限”以继续");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                task.run();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        } else {
            task.run();
        }
    }

    /**
     * 统一的任务执行入口
     */
    private void performTask(String[] extensions, NativeAction action, String taskName) {
        String inputPath = binding.editInputPath.getText().toString().trim();
        String outputPath = binding.editOutputPath.getText().toString().trim();

        if (inputPath.isEmpty() || outputPath.isEmpty()) {
            showToast("请输入输入路径和输出路径");
            return;
        }

        setButtonsEnabled(false);
        showToast("开始执行: " + taskName + "...");

        executor.execute(() -> {
            try {
                if (!FileUtil.exists(inputPath)) {
                    mainHandler.post(() -> {
                        showToast("路径不存在: " + inputPath);
                        setButtonsEnabled(true);
                    });
                    return;
                }

                int count = 0;
                if (FileUtil.isDirectory(inputPath)) {
                    for (String ext : extensions) {
                        count += processDirectory(inputPath, ext, action);
                    }
                } else if (FileUtil.isFile(inputPath)) {
                    action.execute(inputPath, outputPath);
                    count = 1;
                }

                final int finalCount = count;
                mainHandler.post(() -> showToast(taskName + " 完成，共处理: " + finalCount + " 个文件"));

            } catch (Exception e) {
                Log.e(TAG, "Error processing files", e);
                mainHandler.post(() -> showToast("发生错误: " + e.getMessage()));
            } finally {
                mainHandler.post(() -> setButtonsEnabled(true));
            }
        });
    }

    private int processDirectory(String dirPath, String extension, NativeAction action) {
        List<String> foundFiles = FileUtil.findFilesWithExtension(dirPath, extension);
        if (foundFiles == null || foundFiles.isEmpty()) {
            return 0;
        }

        for (String filePath : foundFiles) {
            action.execute(filePath, filePath);
        }
        return foundFiles.size();
    }

    private void setButtonsEnabled(boolean enabled) {
        binding.btnAction1.setEnabled(enabled);
        binding.btnAction2.setEnabled(enabled);
    }

    private void showToast(String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            mainHandler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
        }
    }

    private void callDecScript(String in, String out) { DecScript(in, out); }
    private void callEncScript(String in, String out) { EncScript(in, out); }

    public static native void DecScript(String InputPath, String OutputPath);
    public static native void EncScript(String InputPath, String OutputPath);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}