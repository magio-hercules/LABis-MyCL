package com.labis.mycl.rest.models;

public class Content {
    public final String id;
    public final String genid;
    public final String name;
    public final String theatrical;
    public final String series_id;
    public final String season_id;
    public final String summary;
    public final String image;

    public Content(String id, String genid, String name, String theatrical, String series_id, String season_id, String summary, String image) {
        this.id = id;
        this.genid = genid;
        this.name = name;
        this.theatrical = theatrical;
        this.series_id = series_id;
        this.season_id = season_id;
        this.summary = summary;
        this.image = image;
    }
}
