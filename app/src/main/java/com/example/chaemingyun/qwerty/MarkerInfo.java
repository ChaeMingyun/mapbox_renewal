package com.example.chaemingyun.qwerty;

import android.media.Image;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by 송원근 on 2016-08-10.
 */

public class MarkerInfo {

    private LatLng markerLatLng;
    private String markerTitle;
    private String markerContents;
    private Image markerImage;

    public MarkerInfo(LatLng markerLatLng, String markerTitle,String markerContents) {
        this.markerLatLng = markerLatLng;
        this.markerTitle = markerTitle;
        this.markerContents = markerContents;
    }

    public LatLng getMarkerLatLng() {
        return markerLatLng;
    }

    public void setMarkerLatLng(LatLng markerLatLng) {
        this.markerLatLng = markerLatLng;
    }

    public String getMarkerTitle() {
        return markerTitle;
    }

    public void setMarkerTitle(String markerTitle) {
        this.markerTitle = markerTitle;
    }

    public String getMarkerContents() {
        return markerContents;
    }

    public void setMarkerContents(String markerContents) {
        this.markerContents = markerContents;
    }

    public Image getMarkerImage() {
        return markerImage;
    }

    public void setMarkerImage(Image markerImage) {
        this.markerImage = markerImage;
    }
}
