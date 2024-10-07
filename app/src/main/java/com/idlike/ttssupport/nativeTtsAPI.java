package com.idlike.ttssupport;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Time;

public class nativeTtsAPI {
    static {
        System.loadLibrary("ttssupport");
    }
    public native int spawn_sound(String inputText, String modelPath, String outputSound, float v);
    public native int test_model(String modelPath);
    private String tmp_path = "";
    private String model_path = "";
    private boolean lock = true;
    private long s_tick = 0;
    private long e_tick = -1;
    public boolean init(Context context, String model){
        this.tmp_path = context.getApplicationContext().getFilesDir().getAbsolutePath();
        this.model_path = this.tmp_path+"/"+model;
        this.lock = false;
        this.s_tick = 0;
        this.e_tick = -1;
        return test_model(this.model_path) != -1;
    }
    public byte[] TTS(String text,float speed){
        //加锁,防止重复执行任务
        try {
            while (this.isLock()){
                wait(100);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.lock = true;
        this.s_tick = System.nanoTime();
        //writ tmp
        File tmp = new File(tmp_path + "/tmp.txt");
        try {
            FileOutputStream fp = new FileOutputStream(tmp);
            fp.write(text.getBytes(StandardCharsets.UTF_8));
            fp.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int ret = spawn_sound(tmp.getPath(),model_path,tmp_path + "/out",speed);
        this.lock = false;
        this.e_tick = System.nanoTime();
        if(ret!=1){
            return readFileData(tmp_path + "/out_10");
        }
        return readFileData(tmp_path+"/out");
    }
    public long GetNanoTimeUse(){
        if(this.isLock()){
            return -1;
        }
        return this.e_tick - this.s_tick;
    }

    public boolean isLock() {
        return lock;
    }

    private byte[] readFileData(String path) {
        try (FileInputStream fp = new FileInputStream(path)) {
            ByteArrayOutputStream ot = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int bytesRead;
            while ((bytesRead = fp.read(b)) != -1) {
                ot.write(b, 0, bytesRead);
            }
            return ot.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
