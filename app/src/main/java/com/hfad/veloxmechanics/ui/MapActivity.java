package com.hfad.veloxmechanics.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.utility.BubbleTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    // Constants
    private static final String TAG = "MyTag";

    private GoogleMap mGoogleMap;

    private ArrayList<Mechanic> mMechanicList;
    private GeoPoint mUserLocation;

    private LatLng latLng = null;

    private List<Target> targets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        mMechanicList = bundle.getParcelableArrayList("mechanicList");
        mUserLocation = new GeoPoint(bundle.getDouble(getString(R.string.user_latitude)), bundle.getDouble(getString(R.string.user_longitude)));

        if (isGoogleServiceAvailable()) {
            setContentView(R.layout.activity_map);
            initMap();
        } else {
            setContentView(R.layout.error_map_layout);
        }

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean isGoogleServiceAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isGoogleServiceAvailable: Available");
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, Constants.GOOGLE_SERVICE_AVAILABILTY);
            dialog.show();
            Log.d(TAG, "isGoogleServiceAvailable: Resolvable");
        } else {
            Toast.makeText(this, "Sorry Google Service is not Available on your phone", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.setMyLocationEnabled(true);

        initMechanicMarkers();

    }

    private void initMechanicMarkers() {
        LatLng latLng = null;
        for (Mechanic m : mMechanicList) {
            if (m.getLocation() != null) {
                Log.d(TAG, "onMapReady: " + m.getLocation());
                latLng = new LatLng(m.getLocation().getLatitude(), m.getLocation().getLongitude());

                if (m.getImage().equals("default")) {
                    MarkerOptions options = new MarkerOptions()
                            .title(m.getName())
                            .position(latLng)
                            .snippet(m.getSpecialty());
                    mGoogleMap.addMarker(options);
                } else {

                    LatLng finalLatLng = latLng;
                    Target target = new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Log.d(TAG, "onBitmapLoaded: ");
                            Marker driver_marker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(finalLatLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    .title(m.getName())

                            );
                            targets.clear();
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.d(TAG, "onBitmapFailed: ");
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            Log.d(TAG, "onPrepareLoad: ");
                        }
                    };
                    targets.add(target);

                    Picasso.get()
                            .load(m.getImage())
                            .resize(200,200)
                            .centerCrop()
                            .transform(new BubbleTransform(this,5))
                            .into(target);
                }


            }
        }


        if (latLng != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f)
            );
        }
    }
}
