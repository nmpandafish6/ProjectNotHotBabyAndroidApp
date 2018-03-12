package com.cecs490.pnhb.projectnothotbaby;

/**
 * Created by Nicolas on 2/25/2018.
 */

public class RssFeedModel {

    public String title;
    public String link;
    public String description;

    public RssFeedModel(String title, String link, String description) {
        this.title = title;
        this.link = link;
        this.description = description;
    }

    @Override
    public String toString(){
        return title + "\n" + link + "\n" + description;
    }
}