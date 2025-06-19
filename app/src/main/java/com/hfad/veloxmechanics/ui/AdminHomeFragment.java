package com.hfad.veloxmechanics.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.MechanicAdapterForAdmin;
import com.hfad.veloxmechanics.model.ChatUser;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminHomeFragment extends Fragment implements MechanicAdapterForAdmin.MechanicCallback {

    //Constants
    private static final String TAG = "MyTag";

    // Database Related
    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    List<Mechanic> mMechanicList;
    MechanicAdapterForAdmin mMechanicAdapter;
    private RecyclerView mRecyclerView;

    public AdminHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        initWidgets(view);

        return view;
    }


    private void initWidgets(View view) {


        mRecyclerView = view.findViewById(R.id.recycler);

        mMechanicList = new ArrayList<>();
        mMechanicAdapter = new MechanicAdapterForAdmin(getActivity(), mMechanicList);
        mMechanicAdapter.setMechanicCallback(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mMechanicAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    private void retrieveMechanics() {
        mDb.collection(getString(R.string.collection_mechanic))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mMechanicList.addAll(queryDocumentSnapshots.toObjects(Mechanic.class));
                    mMechanicAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onCallBtnClicked(Mechanic mechanic) {
        Uri number = Uri.parse("tel:" + mechanic.getPhone_number());

        Intent intent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(intent);
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

            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(getString(R.string.chat_user), (Parcelable) chatUser);
            intent.putExtra(getString(R.string.current_user), (Parcelable) currentUser);
            intent.putExtra(getString(R.string.user_type),Constants.ADMIN);
            startActivity(intent);
        });

    }

    @Override
    public void onDeleteBtnClicked(Mechanic mechanic, View view) {
        showDialog(mechanic, view);
    }

    private void showDialog(Mechanic mechanic, View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Delete Mechanic")
                .setMessage("Are you sure you want to delete " + mechanic.getName() + " ?")
                .setPositiveButton("Yes", (dialogInterface, i) -> deleteMechanic(mechanic, view))
                .setNegativeButton("No", (dialogInterface, i) -> {
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMechanic(Mechanic mechanic, View view) {
        DocumentReference documentReference = mDb.collection(getString(R.string.collection_user_type))
                .document(mechanic.getUser_id());

        documentReference.update("type", Constants.DELETED).addOnSuccessListener(aVoid -> {

            Animation animation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
            animation.setDuration(300);
            view.startAnimation(animation);
            new Handler().postDelayed(() -> {
                mMechanicList.remove(mechanic);
                mMechanicAdapter.notifyDataSetChanged();
            }, animation.getDuration());

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveMechanics();

    }
}
