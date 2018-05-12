package com.labis.mycl.model;

public class Content {
    // content_list table
    public final String id;
    public final String gen_id;
    public final int season;
    public final String name;
    public final String name_org;
    public final int chapter_end;
    public final String theatrical;
    public final String series_id;
    public final String summary;
    public final String image;

    // content_my table
    public final String user_id;
    public final String score;
    public final String comment;
    public int chapter;

    public Content(String id, String gen_id, int season, String name, String name_org, int chapter_end, String theatrical, String series_id, String summary, String image,
    String user_id, String score, String comment, int chapter) {
        this.id = id;
        this.gen_id = gen_id;
        this.season = season;
        this.name = name;
        this.name_org = name_org;
        this.chapter_end = chapter_end;
        this.theatrical = theatrical;
        this.series_id = series_id;
        this.summary = summary;
        this.image = image;

        this.user_id = user_id;
        this.score = score;
        this.comment = comment;
        this.chapter = chapter;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

}
