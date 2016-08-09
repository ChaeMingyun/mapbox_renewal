package com.example.chaemingyun.qwerty.mapbox;

/**
 * Created by chaemingyun on 2016. 8. 4..
 */


import com.example.chaemingyun.qwerty.R;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;

import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.animation.TypeEvaluator;
import android.widget.Toast;


public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxMap map;
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;

    final LatLng FIRST_POSITION = new LatLng(37.45, 126.65);    //초기 마커의 위치
    LatLng currentPosition = FIRST_POSITION;    //현재 마커의 위치
    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_map);

        locationServices = LocationServices.getLocationServices(MapActivity.this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // Customize map with markers, polylines, etc.
                map = mapboxMap;
                //학교위치에 마커(test)
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(37.450637, 126.657261))
                        .title("인하대학교 IT공과대학")
                        .snippet("Made By 컴공 채민균 양민승 송원근"));

                final Marker marker = mapboxMap.addMarker(new MarkerViewOptions()
                        .position(new LatLng(37.45, 126.65)));

                marker.getPosition();

                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {

                        // When the user clicks on the map, animate the marker to the click location
                        ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
                                new LatLngEvaluator(), marker.getPosition(), point);
                        markerAnimator.setDuration(2000);
                        markerAnimator.start();

                        currentPosition = point;
                    }
                });
            }
        });

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                }
            }
        });

        // Set up autocomplete widget

        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
        autocomplete.setAccessToken("pk.eyJ1IjoiY29hbHNyYnM3IiwiYSI6ImNpcmc1NTR2NTAwMWtnMW5yZWg0c3ZuM24ifQ.LZeDizCkf7HQvGwoQVIbxw");
        autocomplete.setType(GeocodingCriteria.TYPE_POI);
        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick(CarmenFeature feature) {
                Position position = feature.asPosition();
                updateMap(position.getLatitude(), position.getLongitude());
            }
//            @Override
//            public void OnFeatureClick(GeocodingFeature feature) {
//
//            }
        });
    }

    private void updateMap(double latitude, double longitude) {
        // Build marker
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Geocoder result"));

        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is
                        map.setCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(location))
                                .zoom(15)
                                .build());
                    }
                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude() +
                    ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude() +
                    ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

    public void onClick_menu(View v){
        PopupMenu popup= new PopupMenu(MapActivity.this, v);
        getMenuInflater().inflate(R.menu.minimenu, popup.getMenu());
        popup.setOnMenuItemClickListener(listener);
        popup.show();
    }

    PopupMenu.OnMenuItemClickListener listener= new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {

                case R.id.add_mark:
                    Toast.makeText(MapActivity.this, "마커 추가", Toast.LENGTH_SHORT).show();

                    map.addMarker(new MarkerOptions()
                            .position(currentPosition)
                            .title("test")
                            .snippet("Made By 컴공 채민균 양민승 송원근"));

                    break;

                case R.id.logout:
                    Toast.makeText(MapActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };
}
