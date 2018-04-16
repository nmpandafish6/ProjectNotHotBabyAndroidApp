package com.cecs490.pnhb.projectnothotbaby;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.cecs490.pnhb.projectnothotbaby.ResourceMaster.news_adapter;

/**
 * Created by Nicolas on 2/25/2018.
 */

public class WeatherHTTPRequest extends AsyncTask<Void, Void, String> {

    private Context m_context;
    private Bitmap mIcon11;

    public WeatherHTTPRequest(Context context){
        this.m_context = context;
    }
    @Override
    protected String doInBackground(Void... voids) {
        URL url = null;
        String iconURI = "";
        try {
            url = new URL("http://api.openweathermap.org/data/2.5/forecast?lat=33.838008&lon=-118.159524&APPID=676411a95bbb3b23c1ae504adef40290");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = "NULL";
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int ch;
            StringBuilder sb = new StringBuilder();
            Thread.sleep(5000);
            while((ch = in.read()) != -1)
                sb.append((char)ch);
            data = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        Log.e("WEATHER_TAG",data);

        try {
            JSONObject json = new JSONObject(data);

            Map<String, Integer> icon = new HashMap<>();
            for(int i = 0; i < 8; i++){
                JSONObject forecastInstance = json.getJSONArray("list").getJSONObject(i);
                String iconTemp = forecastInstance.getJSONArray("weather").getJSONObject(0).getString("icon");
                Integer c = icon.get(iconTemp);
                if(c == null) c = new Integer(0);
                c++;
                icon.put(iconTemp,c);

            }
            Map.Entry<String, Integer> mostRepeated = null;
            for(Map.Entry<String, Integer> ele: icon.entrySet()){
                if(mostRepeated == null || mostRepeated.getValue()< ele.getValue())
                    mostRepeated = ele;
            }
            if(mostRepeated != null)
                iconURI = mostRepeated.getKey();
            String urldisplay = "http://openweathermap.org/img/w/" + iconURI + ".png";
            ResourceMaster.preferenceEditor.putString(Constants.LAST_ICON_URI_KEY, urldisplay);
            ResourceMaster.preferenceEditor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return data;
    }

    protected void onPostExecute(String html){
        final String urldisplay = ResourceMaster.preferences.getString(Constants.LAST_ICON_URI_KEY, "");


        Thread waitForReadyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FetchExternalResourceTask fetchTask = new FetchExternalResourceTask(ResourceMaster.m_context);
                fetchTask.execute(urldisplay);
                while(!fetchTask.isCompleted()) {}
                ResourceMaster.m_activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIcon11 = null;
                        int urlStart = Math.max(urldisplay.indexOf("http://") + "http://".length(),
                                urldisplay.indexOf("https://") + "https://".length());
                        String fileName = urldisplay.substring(urlStart);
                        fileName = fileName.replaceAll("/", "_");
                        Log.e("IMAGE_GETTER_TAG", fileName);
                        File file = new File(ResourceMaster.m_context.getFilesDir(), fileName);
                        Log.e("IMAGE_GETTER_TAG", file.getAbsolutePath());
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
                        ImageView news_weather_icon = (ImageView) ((Activity)m_context).findViewById(R.id.news_weather_icon);
                        news_weather_icon.setImageBitmap(bitmap);
                        ImageView home_weather_icon = (ImageView) ((Activity)m_context).findViewById(R.id.home_weather_icon);
                        home_weather_icon.setImageBitmap(bitmap);
                    }
                });
            }
        });
        waitForReadyThread.start();


        double highTemp; String location;
        try {
            JSONObject json = new JSONObject(html);
            location = json.getJSONObject("city").getString("name") + ", " + json.getJSONObject("city").getString("country");
            highTemp = -10000;
            Map<String, Integer> weather = new HashMap<>();
            for(int i = 0; i < 8; i++){
                JSONObject forecastInstance = json.getJSONArray("list").getJSONObject(i);
                int time = forecastInstance.getInt("dt");
                String weatherTemp = forecastInstance.getJSONArray("weather").getJSONObject(0).getString("main");
                double temp = (forecastInstance.getJSONObject("main").getDouble("temp") - 273)*9/5+32;
                highTemp = Math.max(temp,highTemp);
                Integer c = weather.get(weatherTemp);
                if(c == null) c = new Integer(0);
                c++;
                weather.put(weatherTemp,c);

            }
            Map.Entry<String, Integer> mostRepeated = null;
            for(Map.Entry<String, Integer> ele: weather.entrySet()){
                if(mostRepeated == null || mostRepeated.getValue()< ele.getValue())
                    mostRepeated = ele;
            }
            if(mostRepeated != null)
                Log.e("WEATHER_TAG", "The WEATHER in " + location + " is " + mostRepeated.getKey());
            Log.e("WEATHER_TAG", "The HIGH in " + location + " is " + highTemp);


            TextView news_location = (TextView) ((Activity)m_context).findViewById(R.id.news_location);
            TextView news_weather = (TextView)  ((Activity)m_context).findViewById(R.id.news_weather);
            TextView news_weather_condtion = (TextView)  ((Activity)m_context).findViewById(R.id.news_weather_conditions);

            ResourceMaster.preferenceEditor.putString(Constants.LAST_WEATHER_KEY, "High for Today:   " + Math.round(highTemp*10)/10+ "°F");
            ResourceMaster.preferenceEditor.putString(Constants.LAST_WEATHER_CONDITION_KEY, mostRepeated.getKey());
            ResourceMaster.preferenceEditor.putString(Constants.LAST_WEATHER_LOCATION_KEY, location);
            ResourceMaster.preferenceEditor.commit();

            news_location.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_LOCATION_KEY, ""));
            news_weather.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_KEY, ""));
            news_weather_condtion.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_CONDITION_KEY, ""));

            TextView home_location = (TextView) ((Activity)m_context).findViewById(R.id.home_location);
            TextView home_weather = (TextView)  ((Activity)m_context).findViewById(R.id.home_weather);
            TextView home_weather_condtion = (TextView)  ((Activity)m_context).findViewById(R.id.home_weather_conditions);

            home_location.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_LOCATION_KEY, ""));
            home_weather.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_KEY, ""));
            home_weather_condtion.setText( ResourceMaster.preferences.getString(Constants.LAST_WEATHER_CONDITION_KEY, ""));

            Log.e("BITMAP_TAG","BITMAP SET");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
