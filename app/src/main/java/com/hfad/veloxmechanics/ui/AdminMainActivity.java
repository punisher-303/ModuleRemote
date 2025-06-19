package com.hfad.veloxmechanics.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.BottomBarAdapter;
import com.hfad.veloxmechanics.adapter.MechanicAdapterForAdmin;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.utility.NoSwipePager;

import java.util.ArrayList;
import java.util.List;

public class AdminMainActivity extends AppCompatActivity implements View.OnClickListener , BottomNavigationView.OnNavigationItemSelectedListener{

    // Widgets

    private FloatingActionButton mAddBtn;

    private NoSwipePager viewPager;

    //Variables
    private BottomBarAdapter pagerAdapter;
    private AdminHomeFragment  adminHomeFragment = new AdminHomeFragment();
    private ChatFragment chatFragment = new ChatFragment();
    private TopicsFragment topicsFragment = new TopicsFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        initToolbar();
        initWidgets();
        initPagerAdapter();
    }
    private void initPagerAdapter() {
        pagerAdapter = new BottomBarAdapter(getSupportFragmentManager(), 0);
        pagerAdapter.addFragments(adminHomeFragment);
        pagerAdapter.addFragments(chatFragment);
        pagerAdapter.addFragments(topicsFragment);

        viewPager.setAdapter(pagerAdapter);
    }

    private void initWidgets() {

        mAddBtn = findViewById(R.id.add_btn);
        mAddBtn.setOnClickListener(this);

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(this);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);


    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);

        MenuItem search = menu.findItem(R.id.search);

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
        Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_btn:
                Intent intent = new Intent(AdminMainActivity.this, MechanicRegisterActivity.class);
                startActivity(intent);
                break;
        }
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
}
