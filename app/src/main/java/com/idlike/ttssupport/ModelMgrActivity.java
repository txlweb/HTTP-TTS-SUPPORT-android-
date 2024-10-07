package com.idlike.ttssupport;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ModelMgrActivity extends AppCompatActivity {
    public static void deleteFileByIO(String filePath) {
        File file = new File(filePath);
        File[] list = file.listFiles();
        if (list != null) {
            for (File temp : list) {
                deleteFileByIO(temp.getAbsolutePath());
            }
        }
        file.delete();
    }
    private RecyclerView fileListRecyclerView;
    private FileAdapter fileAdapter;
    private List<FileModel> fileList = new ArrayList<>();
    private Button btn_del;
    private Button btn_ena;
    private Button btn_imp;
    public static String base_path;
    public static int select_id = 0;
    int REQUEST_CODE_GET_FILE = 114514;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modelmgr);

        fileListRecyclerView = findViewById(R.id.recyclerView2);
        fileListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 获取文件列表并填充数据模型

        base_path = this.getApplicationContext().getFilesDir().getAbsolutePath();
        File directory = new File(base_path); // 替换为实际目录路径
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String t_o = "UNKNOW";

                if(file.getName().contains(".")){
                    fileList.add(new FileModel(file.getName(), t_o, file.length()));
                }

            }
        }

        // 创建并设置适配器
        fileAdapter = new FileAdapter(fileList);
        fileListRecyclerView.setAdapter(fileAdapter);
        //按钮功能绑定
        btn_del = findViewById(R.id.button4); // 删除

        btn_ena = findViewById(R.id.button2); // 启用
        btn_imp = findViewById(R.id.button1); // 获取按钮的引用

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View rootView = findViewById(android.R.id.content);
                FileModel file = fileList.get(select_id);
                Snackbar.make(rootView, "真的要删除\""+file.getFileName()+"\"吗?(点击其他位置取消)", Snackbar.LENGTH_SHORT)
                        .setAction("确认删除", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recreate();
                                    }
                                }, 2000);
                                    if (new File(base_path + "/" + file.getFileName()).delete()) {
                                        Snackbar.make(rootView, "删除成功!", Snackbar.LENGTH_SHORT).setAction("重载列表", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                recreate();
                                            }
                                        }).show();
                                    } else {
                                        Snackbar.make(rootView, "删除失败!", Snackbar.LENGTH_SHORT).setAction("重载列表", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                recreate();
                                            }
                                        }).show();
                                    }
                            }
                        })
                        .show();

            }
        });

        btn_ena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileModel file = fileList.get(select_id);
                MainActivity.model_name = file.getFileName();
                try {
                    new File(ModelMgrActivity.this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model").delete();
                    new File(ModelMgrActivity.this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model").createNewFile();
                    FileOutputStream fp = new FileOutputStream(ModelMgrActivity.this.getApplicationContext().getFilesDir().getAbsolutePath()+"/model");
                    fp.write(file.getFileName().getBytes(StandardCharsets.UTF_8));
                    fp.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(ModelMgrActivity.this, "配置修改为: "+ file.getFileName()+" (重启应用生效)", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        btn_imp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_CODE_GET_FILE);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GET_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                assert fileUri != null;
                String fileName = getFileNameFromUri(fileUri);
                View rootView = findViewById(android.R.id.content);
                    try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
                        saveFileToPrivateDirectory(inputStream,base_path+"/"+fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(rootView, "导入成功!", Snackbar.LENGTH_SHORT).setAction("好", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {recreate();}
                    }).show();
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 2000);
    }

    private void saveFileToPrivateDirectory(InputStream inputStream, String save_file) {
        File file = new File(save_file);
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            // 文件保存成功，可以在这里进行后续操作，如更新UI等
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String path = uri.getPath();

        if (path != null) {
            // 尝试从路径中提取文件名
            int cut = path.lastIndexOf('/');
            if (cut != -1) {
                fileName = path.substring(cut + 1);
            } else {
                fileName = path;
            }
        } else {
            // 如果路径为空，可能是内容提供者，尝试查询数据库
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        fileName = fileName.replaceAll("primary:","");//有多的东西
        return fileName;
    }
}