package com.example.inlinecamera;

import android.hardware.Camera;
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

public class MainActivity extends AppCompatActivity {
// _ZN7android6Camera12startPreviewEv   64位相机的startPreview
   MyLib myLib = new MyLib();
    String exeFileName = "mylib";
    String exe_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MySdk.init();

        // 执行命令
        otherExe();
    }
    // 确定
    public void onGo(View view){
        myLib.startPreview();
    }
    // 取消
    public void onCancel(View view){
        myLib.cancelPreview();
    }

    // 执行命令
    private void otherExe() {
        runLocalRootUserCommand("setenforce 0");
        remountSystem();
        initExecutableFile();
        getCameraSupportedSize();
        exe();
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