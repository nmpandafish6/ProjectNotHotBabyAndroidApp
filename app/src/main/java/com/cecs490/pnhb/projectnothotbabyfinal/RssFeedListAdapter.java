package com.cecs490.pnhb.projectnothotbabyfinal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Nicolas on 2/25/2018.
 */

public class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private List<RssFeedModel> mRssFeedModels;

    public static class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;

        public FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
        }
    }

    private Context m_context;
    public RssFeedListAdapter(List<RssFeedModel> rssFeedModels, Context context) {
        mRssFeedModels = rssFeedModels;
        m_context = context;
    }

    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rss_feed, parent, false);
        FeedModelViewHolder holder = new FeedModelViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        ((TextView)holder.rssFeedView.findViewById(R.id.titleText)).setText(rssFeedModel.title);
        ((TextView)holder.rssFeedView.findViewById(R.id.descriptionText))
                .setText(Html.fromHtml(rssFeedModel.description, Html.FROM_HTML_MODE_COMPACT,
                        new ImageGetter(), null));
        ((TextView)holder.rssFeedView.findViewById(R.id.linkText)).setText(rssFeedModel.link);
    }

    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }

    private class ImageGetter implements Html.ImageGetter{

        @Override
        public Drawable getDrawable(String source) {
            int urlStart = Math.max(source.indexOf("http://") + "http://".length(),
                    source.indexOf("https://") + "https://".length());
            String fileName = source.substring(urlStart);
            fileName = fileName.replaceAll("/", "_");
            Log.e("IMAGE_GETTER_TAG", fileName);
            File file = new File(m_context.getFilesDir(), fileName);
            Log.e("IMAGE_GETTER_TAG", file.getAbsolutePath());
            if(file.exists()) {
                Drawable d = Drawable.createFromPath(file.getAbsolutePath());
                d.setBounds(0, 0, getScreenWidth(), getScreenWidth() * d.getIntrinsicHeight() / d.getIntrinsicWidth());
                return d;
            }else{
                return null;
            }
        }

        private int getScreenWidth() {
            return Resources.getSystem().getDisplayMetrics().widthPixels;
        }

        private int getScreenHeight() {
            return Resources.getSystem().getDisplayMetrics().heightPixels;
        }
    }
}