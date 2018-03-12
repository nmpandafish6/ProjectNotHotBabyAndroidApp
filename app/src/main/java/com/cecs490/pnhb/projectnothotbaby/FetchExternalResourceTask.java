package com.cecs490.pnhb.projectnothotbaby;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Nicolas on 3/5/2018.
 */

public class FetchExternalResourceTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private boolean isComplete = false;

    public FetchExternalResourceTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        isComplete = false;
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("FILE_TAG", "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // download the file
            input = connection.getInputStream();
            int urlStart = Math.max(sUrl[0].indexOf("http://") + "http://".length(),
                    sUrl[0].indexOf("https://") + "https://".length());
            String fileName = sUrl[0].substring(urlStart);
            //File outputFile = new File(, fileName);
            //Log.e("FILE_TAG", outputFile.getAbsolutePath());
            //boolean success = outputFile.createNewFile();
            //Log.e("FILE_TAG", "CREATE FILE : " + success);
            //output = new FileOutputStream(outputFile);
            fileName = fileName.replaceAll("/", "_");
            File file = new File(context.getFilesDir(), fileName);
            if(!file.exists()) {
                output = this.context.openFileOutput(fileName, Context.MODE_PRIVATE);

                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    output.write(data, 0, count);
                }
            }
        } catch (Exception e) {
            Log.e("FILE_TAG", e.toString());
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        Log.e("FETCH_TAG", "I SHOULD BE COMPLETE");
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.e("FETCH_TAG", "I AM COMPLETE");
        isComplete = true;
    }

    public boolean isCompleted(){
        return isComplete;
    }
}
