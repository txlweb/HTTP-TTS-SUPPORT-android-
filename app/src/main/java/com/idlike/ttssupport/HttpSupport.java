package com.idlike.ttssupport;
import static androidx.core.app.ServiceCompat.startForeground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;

public class HttpSupport extends Service {
    private int port = 0;
    private String keystone_path = "";
    private static ServerSocket serverSocket;
    private static nativeTtsAPI tts = new nativeTtsAPI();
    public void init(Context ct, int p){
        this.port = p;
        keystone_path = ct.getApplicationContext().getFilesDir().getAbsolutePath()+"/keystore.jks";
        tts.init(ct,MainActivity.model_name);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.port = intent.getIntExtra("PORT",8080);
        tts.init(this,MainActivity.model_name);
        new Thread(this::startServer).start();
        new Thread(()->{
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                tts.init(this,MainActivity.model_name);
            }
        });
        NotificationChannel channel = new NotificationChannel(
                "tts_http_service",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "tts_http_service")
                .setContentTitle("TTS服务正在后台运行...")
                .setContentText("现在你可以在浏览器或TextReader APP中使用本地TTS服务了!")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);
        return START_STICKY; // Attempt to restart the service if it's killed
    }
    public void stop(){
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopForeground(true);
        stopSelf();
    }
    private void startServer(){
        try {
//            KeyStore keyStore = KeyStore.getInstance("PKCS12");
//            keyStore.load(new FileInputStream("keystore.p12"), "password".toCharArray());
//
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
//            keyManagerFactory.init(keyStore, "password".toCharArray());
//
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
//            trustManagerFactory.init(keyStore);
//
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
//
//            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
//            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
           serverSocket = new ServerSocket(port);
            while (true) {
                if(serverSocket.isClosed()) break;
                Socket socket = serverSocket.accept();
                handleRequest(socket);
            }
            Toast.makeText(this, "[SERVER SOCKET IS CLOSED]", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }

    }
    private static void handleRequest(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String requestLine = reader.readLine();
            String[] parts = requestLine.split(" ");
            String path = parts[1];
            if (path.contains("/api")) {
                String[] a = URLDecoder.decode(path, String.valueOf(StandardCharsets.UTF_8)).split("\\?");
                String text = a[2];
                String spd = a[1];
                sendResponse(socket.getOutputStream(),200,"OK","audio/mpeg",tts.TTS(text, (float) Double.parseDouble(spd)));
                System.out.println(tts.GetNanoTimeUse()/ 1_000_000.0+" ms");
            }else {
                sendResponse(socket.getOutputStream(),200,"OK","text/html","<h1>TTS LOCAL SUPPORT API</h1><h2>example: /api?1.0?TEST_TEXT</h2>".getBytes());

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void sendResponse(OutputStream out, int statusCode, String statusText, String contentType, byte[] data)
            throws IOException {
        String statusLine = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n";
        String header = "Content-Type: " + contentType + "\r\n" + "Content-Length: " + data.length + "\r\n"
                + "Connection: close\r\n\r\n";
        out.write(statusLine.getBytes());
        out.write(header.getBytes());
        out.write(data);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
