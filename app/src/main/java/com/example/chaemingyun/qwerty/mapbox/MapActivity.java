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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chaemingyun.qwerty.AddMarkerDialog;
import com.example.chaemingyun.qwerty.MarkerInfo;
import com.example.chaemingyun.qwerty.R;
import com.example.chaemingyun.qwerty.firebase.auth.GoogleSignInActivity;
import com.example.chaemingyun.qwerty.firebase.database.model.FootPrint;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MapActivity extends AppCompatActivity {
    //firebase Auth
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = storage.getReferenceFromUrl("gs://qwerty-7992b.appspot.com");
    private Uri mDownloadUrl;
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    //firebase Database
    private DatabaseReference mDatabase;

    //mapbox
    private MapView mapView;
    private MapboxMap map;
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;
    AddMarkerDialog addMarkerDialog; //dialog 객체
    final LatLng FIRST_POSITION = new LatLng(37.45, 126.65);    //초기 이동 마커의 위치
    LatLng currentPosition = FIRST_POSITION;    //현재 마커의 위치
    private static final int PERMISSIONS_LOCATION = 0;
    private boolean markerFlag = true;  //dialog의  취소가 눌렸는지를 알려준다.

    //gallery
    final int OPEN_GELLERY = 100;   //갤러리 불러올 request 코드
    Icon icon;  //마커에 넣을 임시 아이콘
    Uri uri;

    ArrayList<MarkerInfo> markerArrayList;  //마커들을 저장할 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //todo 데이터베이스에서 데이터 불러와야되는 시점
        //====== 1.add marker 관련 ======//
        if (savedInstanceState != null) {
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        Intent intent = new Intent(this.getIntent());
        final String userUid = intent.getStringExtra("uid");

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

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
                    FootPrint footPrint = new FootPrint();
                    try {
                        uploadFromUri(uri, userUid);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //todo 데이터베이스에 저장이 되어야되는 시점
                    String key = mDatabase.child(userUid).push().getKey();

                    String currentLatLng = currentPosition.toString();
                    String[] split = currentLatLng.split(",");
                    String latitude = split[0].substring(17);
                    String longitude = split[1].substring(11);
                    String title = addMarkerDialog.getEditTextTitle();
                    String snippet = addMarkerDialog.getEditTextContents();
                    String imageUrl = mDownloadUrl.toString();

                    footPrint.setLatitude(latitude);
                    footPrint.setLongitude(longitude);
                    footPrint.setTitle(title);
                    footPrint.setSnippet(snippet);
                    footPrint.setImageUrl(imageUrl);

                    Map<String, Object> postValues = footPrint.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(userUid + key, postValues);
                    mDatabase.updateChildren(childUpdates);

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

        //마커추가에서 취소 버튼 눌렀을때 발생
        addMarkerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Toast.makeText(MapActivity.this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
                markerFlag = false; //취소가 눌렸음을 알려준다.
            }
        });


        //====== 2.mapbox 관련 ======//
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


        //====== 3.logout 관련 ======//
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    // [START upload_from_uri]
    private void uploadFromUri(Uri fileUri, String userUid) throws FileNotFoundException {
        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg

        StorageReference photoRef = mStorageRef.child(userUid).child(fileUri.getLastPathSegment());
        // [END get_child_ref]
        // Upload file to Firebase Storage
//        UploadTask uploadTask = photoRef.putFile(fileUri);
        InputStream inputStream = new FileInputStream(new File(fileUri.getPath()));
        UploadTask uploadTask = photoRef.putStream(inputStream);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDownloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }
    // [END upload_from_uri]

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
        outState.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
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
                    addMarkerDialog.show();//만들어놓은 dialog 나옴
                    break;

                case R.id.logout:
                    // Firebase sign out
                    mAuth.signOut();

                    // Google sign out
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
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
                    uri = data.getData();

                    Bitmap galleryImage;   //불러온 이미지 저장할 변수

                    //이미지 데이터를 비트맵으로 받아온다.
                    galleryImage = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                    data.getData();

                    galleryImage = Bitmap.createScaledBitmap(galleryImage, 50, 50, true);
                    IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
                    icon = iconFactory.fromBitmap(galleryImage);

                    galleryImage = Bitmap.createScaledBitmap(galleryImage, 200, 200, true);

                    addMarkerDialog.setImageViewImage(galleryImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}