package com.hfad.veloxmechanics.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.SpinnerAdapter;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.model.SpinnerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MechanicRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    //Constants
    private static final String TAG = "MechanicRegisterActivit";

    //Widgets
    private Spinner mSpecialtySpinner;
    private Button mRegisterBtn;
    private TextInputLayout mNameInput;
    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;
    private TextInputLayout mPhoneNumber;
    private ProgressBar mProgressBar;
    private RelativeLayout mParentLayout;

    //Database Related
    private FirebaseFirestore mDb;
    FirebaseAuth mAuth2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_register);

        initToolbar();
        initDatabase();
        initWidgets();

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("New Mechanic");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initDatabase() {
        mDb = FirebaseFirestore.getInstance();


        // Create a User without signing in to it
        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl(getString(R.string.firebase_database_url))
                .setApiKey(getString(R.string.web_api_key))
                .setApplicationId(getString(R.string.project_id))
                .build();

        try{
            FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(),firebaseOptions,getString(R.string.project_name));
            mAuth2 = FirebaseAuth.getInstance(myApp);
        }
        catch (IllegalStateException e){
            mAuth2  = FirebaseAuth.getInstance(FirebaseApp.getInstance(getString(R.string.project_name)));
        }


    }

    private void initWidgets() {

        // Initializing Spinner Item
        ArrayList<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem("Car",R.drawable.ic_car));
        list.add(new SpinnerItem("Bike",R.drawable.ic_motorcycle));

        mSpecialtySpinner = findViewById(R.id.specialty);

        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this,list);
        mSpecialtySpinner.setAdapter(spinnerAdapter);


        mNameInput = findViewById(R.id.name);
        mEmailInput = findViewById(R.id.email);
        mPasswordInput = findViewById(R.id.password);
        mPhoneNumber = findViewById(R.id.phone_number);

        mProgressBar = findViewById(R.id.progress_bar);
        mParentLayout = findViewById(R.id.parent_layout);

        mRegisterBtn = findViewById(R.id.register_btn);
        mRegisterBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register_btn:
                disableButton();
                addMechanicToDatabase();
                break;
        }
    }

    private void addMechanicToDatabase() {

        SpinnerItem item = (SpinnerItem) mSpecialtySpinner.getSelectedItem();

        final String specialty = item.getSpinnerText();
        final String name = mNameInput.getEditText().getText().toString();
        final String email = mEmailInput.getEditText().getText().toString();
        final String phoneNumber = mPhoneNumber.getEditText().getText().toString();
        String password = mPasswordInput.getEditText().getText().toString();



        if(TextUtils.isEmpty(name)||TextUtils.isEmpty(email)||TextUtils.isEmpty(password)||TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
            enableButton();
            return;
        }

        mAuth2.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Mechanic Registered Successfully");
                    Mechanic mechanic = new Mechanic();
                    mechanic.setEmail(email);
                    mechanic.setName(name);
                    mechanic.setSpecialty(specialty);
                    mechanic.setUser_id(task.getResult().getUser().getUid());
                    mechanic.setPhone_number(phoneNumber);


                    mAuth2.signOut();
                    saveToDatabase(mechanic); // Add Mechanic information into mechanic's collection
                }
                else{
                    enableButton();
                    Toast.makeText(MechanicRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void saveToDatabase(final Mechanic mechanic) {
        DocumentReference mechanicRef = mDb.collection(getString(R.string.collection_mechanic))
                .document(mechanic.getUser_id());
        mechanicRef.set(mechanic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: Mechanic information added to database successfully");
                    addUserTypeToDatabase(mechanic.getUser_id());
                }
                else{
                    enableButton();
                    Toast.makeText(MechanicRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addUserTypeToDatabase(String user_id) {

        DocumentReference userTypeRef = mDb.collection(getString(R.string.collection_user_type))
                .document(user_id);

        Map<String,Integer> map = new HashMap<>();
        map.put("type", Constants.MECHANIC);
        userTypeRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    resetWidgets();
                    Log.d(TAG, "onComplete: user type added successfully");
                    Toast.makeText(MechanicRegisterActivity.this, "Mechanic Created successfully", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(MechanicRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                enableButton();
            }
        });
    }
    // resetting all the fields
    private void resetWidgets() {
        mNameInput.getEditText().setText("");
        mParentLayout.clearFocus();
        mEmailInput.getEditText().setText("");
        mPasswordInput.getEditText().setText("");
        mPhoneNumber.getEditText().setText("");
    }

    // stop user from continously hitting register button and show&hide progressbar
    public void disableButton() {
        mRegisterBtn.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void enableButton() {
        mRegisterBtn.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}
