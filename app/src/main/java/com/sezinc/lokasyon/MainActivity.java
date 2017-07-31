package com.sezinc.lokasyon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap map;
    //Google play servis kütüphaneleri kullanılmak istendiğinde "Google Api Client" örneği tanımlanır.
    //Google API Client, Google Play servislerine ortak giriş noktası oluşturur ve
    // kullanıcı cihazıyla herbir Google servisine ağ bağlanısını yönetir.
    private GoogleApiClient mGoogleApiClient;
    private final LatLng mDefaultLocation = new LatLng(41.007651, 28.976956);
    private static final int DEFAULT_ZOOM = 15;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // Fused Location Provider tarafından en son bilinen yer olarak alınan lokasyon.
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private static final String TAG = MainActivity.class.getSimpleName();
//Lokasyonu yazılı olarak vermek için
protected Location mLastLocation;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

    private final String LOG_TAG ="TestApp";
    private LocationRequest mLocationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fused Location Provider ve the Places API kullanmak için Play services client kurar .
        // Google Places API and the Fused Location Provider isteği için addApi() kullanır.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
//Enlem Boylam bilgisi
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));

//Haritanın atanması
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Googleapiclient'a bağlanır.
        mGoogleApiClient.connect();
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Kullanıcı geçersiz kıldığında hala bağlı ise GoogleApiClient bağlanısı sonlandırılır.
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Google Play services client başarılı bir şekilde bağlandığında haritayı kurar.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        mLocationRequest = LocationRequest.create();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(10); // Update location every second

     //   mLastLocation = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
       // mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient); //LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient bağlantısı askıya alındı");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient bağlantısı koptu");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, location.toString());

    }




    /**
     * Hazır olduğunda haritayı çalıştırır.
     * Bu callback Harita kullanıma hazır olduğunda tetiklenir.
     */
    @Override
    public void onMapReady(GoogleMap mMap) {
        map = mMap;

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Lokasyonum katmanını ve ilgili controlü açar .
        updateLocationUI();

        // Hali hazır konumu alır ve haritanın pozisyonunu belirler
        getDeviceLocation();
// Haritaya dokunduğunda enlem boylam bilgisi verir

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onMapClick(LatLng point){
                //Haritada tıklanan noktanın enlem boylamını alır
                double pointLat=point.latitude;
                double pointLong= point.longitude;
                //Uygun formatta bir string e atar.
                DecimalFormat precision = new DecimalFormat("#,#######");
                String txtLatLong= String.valueOf(pointLat);// precision.format(pointLat).toString();
                txtLatLong+= ", ";
                txtLatLong += String.valueOf(pointLong);//precision.format(pointLong).toString();

              map.addMarker(new MarkerOptions().position(point).title(txtLatLong));
                map.animateCamera(CameraUpdateFactory.newLatLng(point));

                //test


            }
        });


    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }

    /*
     *Lokasyon izni isteği. Cihazın lokasyonunu alabiliriz. İzin isteğinin sonucu callback ile ele alınıyor.
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);

        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }
// Uygulama çalıştığına verilen izinler
// Kullanıcıya lokasyon izni verip veya vermemesi için fırsat tanır.
    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Cihazın en iyi ve en son konumunu alır. Enderde olsa bazı durumlarda boş olabilir.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);


        }

        // Haritanın kamera pozisyonunu cihazın şimdiki konumuna ayarlar.
        if (mCameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Şimdiki konum bilgileri boş. Default kullanım");
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
//Bulunduğumuz yerin enlem boylam bilgisi
        if (mLastKnownLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastKnownLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastKnownLocation.getLongitude()));
        }
    }
// İzin isteğinin sonucunu ele alır
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // eğer istek iptal edilirse, sonuç dizisi boş olur.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
