package com.idlike.ttssupport;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;


import android.os.Build;

import com.idlike.ttssupport.EncodingDetect;


public class IniLib {
    public static boolean lastLineisCRLF(String filename) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(filename, "r");
            long pos = raf.length() - 2;
            if (pos < 0) return false; // too short
            raf.seek(pos);
            return raf.read() == '\r' && raf.read() == '\n';
        } catch (IOException e) {
            return false;
        } finally {
            if (raf != null) try {
                raf.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static String GetThing(String FileName, String Node, String key) {//will return key
        //校验文件存在
        if(!new File(FileName).isFile()) return "UnknownThing";
        //如果文件尾部没有换行符,就要添加,否则会报错!!!!
        try {
            if (!lastLineisCRLF(FileName))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.write(Paths.get(FileName), "\r\n".getBytes(), StandardOpenOption.APPEND);
                }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> lines = ReadCFGFile(FileName);
        boolean getLN = false;
        for (String line : lines) {
            if (line.contains("[" + Node + "]")) {
                getLN = true;
                continue;
            }
            if (getLN & (line.contains(key + "=") || line.contains(key + " ="))) {
                String[] a = line.split("=");
                return a[1];
            }
            if (line.contains("[") & line.contains("]")) {
                getLN = false;
            }
        }
        return "UnknownThing";
    }

    public static void SetThing(String FileName, String Node, String key, String Value) {//will return key
        if (!new File(FileName).isFile()) WriteFileToThis(FileName, "[" + Node + "]");
        List<String> lines = ReadCFGFile(FileName);
        boolean getLN = false;
        boolean changed = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("[" + Node + "]")) {
                getLN = true;
                continue;
            }
            if (getLN & (line.contains(key + "=") || line.contains(key + " ="))) {
                lines.set(i, key + "=" + Value + "\r\n");//存在就覆写上去
                changed = true;
                break;
            }
            if (line.contains("[") & line.contains("]")) {
                if (getLN) {
                    lines.add(i - 1, key + "=" + Value + "\r\n");//如果找到node却没有key则在下一个node前插入k+v
                    changed = true;
                    break;
                }
                getLN = false;
            }
        }
        if (!changed) {//这种情况就是没有node或只有一个node
            if (getLN) {//直接写
                lines.add(key + "=" + Value + "\r\n");
            } else {//没node就创建
                lines.add("[" + Node + "]" + "\r\n");
                lines.add(key + "=" + Value + "\r\n");
            }
        }
        //写回文件
        StringBuilder ln = new StringBuilder();
        for (String line : lines) {
            if (line.contains("=") || line.contains("[") || line.contains("#")) ln.append(line).append("\r\n");
        }
        if (new File(FileName).isFile()) new File(FileName).delete();
        WriteFileToThis(FileName, String.valueOf(ln));
    }
    public static void WriteFileToThis(String file_name, String data) {
        try {
            File file = new File(file_name);
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file_name, true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(data);
            bufferWriter.close();
            //System.out.println("Done( "+data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void allClose(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null) closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> ReadCFGFile(String strFilePath) {
        File file = new File(strFilePath);
        List<String> rstr = new ArrayList<>();
        if (!file.exists() || file.isDirectory()) {
            System.out.println((char) 27 + "[31m[E]: 找不到文件"+strFilePath+"." + (char) 27 + "[39;49m");
        } else {
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                String encode = "UTF-8";
                //限制encode范围,只能给高频的用
                if(new File(strFilePath+".encode").isFile()){
                    try (BufferedReader reader = new BufferedReader(new FileReader(strFilePath+".encode"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            encode = line;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    if(new File(strFilePath).length()>0){
                        encode = EncodingDetect.getJavaEncode(strFilePath);
                    }
                    //System.out.println("FileEncode = "+encode);
                    WriteFileToThis(strFilePath+".encode",encode);
                }
                fileInputStream = new FileInputStream(file);
                inputStreamReader = new InputStreamReader(fileInputStream, encode);
                bufferedReader = new BufferedReader(inputStreamReader);
                String str;
                while ((str = bufferedReader.readLine()) != null) {
                    rstr.add(str);
                }
                return rstr;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                allClose(bufferedReader, inputStreamReader, fileInputStream);
            }
        }
        return rstr;
    }
}
