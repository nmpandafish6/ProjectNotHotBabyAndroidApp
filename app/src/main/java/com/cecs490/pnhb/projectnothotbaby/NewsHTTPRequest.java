package com.cecs490.pnhb.projectnothotbaby;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nicolas on 2/25/2018.
 */

public class NewsHTTPRequest extends AsyncTask<Void, Void, Boolean> {


    private Context m_context;
    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;
    private RecyclerView mRecyclerView;
    private ArrayList<String> fetchQueue = new ArrayList<>();

    public NewsHTTPRequest(Context context){
        this.m_context = context;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String fileName = "nothotbabycecs490.tumblr.com_rss";
        try {
            FileInputStream inputStream =  m_context.openFileInput(fileName);
            mFeedModelList = parseFeed(inputStream);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void onPostExecute(Boolean success){
        if(success){
            // Fill RecyclerView
            Log.e("XML_TAG","OPENED URL");
            for(RssFeedModel model: mFeedModelList){
                Log.e("XML_TAG", model.toString());
            }
            Log.e("FETCH_TAG", "READY TO FETCH " + fetchQueue.size() + " ITEMS");
            Thread waitForRSSReadyThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!fetchQueue.isEmpty()){
                        FetchExternalResourceTask resourceFetcher = new FetchExternalResourceTask(m_context);
                        String url = fetchQueue.remove(0);
                        Log.e("FETCH_TAG", "FETCHING : " + url);
                        resourceFetcher.execute(url);
                        while(!resourceFetcher.isCompleted()) {
                            //Log.e("WAITING_TAG", "I AM WAITING FOR RSS");
                        }
                    }
                    Log.e("FETCH_TAG", "I FETCHED ALL THE THINGS");
                    try {
                        mRecyclerView = (RecyclerView) ((Activity) m_context).findViewById(R.id.news_feed);
                        mRecyclerView.setAdapter(new RssFeedListAdapter(mFeedModelList, m_context));
                    }catch(Exception e){
                    }
                }
            });
            waitForRSSReadyThread.start();
        }
    }

    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException,
            IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.e("XML_TAG", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                    Log.e("READING_TAG","READING: " + description);
                    Matcher m = Pattern.compile("\"(https://.+?)\"").matcher(description);
                    while(m.find()){
                        String resource = m.group(1);
                        fetchQueue.add(resource);
                        Log.e("FETCH_TAG", "QUEUED : " + resource);
                    }
                }

                if (description != null) {
                    title += "";
                    link += "";
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description);
                        items.add(item);
                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;
                    }

                    title = null;
                    link = null;
                    description = null;
                    isItem = false;
                }
            }
            return items;
        } finally {
            inputStream.close();
        }
    }
}
