package com.elmaravilla.prowebbrowser.model;

import io.realm.RealmObject;

public class bookmarksModel extends RealmObject {
    String url , title , date;
    byte[] favicon;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public byte[] getFavicon() {
        return favicon;
    }

    public void setFavicon(byte[] favicon) {
        this.favicon = favicon;
    }
}
