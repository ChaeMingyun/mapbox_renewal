package com.example.chaemingyun.qwerty.mapbox;

/**
 * Created by chaemingyun on 2016. 8. 4..
 */


import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chaemingyun.qwerty.AddMarkerDialog;
import com.example.chaemingyun.qwerty.MarkerInfo;
import com.example.chaemingyun.qwerty.R;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;

import java.util.ArrayList;


public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    private MapboxMap map;
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;
    AddMarkerDialog addMarkerDialog; //dialog 객체

    final LatLng FIRST_POSITION = new LatLng(37.45, 126.65);    //초기 이동 마커의 위치
    LatLng currentPosition = FIRST_POSITION;    //현재 마커의 위치
    private static final int PERMISSIONS_LOCATION = 0;
    private boolean markerFlag = true;  //dialog의 취소가 눌렸는지를 알려준다.

    final int OPEN_GELLERY = 100;   //갤러리 불러올 request 코드

    Icon icon;  //마커에 넣을 임시 아이콘

    ArrayList<MarkerInfo> markerArrayList;  //마커들을 저장할 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo 데이터베이스에서 데이터 불러와야되는 시점

        //dialog객체 생성 및 설정
        addMarkerDialog = new AddMarkerDialog(MapActivity.this);
        addMarkerDialog.setTitle("발자취 남기기");

        //dialog가 사라졌을때 발생
        addMarkerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //현재 이동마커의 위치에 입력받은 내용들로 마커 생성
                if (markerFlag) {
                    //todo storage 에 올라가는 시점
                    //todo storage 에서 내려받는 시점
                    //todo 데이터베이스에 저장이 되어야되는 시점
                    //todo 데이터베이스에서 데이터 불러와야되는 시점
//                    MarkerOptions ms = new MarkerOptions()
//                            .position(currentPosition)
//                            .title(addMarkerDialog.getEditTextTitle())
//                            .snippet(addMarkerDialog.getEditTextContents());
//                    ms.setIcon(icon);
//                    map.addMarker(ms);
                }

                addMarkerDialog.clearText();//입력칸에 남아있는 내용 초기화
                markerFlag = true;//초기화
            }
        });

        //마커추가에서 취소 버튼 눌었을때 발생
        addMarkerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Toast.makeText(MapActivity.this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
                markerFlag = false; //취소가 눌렸음을 알려준다.
            }
        });

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

    //팝업메뉴 버튼
    public void onClick_menu(View v) {
        PopupMenu popup = new PopupMenu(MapActivity.this, v);
        getMenuInflater().inflate(R.menu.minimenu, popup.getMenu());
        popup.setOnMenuItemClickListener(listener);
        popup.show();
    }

    PopupMenu.OnMenuItemClickListener listener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            // TODO Auto-generated method stub
            switch (item.getItemId()) {

                case R.id.add_mark:
                    AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this);

                    addMarkerDialog.show();//만들어놓은 dialog나옴
                    break;

                case R.id.logout:
                    Toast.makeText(MapActivity.this, "로그아웃", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };

    //갤러리에서 이미지 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Toast.makeText(getBaseContext(), "resultCode : " + resultCode, Toast.LENGTH_SHORT).show();

        if (requestCode == OPEN_GELLERY) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    //Uri에서 이미지 이름을 얻어온다.
                    //String name_Str = getImageNameToUri(data.getData());

                    Bitmap gellery_image;   //불러온 이미지 저장할 변수

                    //이미지 데이터를 비트맵으로 받아온다.
                    gellery_image = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    gellery_image = Bitmap.createScaledBitmap(gellery_image, 50, 50, true);
                    IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
                    icon = iconFactory.fromBitmap(gellery_image);

                    gellery_image = Bitmap.createScaledBitmap(gellery_image, 200, 200, true);

                    addMarkerDialog.setImageView_img(gellery_image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}