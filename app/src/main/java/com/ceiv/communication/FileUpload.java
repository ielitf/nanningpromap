package com.ceiv.communication;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Created by zhangdawei on 2018/8/10.
 */

public class FileUpload {

    private final static String TAG = "FileUpload";

    //上传的状态
    public final static int UPLOAD_RUNNING = 0xfe;
    public final static int UPLOAD_FINISHED = 0xff;
    //上传的结果
    public final static int SUCCESS = 0x00;
    public final static int INVALID_PARAM = 0x01;
    public final static int FILE_NOT_EXISTS = 0x02;
    public final static int FAILED = 0x03;


    private FileUploadCallBack callback;

    public FileUpload(FileUploadCallBack callback) {
        this.callback = callback;
    }


    public interface FileUploadCallBack {

        public void upload(int status, int result);

    }

    public void upload(final String targetUrl, final String remoteFile, final String localPath, final String localFile) {

        Log.d(TAG, "Upload file:" + localPath + "/" + localFile + " to " + targetUrl + "/" + remoteFile);

//        if (TextUtils.isEmpty(targetUrl) || TextUtils.isEmpty(filePath)) {
//            Log.e(TAG, "invalid parameters!");
//            callback.upload(UPLOAD_FINISHED, INVALID_PARAM);
//            return;
//        }
//
//        final File file = new File(filePath);
//        if (!file.exists() || !file.isFile()) {
//            Log.e(TAG, "file does't exists!");
//            callback.upload(UPLOAD_FINISHED, FILE_NOT_EXISTS);
//            return;
//        }



        new Thread(new Runnable() {

            URL httpUrl = null;
            HttpURLConnection connection = null;
            FileInputStream fileInputStream = null;
            DataOutputStream dataOutputStream = null;



            @Override
            public void run() {

                //边界标识，随机生成
                String BOUNDARY = UUID.randomUUID().toString();
                String PREFIX = "--";
                String LINE_END = "\r\n";
                String CONTENT_TYPE = "multipart/form-data";    //内容类型

                File file = new File(localPath + "/" + localFile);
                long fileSize = file.length();
                long transferredSize = 0;
                int process = 0;

                try {
                    //httpUrl = new URL(targetUrl + "/" + remoteFile);
                    httpUrl = new URL(targetUrl);
                    connection = (HttpURLConnection) httpUrl.openConnection();

                    //选择流式输出，否则会出现内存溢出的错误：Throwing OutOfMemoryError
                    connection.setChunkedStreamingMode(4 * 1024);

                    connection.setReadTimeout(5000);
                    connection.setConnectTimeout(5000);
                    connection.setDoInput(true);        //允许输入流
                    connection.setDoOutput(true);       //允许输出流
                    connection.setUseCaches(false);     //不允许使用缓存
                    connection.setRequestMethod("POST");    //请求方式
                    connection.setRequestProperty("Charset", "UTF-8");  //设置编码
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

                    dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    StringBuffer sb = new StringBuffer();
                    sb.append(PREFIX);
                    sb.append(BOUNDARY);
                    sb.append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + remoteFile + "\"" + LINE_END);
                    sb.append("Content-Type: application/octet-stream; charset=" + "UTF-8" + LINE_END);
                    sb.append(LINE_END);
                    dataOutputStream.write(sb.toString().getBytes());

                    fileInputStream = new FileInputStream(file);
                    byte[] bytes = new byte[4 * 1024];
                    int len = 0;
                    int temp_process = 0;
                    while ((len = fileInputStream.read(bytes)) != -1) {
                        dataOutputStream.write(bytes, 0, len);

                        transferredSize += len;
                        temp_process = (int) (100 * transferredSize / fileSize);
                        if (temp_process > process) {
                            process = temp_process;
                            callback.upload(UPLOAD_RUNNING, process);
                        }
                    }

                    dataOutputStream.write(LINE_END.getBytes());
                    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                    dataOutputStream.write(end_data);
                    dataOutputStream.flush();

                    if (fileSize != transferredSize) {
                        Log.e(TAG, "Size of Upload file wrong!");
                        Log.d(TAG, "Remote file size:" + transferredSize + "Byte, local file size:" + fileSize + "Byte");
                        callback.upload(UPLOAD_FINISHED, FAILED);
                    } else {
                        int res = connection.getResponseCode();
                        if (HttpURLConnection.HTTP_OK == res) {
                            Log.d(TAG, "upload file:" + localPath + "/" + localFile + ", size:" + fileSize + "Byte success");
                            callback.upload(UPLOAD_FINISHED, SUCCESS);
                        } else {
                            Log.d(TAG, "upload file failed, http server respone:" + res);
                            callback.upload(UPLOAD_FINISHED, FAILED);
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "upload file failed!");
                    e.printStackTrace();
                    callback.upload(UPLOAD_FINISHED, FAILED);
                } finally {
                    try {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "close input output stream error!");
                    }
                }

            }
        }).start();

    }



}
