package com.idlike.ttssupport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.idlike.ttssupport.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'ttssupport' library on application startup.

    static String model_name = "";
    private ActivityMainBinding binding;
    int port = 8088;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.test_spd) {
            Toast.makeText(MainActivity.this, "正在测试,请耐心等待(如果闪退了证明这个模型不可用)", Toast.LENGTH_SHORT).show();
            nativeTtsAPI ttsAPI = new nativeTtsAPI();
            ttsAPI.init(MainActivity.this,model_name);
            ttsAPI.TTS("我想说点掏心窝子话!\n",1.0f);
            float s = (float) (ttsAPI.GetNanoTimeUse()/ 1_000_000_000.0);
            binding.sampleText.setText("速度指数 (越大代表性能越强): "+(10-s)+"" +
                    "\n 需要重启应用来重启HTTP服务!!");
            Intent serviceIntent = new Intent(MainActivity.this, HttpSupport.class);
            stopService(serviceIntent); // 停止服务
            Toast.makeText(MainActivity.this, "请重启应用", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.model_manager) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, ModelMgrActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.set_port) {
            Uri uri = FileProvider.getUriForFile(MainActivity.this,getPackageName()+".provider",new File(getFilesDir(), "config.ini"));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            startActivity(intent);
            Toast.makeText(MainActivity.this, "需要重启APP来重启服务.", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!new File(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/config.ini").isFile()){
            IniLib.SetThing(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/config.ini","setting","port","8088");
        }
        String p = IniLib.GetThing(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/config.ini","setting","port");
        port = Integer.parseInt(p);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView tv = binding.sampleText;

        if(!new File(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/single_speaker_fast.bin").isFile()) {
            copyAssetToInternalStorage(this, "single_speaker_fast.bin", "single_speaker_fast.bin");
        }

        if(new File(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model").isFile()){
            try {
                FileInputStream fp = new FileInputStream(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model");
                byte[] b = new byte[(int) new File(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model").length()];
                fp.read(b);
                model_name = new String(b,StandardCharsets.UTF_8);
                fp.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                new File(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model").createNewFile();
                FileOutputStream fp = new FileOutputStream(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model");
                fp.write("single_speaker_fast.bin".getBytes(StandardCharsets.UTF_8));
                fp.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Toast.makeText(MainActivity.this, "需要重启APP来完成模型配置.", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(!new File(this.getApplicationContext().getFilesDir().getAbsolutePath()+"/single_speaker_fast.bin").isFile()) {
            Toast.makeText(MainActivity.this, "[FAILED]: 选择的模型不能打开!将恢复默认模型!", Toast.LENGTH_SHORT).show();
            finish();
        }

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!tv.getText().toString().contains("!!")){
                    tv.setText("HTTP TTS IS RUNNING IN PORT :" +port+
                            "\n [守护进程已创建 点击刷新状态]" +
                            "\n model = "+model_name);
                }
            }
        });
        tv.setText("HTTP TTS IS RUNNING IN PORT :"+port +
                "\n [守护进程已创建 点击刷新状态]" +
                "\n model = "+model_name);

        new Thread(() -> {
            Intent intent = new Intent(this, HttpSupport.class);
            intent.putExtra("PORT", port); // 设置你想要的端口号
            startService(intent);
            //启动后进入后台\
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            moveTaskToBack(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "HTTP TTS API已在后台运行", Toast.LENGTH_SHORT).show();
                }
            });


        }).start();
        Toast.makeText(MainActivity.this, "TTS服务正在启动...", Toast.LENGTH_SHORT).show();

    }



    public static void copyAssetToInternalStorage(Context context, String assetName, String fileName)  {
        // 获取内部存储目录
        File dir = context.getFilesDir();
        File file = new File(dir, fileName);

        // 确保目录存在
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 读取assets目录下的文件
        try (java.io.InputStream in = context.getAssets().open(assetName);
             OutputStream out = Files.newOutputStream(file.toPath())) {
            // 创建一个缓冲区
            byte[] buffer = new byte[1024];
            int read;
            // 从输入流读取数据到缓冲区，然后写入到输出流
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            // 刷新输出流，确保所有数据都被写入
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 文件复制完成
        // 注意：这里不需要手动关闭输入流和输出流，因为使用了try-with-resources语句
    }
}