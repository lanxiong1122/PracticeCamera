package com.example.inlinecamera;

// MyLib.java
public class MyLib {
    static {
        System.loadLibrary("mylib");
    }

    public native void startPreview();
    public native void cancelPreview();
}