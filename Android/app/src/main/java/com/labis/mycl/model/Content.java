package com.labis.mycl.model;

import java.io.Serializable;

public class Content implements Serializable {
    // content_list table
    public String id;
    public final String gen_id;
    public final int season;
    public final String name;
    public final String name_org;
    public int chapter_end;
    public final int theatrical;
    public String series_id;
    public final String summary;
    public final String publisher;
    public final int auth;
    public String image;

    // content_my table
    public String user_id;
    public String score;
    public String comment;
    public int chapter;
    public int favorite;

    public Content(String id, String gen_id, int season, String name, String name_org, int chapter_end, int theatrical, String series_id, String summary,
                   String publisher, int auth, String image, String user_id, String score, String comment, int chapter, int favorite) {
        this.id = id;
        this.gen_id = gen_id;
        this.season = season;
        this.name = name;
        this.name_org = name_org;
        this.chapter_end = chapter_end;
        this.theatrical = theatrical;
        this.series_id = series_id;
        this.summary = summary;
        this.publisher = publisher;
        this.auth = auth;
        this.image = image;

        this.user_id = user_id;
        this.score = score;
        this.comment = comment;
        this.chapter = chapter;
        this.favorite = favorite;
    }

    public Content(String gen_id, int season, String name, String name_org, int theatrical, String summary,
                   String publisher, int auth, String image) {
        this.gen_id = gen_id;
        this.season = season;
        this.name = name;
        this.name_org = name_org;
        this.theatrical = theatrical;
        this.summary = summary;
        this.publisher = publisher;
        this.auth = auth;
        this.image = image;
    }

    public int getChapter() {
        return chapter;
    }
    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public String getImage() { return image; }
    public void setImage(String url) {
        this.image = url;
    }

}
