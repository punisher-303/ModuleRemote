package com.hfad.veloxmechanics.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity implements View.OnClickListener {

    // constants
    private static final String TAG = "UserRegisterActivity";

    // Widgets
    private TextInputLayout mNameInput;
    private TextInputLayout mEmailInput;
    private TextInputLayout mPasswordInput;
    private Button mRegisterBtn;
    private ProgressBar mProgressBar;

    //Database Related
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        initWidgets();
        initToolbar();

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // initializing TextInputLayouts and Button
    private void initWidgets() {
        mNameInput = findViewById(R.id.name);
        mEmailInput = findViewById(R.id.email);
        mPasswordInput = findViewById(R.id.password);

        mRegisterBtn = findViewById(R.id.register_btn);
        mRegisterBtn.setOnClickListener(this);

        mProgressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_btn:
                registerUser();
                break;
        }
    }

    private void registerUser() {

        disableButton();

        final String name = mNameInput.getEditText().getText().toString();
        final String email = mEmailInput.getEditText().getText().toString();
        final String password = mPasswordInput.getEditText().getText().toString();


        // Validate all the fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Sorry , You should fill all the fields", Toast.LENGTH_SHORT).show();
            enableButton();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UserRegisterActivity.this, "You have been registered Successfully", Toast.LENGTH_SHORT).show();
                    saveToDatabase(name, email);
                } else {
                    Toast.makeText(UserRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    enableButton();
                }
            }
        });

    }

    private void saveToDatabase(String name, String email) {

        String uid = mAuth.getUid();

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setUser_id(uid);

        DocumentReference userRef = mDb.collection(getString(R.string.collection_user))
                .document(uid);

        userRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    setUserType();
                    Log.d(TAG, "onComplete: Data Entered Successfully");
                } else {
                    Toast.makeText(UserRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    enableButton();
                }

            }
        });


    }

    private void setUserType() {

        DocumentReference typeRef = mDb.collection(getString(R.string.collection_user_type))
                .document(mAuth.getUid());

        Map<String, Integer> map = new HashMap<>();
        map.put("type", Constants.USER);

        typeRef.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                enableButton();
                if (task.isSuccessful()) {
                    goToLoginPage();
                } else {
                    Toast.makeText(UserRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void goToLoginPage() {
        Intent intent = new Intent(UserRegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
