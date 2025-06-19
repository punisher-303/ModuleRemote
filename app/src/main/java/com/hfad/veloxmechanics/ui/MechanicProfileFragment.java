package com.hfad.veloxmechanics.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.Mechanic;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import uk.co.mgbramwell.geofire.android.GeoFire;
import uk.co.mgbramwell.geofire.android.listeners.SetLocationListener;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MechanicProfileFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    // Constants
    private static final String TAG = "MechanicProfileFragment";
    private static final int CAMERA_BTN = 0;
    private static final int SWITCH_BTN = 1;
    private static final String EDIT_NAME = "Name";
    private static final String EDIT_EMAIL = "Email";
    private static final String EDIT_PHONE = "Phone Number";


    //widgets
    private Switch mAvailableSwitch;
    private ProgressBar mProgressBar;
    private ImageView mChangePictureBtn;
    private TextView mProfileName;
    private TextView mEmail;
    private TextView mPhoneNumber;
    private ImageView mEditPhone;
    private ImageView mEditEmail;
    private ImageView mEditName;
    private CircleImageView mProfilePic;
    private EditText tempLatInput, tempLongInput;
    // Database
    private FirebaseFirestore mDb;
    private StorageReference mStorageRef;

    // Variables
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;


    public MechanicProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mechanic_profile, container, false);


        mDb = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        initWidgets(view);
        initLocationListener();

        retrieveInitialData();


        return view;
    }

    private void retrieveInitialData() {
        mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d(TAG, "onEvent: " + e.getMessage());
                    return;
                }
                Mechanic mechanic = documentSnapshot.toObject(Mechanic.class);
                setInitWidgetValues(mechanic);

            }
        })
        ;

    }

    private void setInitWidgetValues(final Mechanic mechanic) {
        mProfileName.setText(mechanic.getName());
        mEmail.setText(mechanic.getEmail());
        mPhoneNumber.setText(mechanic.getPhone_number());

        if (!mechanic.getImage().equals("default")) {
            Picasso.get()
                    .load(mechanic.getImage())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_profile).into(mProfilePic, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: Retrieved Offline");
                }

                @Override
                public void onError(Exception e) {
                    Picasso.get()
                            .load(mechanic.getImage())
                            .placeholder(R.drawable.default_profile)
                            .into(mProfilePic);
                }
            });
        }
        if (mechanic.isOnline()) {
            mAvailableSwitch.setOnCheckedChangeListener(null);
            mAvailableSwitch.setChecked(true);
            mAvailableSwitch.setOnCheckedChangeListener(this);
        } else {
            mAvailableSwitch.setOnCheckedChangeListener(null);
            mAvailableSwitch.setChecked(false);
            mAvailableSwitch.setOnCheckedChangeListener(this);
        }
    }

    private void initLocationListener() {

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: called" + location.getLatitude() + " " + location.getLongitude());
                //if ((int) location.getAccuracy() > 50) {
                    mLocationManager.removeUpdates(mLocationListener);
                    saveMechanicLocationToDb(new GeoPoint(location.getLatitude(), location.getLongitude()));
                //}
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d(TAG, "onProviderDisabled: called");
                alertDialogNoGps();
            }
        };
    }

    public void initWidgets(View view) {
        mProgressBar = view.findViewById(R.id.progress_bar);

        mProfilePic = view.findViewById(R.id.profile_pic);

        mProfileName = view.findViewById(R.id.name);
        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phone);

        mEditEmail = view.findViewById(R.id.edit_email);
        mEditEmail.setOnClickListener(this);

        mEditPhone = view.findViewById(R.id.edit_phone);
        mEditPhone.setOnClickListener(this);

        mEditName = view.findViewById(R.id.edit_name);
        mEditName.setOnClickListener(this);

        // Temp EditText
        tempLatInput = view.findViewById(R.id.latitude);
        tempLongInput = view.findViewById(R.id.longitude);


        mChangePictureBtn = view.findViewById(R.id.change_picture);
        mChangePictureBtn.setOnClickListener(this);

        mAvailableSwitch = view.findViewById(R.id.available_switch);
        mAvailableSwitch.setOnCheckedChangeListener(this);
    }

    private void setMechanicNotAvailable() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(getString(R.string.field_mechanic_online), false);

        mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid()).update(map).addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Set Not Available", Toast.LENGTH_SHORT).show());
    }


    // Alert Dialog for asking to enable gps location
    private void alertDialogNoGps() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity())
                .setTitle("Enable Gps")
                .setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        Dialog dialog = alert.create();
        dialog.show();
    }


    @SuppressLint("MissingPermission")
    private void setMechanicAvailable() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        // Check for recent location and if it's been derived less than two minutes with accuracy bigger than 50
        if (location != null &&
                location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000 &&
                location.getAccuracy() > 30) {
            saveMechanicLocationToDb(new GeoPoint(location.getLatitude(), location.getLongitude()));

        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
    }

    private void saveMechanicLocationToDb(GeoPoint geoPoint) {


        // Just For Testing Purpose Adding a random number to location

        GeoPoint tempGeoPoint = getTempGeoPoint(geoPoint);

        HashMap<String, Object> map = new HashMap<>();
        map.put(getString(R.string.field_mechanic_location), tempGeoPoint);
        map.put(getString(R.string.field_mechanic_online), true);
        map.put(getString(R.string.field_mechanic_time_stamp), FieldValue.serverTimestamp());

        GeoFire geoFire = new GeoFire(mDb.collection(getString(R.string.collection_mechanic)));

        DocumentReference mechanicLocationRef = mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid());
        mechanicLocationRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    geoFire.setLocation(mechanicLocationRef.getId(), tempGeoPoint.getLatitude(), tempGeoPoint.getLongitude());
                    //
                    Toast.makeText(getActivity(), "Location Inserted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                enableButton(SWITCH_BTN);
            }
        });
    }

    private GeoPoint getTempGeoPoint(GeoPoint geoPoint) {

        String latStr = tempLatInput.getText().toString();
        String lngStr = tempLongInput.getText().toString();

        if (TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr))
            return geoPoint;

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);


        return new GeoPoint(lat, lng);
    /*        Random random = new Random();

        // Getting a random location around radius of 5 km
        double radiusInDegress = 5000/113000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegress * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        double new_x = x / Math.cos(Math.toRadians(geoPoint.getLongitude()));

        double foundLongitude = new_x + geoPoint.getLongitude();
        double foundLatitude = y + geoPoint.getLatitude();

        Log.d(TAG, "getTempGeoPoint: "+foundLatitude+" , "+foundLongitude);

        return new GeoPoint(foundLatitude,foundLongitude);*/
    }


    // stop user from continously hitting register button and show&hide progressbar
    public void disableButton(int btn) {
        switch (btn) {
            case CAMERA_BTN:
                mChangePictureBtn.setEnabled(false);
                break;
            case SWITCH_BTN:
                mAvailableSwitch.setEnabled(false);
                break;
        }
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void enableButton(int btn) {
        switch (btn) {
            case CAMERA_BTN:
                mChangePictureBtn.setEnabled(true);
                break;
            case SWITCH_BTN:
                mAvailableSwitch.setEnabled(true);
                break;
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        view.startAnimation(animation);

        switch (view.getId()) {
            case R.id.change_picture:
                startGalleryIntent();
                break;
            case R.id.edit_name:
                showEditDialog(EDIT_NAME);
                break;
            case R.id.edit_email:
                showEditDialog(EDIT_EMAIL);
                break;
            case R.id.edit_phone:
                showEditDialog(EDIT_PHONE);
                break;
        }
    }

    private void showEditDialog(String editText) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.mechanic_edit_dialog, null);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("New " + editText);
        alertDialog.setCancelable(false);

        final EditText editInput = view.findViewById(R.id.edit);
        editInput.setHint("Enter New " + editText);
        switch (editText) {
            case EDIT_EMAIL:
                editInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case EDIT_PHONE:
                editInput.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
        }


        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Edit", (dialogInterface, i) -> {
            String changedText = editInput.getText().toString().trim();
            switch (editText) {
                case EDIT_NAME:
                    editMechanicName(changedText);
                    break;
                case EDIT_EMAIL:
                    reauthenticationDialog(changedText);
                    break;
                case EDIT_PHONE:
                    editMechanicPhone(changedText);
                    break;
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> alertDialog.dismiss());
        alertDialog.setView(view);
        alertDialog.show();
    }

    // ReAuthenticate User in order to change email
    private void reauthenticationDialog(String changedText) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.mechanic_reauthenticate, null);
        EditText emailInput = view.findViewById(R.id.email);
        EditText passwordInput = view.findViewById(R.id.password);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Please Login Again");

        alertDialog.setView(view);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Login", (dialogInterface, i) -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                updateEmailAuthentication(email, password, changedText);
            }
        });
        alertDialog.show();

    }

    private void updateEmailAuthentication(String email, String password, String newEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updateEmail(newEmail).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(getActivity(), "Email Updated Successfully", Toast.LENGTH_SHORT).show();
                        editMechanicEmail(newEmail);
                    } else {
                        Toast.makeText(getActivity(), task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void editMechanicEmail(String newEmail) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(getString(R.string.field_mechanic_email), newEmail);

        mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid()).update(map).addOnSuccessListener(aVoid -> mEmail.setText(newEmail));

    }

    private void editMechanicPhone(String changedText) {


        HashMap<String, Object> map = new HashMap<>();
        map.put(getString(R.string.field_mechanic_phone_number), changedText);

        mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid()).update(map).addOnSuccessListener(aVoid -> mPhoneNumber.setText(changedText));
    }

    private void editMechanicName(String changedText) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(getString(R.string.field_mechanic_name), changedText);

        mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid()).update(map).addOnSuccessListener(aVoid -> mProfileName.setText(changedText));
    }

    private void startGalleryIntent() {

        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(getContext(), this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                disableButton(CAMERA_BTN);
                addImageToDb(compressImage(resultUri));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void addImageToDb(byte[] compressImage) {
        final StorageReference filePath = mStorageRef.child(getString(R.string.collection_profile_pic)).child(FirebaseAuth.getInstance().getUid() + ".jpg");
        filePath.putBytes(compressImage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addDownloadUrlToDb(filePath);
            }
        });

    }

    private void addDownloadUrlToDb(StorageReference filePath) {
        filePath.getDownloadUrl().addOnSuccessListener(uri -> mDb.collection(getString(R.string.collection_mechanic))
                .document(FirebaseAuth.getInstance().getUid())
                .update("image", uri.toString()).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Image Added Successfully", Toast.LENGTH_SHORT).show();
                    enableButton(CAMERA_BTN);
                }));
    }


    private byte[] compressImage(Uri resultUri) {

        byte[] thumb_byte = null;


        File thumbFilePath = new File(resultUri.getPath());
        try {
            Bitmap thumbBitmap = new Compressor(getActivity())
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(thumbFilePath);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            thumb_byte = baos.toByteArray();


        } catch (IOException e) {

        }
        return thumb_byte;
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            disableButton(SWITCH_BTN);
            setMechanicAvailable();
        } else {
            setMechanicNotAvailable();
        }
    }
}
