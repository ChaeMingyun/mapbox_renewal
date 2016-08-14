package com.example.chaemingyun.qwerty.firebase.database.model;

/**
 * Created by chaemingyun on 2016. 8. 10..
 */
public class FootPrint {
    private String footPrintUid;
    private String latitude;
    private String longitude;
    private String title;
    private String snippet;
    private String imageUri;

    public String getFootPrintUid() {
        return footPrintUid;
    }

    public void setFootPrintUid(String footPrintUid) {
        this.footPrintUid = footPrintUid;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
