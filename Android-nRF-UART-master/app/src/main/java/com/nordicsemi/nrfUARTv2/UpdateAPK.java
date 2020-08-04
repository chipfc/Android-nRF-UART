package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Le Trong Nhan on 04/08/2020.
 */

public class UpdateAPK extends AsyncTask<String, String, String>  {
    private Context mContext;
    private UpdateAPKListener mListener;

    public UpdateAPK(Context context, UpdateAPKListener listener) {
        mContext = context;
        mListener = listener;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }
    protected  String doInBackground(String... f_url) {

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dcar_update.apk";
        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(path);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e("NPNtv", "Well that didn't work out so well...");
            Log.e("NPNtv", e.getMessage());
            //writeToFile("0*fail", mContext,"repo.txt");
        }
        return path;
    }

    protected void onPostExecute(String path) {

        if (mListener == null) { return; }
        mListener.onDownloadApkToUpdate(path);
    }
    public interface UpdateAPKListener {
        void onDownloadApkToUpdate(String path);
    }
}
