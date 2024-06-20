package com.example.newcamera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.lang.reflect.Method;

public class HomeActivity extends AppCompatActivity {
    public static int REQUEST_VIDEO_PICK = 500;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);
    }
    // 处理按钮点击事件
    public void hookCamera(View view) {
        // 启动视频选择器
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK) {
            // 获取用户选择的视频Uri
            Uri videoUri = data.getData();
            // 调用hook方法，替换系统相机的预览视频为用户选择的视频
            hookCameraLogic(videoUri);
        }
    }

    private void hookCameraLogic(Uri videoUri) {
        // 示例：显示一个Toast提示
        Toast.makeText(this, "Hooking Camera with selected video...", Toast.LENGTH_SHORT).show();

        // 调用CameraHook的hook方法，将视频Uri传递进去
        CameraHook.hookCameraPreview(videoUri);
    }
}


