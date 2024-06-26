package com.example.inlinecamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {
// _ZN7android6Camera12startPreviewEv   64位相机的startPreview
   MyLib myLib = new MyLib();
    String exeFileName = "mylib";
    String exe_path;
    private SurfaceView mSurfaceView;
    private CameraHelper mCameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MySdk.init();

        // 执行命令
        // otherExe();
    }
    // 确定
    public void onGo(View view){
        myLib.startPreview();
    }
    // 取消
    public void onCancel(View view){
        myLib.cancelPreview();
    }
    public void onTest(View view){
        try {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else {
               // getCameraSupportedSize();
                // Create SurfaceView and add it to the layout
                if (mSurfaceView == null) mSurfaceView = new SurfaceView(this);
                mSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(
                        convertDpToPx(200), // Width in pixels
                        convertDpToPx(200)  // Height in pixels
                ));
                if (mSurfaceView.getParent() != null && mSurfaceView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) mSurfaceView.getParent()).removeView(mSurfaceView);
                }

                    // Find the layout where the SurfaceView will be added
                ViewGroup layout = findViewById(R.id.main);

                layout.addView(mSurfaceView);

                // Initialize CameraHelper with the SurfaceView
                mCameraHelper = new CameraHelper(this,mSurfaceView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

  private int convertDpToPx(int dp) {
            float density = getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }

    // 执行命令
    private void otherExe() {
        try {
            runLocalRootUserCommand("setenforce 0");
            remountSystem();
            initExecutableFile();
            //getCameraSupportedSize();
            exe();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    // 1
    private String runLocalRootUserCommand(String command) {
        String result = "";
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataInputStream inputStream = new DataInputStream(p.getInputStream());
            OutputStream outputStream = p.getOutputStream();

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(command + "\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            p.waitFor();

            byte[] buffer = new byte[1024];
            while (inputStream.read(buffer) > 0) {
                String s = new String(buffer);
                result = result + s;
            }
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Log.e("jieguo",result);
        return result;
    }

    // 2
    // remount /system to read and write
    public void remountSystem()
    {
        String s = runLocalRootUserCommand("mount");

        int a = s.indexOf("/system",0);
        while (true)
        {
            if(a<0)
            {
                Log.e("msg", "error: can not remount system!");
                return;
            }
            if(a+7>s.length())
            {
                Log.e("msg", "error: can not remount system!");
                return;
            }
            if(s.charAt(a-1) ==' ' && s.charAt(a+7) ==' ')
            {
                break;
            }
            a = s.indexOf("/system",a+7);
        }

        s = s.substring(0,a);
        String[] temp = s.split("\n");
        if(temp.length == 0)
        {
            Log.e("msg", "error: can not remount system!");
            return;
        }
        s = temp[temp.length-1];

        String remountString = "mount -o remount "+s+" /system";
        Log.i("msg", remountString);
        runLocalRootUserCommand(remountString);
    }
    // 3
    public void initExecutableFile() {
        exe_path = "data/data/" + getPackageName() + "/" + exeFileName;
        //so_path = "data/data/" + getPackageName() + "/" + soFileName;
        //imageInfo_path = "data/data/" + getPackageName() + "/" + imageInfoName;
        try {
            copyDataToExePath(exeFileName, exe_path);
            //copyDataToExePath(soFileName, so_path);
            /*String  result = runLocalRootUserCommand("ls /system/lib/libhook.so");
            if(result.length()<10) {
                String copySoToSystem = "cat " + so_path + " > " + "/system/lib/libhook.so \n" +
                        "chmod 777 /system/lib/libhook.so \n";
                runLocalRootUserCommand(copySoToSystem);
            }*/
            runLocalRootUserCommand("chmod 777 /system/lib/mylib.so");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void copyDataToExePath(String srcFileName, String strOutFileName) throws IOException {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = getAssets().open(srcFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }
    // 4
    public void getCameraSupportedSize()
    {
        Camera mCamera = Camera.open();

        Camera.Parameters parameters=mCamera.getParameters();

        Log.e("getCameraSupportedSize:",""+parameters.getSupportedPreviewSizes()) ;
        mCamera.startPreview();
    }
    // 5
    public String exe() {
        String s = runLocalRootUserCommand("ps mediaserver");
        Log.i("msg", s);

        String[] items = s.split("media");
        String item = items[0].trim();
        String pid = item.split(" ")[0];
        Log.i("msg", pid);

        s = runLocalRootUserCommand("ps " + exeFileName);
        if (!s.contains(exeFileName)) {
            return runExecutable(pid);
        }
        return null;
    }
    public String runExecutable(String args) {
        String exeCmd = "chmod 777 " + exe_path + "\n" +
                exe_path + " " + args + "\n";

        return runLocalRootUserCommand(exeCmd);
    }

}


class CameraHelper implements SurfaceHolder.Callback {

    private static final String TAG = "CameraHelper";

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private int currentRotation;
    private boolean isFrontFacingCamera;

    public CameraHelper(Context context, SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        currentRotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        openCameraAndSetPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged - format: " + format + ", width: " + width + ", height: " + height);
        // Adjust camera parameters if needed based on the new dimensions
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        releaseCamera();
    }

    private void openCameraAndSetPreview(SurfaceHolder holder) {
        Log.d(TAG, "openCameraAndSetPreview");

        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (mCamera == null) {
            Log.e(TAG, "Failed to open camera.");
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        isFrontFacingCamera = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;

        int rotation = calculateRotation(currentRotation, isFrontFacingCamera);
        Log.d(TAG, "Rotation calculated: " + rotation);

        parameters.setRotation(rotation);
        Log.d(TAG, "Setting camera parameters: " + parameters.flatten());

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = determineBestPreviewSize(parameters, mSurfaceView.getWidth(), mSurfaceView.getHeight());
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            Log.d(TAG, "Setting preview size: " + previewSize.width + "x" + previewSize.height);
        }

        try {
            mCamera.setParameters(parameters);
            setCameraDisplayOrientation(mCamera); // 设置显示方向
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            Log.d(TAG, "Camera preview started.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to start camera preview.", e);
            releaseCamera();
        }
    }

    private void releaseCamera() {
        Log.d(TAG, "releaseCamera");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "Camera released.");
        }
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        releaseCamera();
    }

    private int calculateRotation(int displayRotation, boolean frontFacing) {
        if (frontFacing) {
            switch (displayRotation) {
                case Surface.ROTATION_0:
                    return 270;
                case Surface.ROTATION_90:
                    return 180;
                case Surface.ROTATION_180:
                    return 90;
                case Surface.ROTATION_270:
                    return 0;
            }
        } else {
            switch (displayRotation) {
                case Surface.ROTATION_0:
                    return 90;
                case Surface.ROTATION_90:
                    return 0;
                case Surface.ROTATION_180:
                    return 270;
                case Surface.ROTATION_270:
                    return 180;
            }
        }
        return 0;
    }

    private void setCameraDisplayOrientation(Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int rotation = ((WindowManager) mSurfaceView.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private Camera.Size determineBestPreviewSize(Camera.Parameters parameters, int surfaceWidth, int surfaceHeight) {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        if (sizes == null) return null;

        Camera.Size bestSize = null;
        int bestScore = Integer.MAX_VALUE;

        for (Camera.Size size : sizes) {
            int score = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
            if (score < bestScore) {
                bestScore = score;
                bestSize = size;
            }
        }
        return bestSize;
    }
}

