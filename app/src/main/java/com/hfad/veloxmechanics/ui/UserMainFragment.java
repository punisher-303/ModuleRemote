package com.hfad.veloxmechanics.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.MechanicAdapter;
import com.hfad.veloxmechanics.adapter.SpinnerAdapter;
import com.hfad.veloxmechanics.model.ChatUser;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.model.SpinnerItem;
import com.hfad.veloxmechanics.model.User;

import java.util.ArrayList;
import java.util.Calendar;

import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.model.Distance;
import uk.co.mgbramwell.geofire.android.model.DistanceUnit;
import uk.co.mgbramwell.geofire.android.model.QueryLocation;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserMainFragment extends Fragment implements View.OnClickListener, MechanicAdapter.MechanicListener{

    //Constants
    private static final String TAG = "MyTag";

    // Widgets
    private Button mFindMechanicBtn;
    private RecyclerView mRecyclerView;
    private Spinner mSpecialtySpinner;
    private ProgressBar mProgressBar;
    //Database
    private GeoFire geoFireMechanic;
    private FirebaseFirestore mDb;
    // Variables
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private ArrayList<Mechanic> mMechanicList;
    private MechanicAdapter mMechanicAdapter;
    private GeoPoint mUserLocation;
    private boolean dialogShownOnce = false; // stop gps dialog from showing multiple times


    public UserMainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_main, container, false);


        mDb = FirebaseFirestore.getInstance();
        geoFireMechanic = new GeoFire(mDb.collection(getString(R.string.collection_mechanic)));

        getLocationPermission();


        initWidgets(view);
        initLocationListener();

        return view;
    }
    private void initLocationListener() {

        mLocationManager = (LocationManager) getActivity(). getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: called" + location.getLatitude() + " " + location.getLongitude());
                //if ((int) location.getAccuracy() > 30) {
                mLocationManager.removeUpdates(mLocationListener);
                findMechanics(new GeoPoint(location.getLatitude(), location.getLongitude()));
                Log.d(TAG, "onLocationChanged: Stopped");
                //}
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(TAG, "onProviderEnabled: called");
                disableButton();
            }

            @Override
            public void onProviderDisabled(String s) {
                if(!dialogShownOnce){
                    Log.d(TAG, "onProviderDisabled: called");
                    enableButton();
                    alertDialogNoGps();
                }
                dialogShownOnce = true;
            }
        };
    }

    private void findMechanics(GeoPoint geoPoint) {

        SpinnerItem item = (SpinnerItem) mSpecialtySpinner.getSelectedItem();

        final String specialty = item.getSpinnerText();

        mUserLocation = geoPoint;

        QueryLocation queryLocation = QueryLocation.fromDegrees(geoPoint.getLatitude(), geoPoint.getLongitude());
        Distance searchDistance = new Distance(8.0, DistanceUnit.KILOMETERS);



        geoFireMechanic
                .query()
                .whereEqualTo(getString(R.string.field_mechanic_online), true)
                .whereEqualTo(getString(R.string.field_mechanic_specialty),specialty)
                .whereNearTo(queryLocation, searchDistance)
                .build()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    enableButton();
                    Log.d(TAG, "onSuccess: Document Found");
                    for (QueryDocumentSnapshot m : queryDocumentSnapshots) {
                        Mechanic temp = m.toObject(Mechanic.class);
                        Log.d(TAG, "onSuccess: " + temp.getName());
                    }
                    mMechanicList.addAll(queryDocumentSnapshots.toObjects(Mechanic.class));
                    mMechanicAdapter.setmUserLocation(geoPoint);
                    mMechanicAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: Documents Not found" + e.getMessage());
                    Toast.makeText(getActivity(), "Sorry No Mechanics Found", Toast.LENGTH_LONG).show();
                    enableButton();
                });

    }

    // Alert Dialog for asking to enable gps location
    private void alertDialogNoGps() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Enable Gps")
                .setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    enableButton();
                });
        Dialog dialog = alert.create();
        dialog.show();

    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // Check for recent location and if it's been derived less than two minutes with accuracy bigger than 50
        if (location != null &&
                location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000 &&
                location.getAccuracy() > 50) {
            findMechanics(new GeoPoint(location.getLatitude(), location.getLongitude()));

        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            //just for checking
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,mLocationListener);
        }
    }

    private void initWidgets(View view) {
        // Initializing Spinner Item
        ArrayList<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem("Car", R.drawable.ic_car));
        list.add(new SpinnerItem("Bike", R.drawable.ic_motorcycle));

        mSpecialtySpinner = view.findViewById(R.id.specialty);

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(getActivity(), list);
        mSpecialtySpinner.setAdapter(spinnerAdapter);

        mProgressBar = view.findViewById(R.id.progress_bar);

        mFindMechanicBtn = view.findViewById(R.id.find_mechanic_btn);
        mFindMechanicBtn.setOnClickListener(this);

        mMechanicList = new ArrayList<>();
        mMechanicAdapter = new MechanicAdapter(getActivity(), mMechanicList, this);

        mRecyclerView = view.findViewById(R.id.recycler);
        mRecyclerView.setAdapter(mMechanicAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

    }


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: called");
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: called");
                } else {
                    getLocationPermission();
                }
                break;
        }

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.find_mechanic_btn:
                dialogShownOnce = false;
                disableButton();
                if(mMechanicList.size()>0){
                    mMechanicList.clear();
                    mMechanicAdapter.notifyDataSetChanged();
                }
                getUserLocation();
                break;
        }
    }

    @Override
    public void onChatBtnClicked(Mechanic mechanic) {

        // Create One Chat User
        // And Create One Current User

        ChatUser chatUser = new ChatUser();
        chatUser.setName(mechanic.getName());
        chatUser.setImage(mechanic.getImage());
        chatUser.setUser_id(mechanic.getUser_id());

        // Get the user name and send it to Chat Activity
        DocumentReference documentReference = mDb.collection(getString(R.string.collection_user))
                .document(FirebaseAuth.getInstance().getUid());
        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);

            ChatUser currentUser = new ChatUser();
            currentUser.setName(user.getName());
            currentUser.setUser_id(user.getUser_id());
            currentUser.setImage("default");

            Intent intent = new Intent(getActivity(),ChatActivity.class);
            intent.putExtra(getString(R.string.chat_user), (Parcelable) chatUser);
            intent.putExtra(getString(R.string.current_user),(Parcelable)currentUser);
            intent.putExtra(getString(R.string.user_type),Constants.USER);
            startActivity(intent);
        });

    }

    @Override
    public void showMapPage() {
        Intent intent = new Intent(getActivity(), MapActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("mechanicList", mMechanicList);

        intent.putExtras(bundle);
        intent.putExtra(getString(R.string.user_latitude), mUserLocation.getLatitude());
        intent.putExtra(getString(R.string.user_longitude), mUserLocation.getLongitude());

        startActivity(intent);
    }

    @Override
    public void onCallBtnClicked(Mechanic mechanic) {
        Uri number = Uri.parse("tel:" + mechanic.getPhone_number());

        Intent intent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(intent);
    }

    // stop user from continuosly hitting register button and show&hide progressbar
    public void disableButton() {
        mFindMechanicBtn.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void enableButton() {
        mFindMechanicBtn.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

}
