package com.hfad.veloxmechanics.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Constants
    private static final String TAG = "DebugTag";

    //Widgets
    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;
    private ProgressBar mProgressBar;
    private CardView mLoginBtn;
    private TextView mSignupBtn;

    //Database Related
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    // Internal Storage
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        sp = getSharedPreferences(getString(R.string.cached_user_type), Activity.MODE_PRIVATE);

        // Check if user is already signed in and decide which page to open
        init();
        // initialize the widgets
        initWidgets();
    }

    private void initWidgets() {
        mEmailInput = findViewById(R.id.email);
        mPasswordInput = findViewById(R.id.password);
        mProgressBar = findViewById(R.id.progress_bar);

        mLoginBtn = findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(this);

        mSignupBtn = findViewById(R.id.signup_btn);
        mSignupBtn.setOnClickListener(this);
    }

    private void init() {

        int userType = sp.getInt(mAuth.getUid(), -1);

        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "init: User null");
            return;
        }
        // In Case User Type is not saved Locally Retrieve it from database and add it to shared preferences
        else if (userType == -1) {
            // Check Type of user. it can be admin , user or mechanic
            DocumentReference typeRef = mDb.collection(getString(R.string.collection_user_type))
                    .document(mAuth.getUid());
            typeRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Retrieved user type from online database");

                    int userType1 = task.getResult().get("type", Integer.class);
                    saveToSharedPrefrences(userType1);

                    goToMainPages(userType1);
                } else {
                    Log.d(TAG, "onComplete: " + task.getException().getMessage());
                }
                enableButton();
            });
        } else {
            Log.d(TAG, "init: retrieve user type from shared prefrences "+userType);
            goToMainPages(userType);
        }

    }

    private void saveToSharedPrefrences(int userType) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(mAuth.getUid(), userType);
        editor.apply();
    }

    // choose which page to go based on user type
    private void goToMainPages(int userType) {
        Intent intent = null;
        boolean deleted = false;
        switch (userType) {
            case Constants.ADMIN:
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                break;
            case Constants.MECHANIC:
                intent = new Intent(LoginActivity.this, MechanicMainActivity.class);
                break;
            case Constants.USER:
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
            case Constants.DELETED:
                intent = new Intent(LoginActivity.this, ErrorActivity.class);
                startActivity(intent);
                deleted = true;
                break;
        }

        if (intent != null && !deleted) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view.startAnimation(animation);

        switch (view.getId()) {
            case R.id.login_btn:
                disableButton();
                login();
                break;
            case R.id.signup_btn:
                goToSignupPage();
                break;

        }
    }

    private void goToSignupPage() {
        Intent intent = new Intent(LoginActivity.this, UserRegisterActivity.class);
        startActivity(intent);
    }

    private void login() {

        String email = mEmailInput.getEditText().getText().toString();
        String password = mPasswordInput.getEditText().getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill All the fields", Toast.LENGTH_SHORT).show();
            enableButton();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    init();
                } else {
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    enableButton();
                }
            }
        });

    }


    // stop user from continously hitting register button and show&hide progressbar
    public void disableButton() {
        mLoginBtn.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void enableButton() {
        mLoginBtn.setEnabled(true);
        mProgressBar.setVisibility(View.INVISIBLE);
    }
}
