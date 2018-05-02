package com.labis.mycl.contents;

public class Item {

    String index;
    String name;
    String imgUrl;

    public String getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imgUrl;
    }

    public Item(String index, String name, String imgUrl) {
        this.index = index;
        this.name = name;
        this.imgUrl = imgUrl;
    }
}
