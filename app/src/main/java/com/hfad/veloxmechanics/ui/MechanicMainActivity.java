package com.hfad.veloxmechanics.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.SettingInjectorService;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.BottomBarAdapter;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.service.GpsService;
import com.hfad.veloxmechanics.utility.NoSwipePager;

import java.util.Calendar;

public class MechanicMainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    // Constants
    private static final String TAG = "MechanicMainActivity";

    //Database Related
    FirebaseFirestore mDb;

    //Widgets
    private NoSwipePager viewPager;
    private CoordinatorLayout coordinatorLayout;
    //Variables
    private BottomBarAdapter pagerAdapter;
    private MechanicProfileFragment mechanicProfileFragment = new MechanicProfileFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private TopicsFragment topicsFragment = new TopicsFragment();

    private String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private final int PERMISSION_CODE = 23;
    private ListenerRegistration mMechanicDeletedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDb = FirebaseFirestore.getInstance();


        //getLocationPermission();
        //getStoragePermission();
        //If mechanic is deleted change the sharedPreferences so mechanic can't login

        askPermissions();

        initToolbar();
        initWidgets();
        initPagerAdapter();
        checkIfDeleted();
    }

    private void askPermissions() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_CODE);
        }
    }

    private boolean hasPermission() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkIfDeleted() {
        DocumentReference documentReference = mDb.collection(getString(R.string.collection_user_type))
                .document(FirebaseAuth.getInstance().getUid());
        mMechanicDeletedListener = documentReference.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null)
                return;
            Integer type = Integer.parseInt(documentSnapshot.get("type").toString());
            if (type == Constants.DELETED) {
                updateSharedPreferences();
                Toast.makeText(MechanicMainActivity.this, "Sorry Your account has been deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSharedPreferences() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.cached_user_type), Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(FirebaseAuth.getInstance().getUid(), Constants.DELETED);
        editor.apply();
        logout();
    }


    private void initPagerAdapter() {
        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager(), 0);
        pagerAdapter.addFragments(mechanicProfileFragment);
        pagerAdapter.addFragments(chatFragment);
        pagerAdapter.addFragments(topicsFragment);

        viewPager.setAdapter(pagerAdapter);
    }


    private void initWidgets() {
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);

        coordinatorLayout = findViewById(R.id.coordinator);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);
    }

    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showPermissionErrorMessage();
                        }
                    }
                }
        }


    }

    private void showPermissionErrorMessage() {
        /*Snackbar snackbar = Snackbar.make(viewPager,"Permissions are necessary so Velox can function properly",Snackbar.LENGTH_LONG);
        snackbar.setAction("Accept", view -> {
            askPermissions();
        });

        snackbar.show();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(task -> goToMainPage());
    }

    private void goToMainPage() {
        Intent intent = new Intent(MechanicMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main:
                viewPager.setCurrentItem(0);
                return true;
            case R.id.chat:
                viewPager.setCurrentItem(1);
                return true;
            case R.id.topic:
                viewPager.setCurrentItem(2);
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMechanicDeletedListener!=null){
            mMechanicDeletedListener.remove();
        }
    }
}
