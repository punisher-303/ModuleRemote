package com.hfad.veloxmechanics.ui;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.ActiveChatAdapter;
import com.hfad.veloxmechanics.model.ActiveChat;
import com.hfad.veloxmechanics.model.ChatUser;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.model.User;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ActiveChatAdapter.OnChatClickCallback {
    //Constants
    private static final String TAG = "MyTag";

    //Widgets
    private RecyclerView mRecyclerView;

    // Database Related
    private FirebaseFirestore mDb;
    private String mCurrentUserId;
    //Variables
    private ArrayList<ActiveChat> mActiveChats = new ArrayList<>();
    private ActiveChatAdapter mChatAdapter;
    private ChatUser mCurrentUser = new ChatUser();
    private ListenerRegistration mActiveChatListener;
    private int mUserType = -1;
    // Internal Storage
    SharedPreferences sp;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        mDb = FirebaseFirestore.getInstance();
        mCurrentUserId = FirebaseAuth.getInstance().getUid();
        sp = getActivity().getSharedPreferences(getString(R.string.cached_user_type), Activity.MODE_PRIVATE);

        initChatUser();// Get user Data based on user_type which is saved in sharedPreferences


        initWidgets(view);

        return view;
    }

    private void initChatUser() {
        int userType = sp.getInt(FirebaseAuth.getInstance().getUid(), -1);
        mUserType = userType;
        switch (userType) {
            case Constants.MECHANIC:
                retrieveAsMechanic();
                break;
            default:
                retrieveAsUserOrAdmin();
        }
    }

    private void retrieveAsUserOrAdmin() {
        DocumentReference reference = mDb.collection(getString(R.string.collection_user))
                .document(mCurrentUserId);
        reference.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            mCurrentUser.setName(user.getName());
            mCurrentUser.setUser_id(user.getUser_id());
            mCurrentUser.setImage("default");
        });
    }

    private void retrieveAsMechanic() {
        DocumentReference reference = mDb.collection(getString(R.string.collection_mechanic))
                .document(mCurrentUserId);
        reference.get().addOnSuccessListener(documentSnapshot -> {
            Mechanic mechanic = documentSnapshot.toObject(Mechanic.class);
            mCurrentUser.setName(mechanic.getName());
            mCurrentUser.setImage(mechanic.getImage());
            mCurrentUser.setUser_id(mechanic.getUser_id());
        });
    }

    private void retrieveActiveChats() {
        CollectionReference reference = mDb.collection(getString(R.string.collection_active_chats))
                .document(mCurrentUserId)
                .collection(getString(R.string.sub_collection_active_chat));
        mActiveChatListener = reference
                .orderBy("time_stamp",Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Active Chat Chat Fragment onEvent: ", e);
                        return;
                    }
                    mActiveChats.clear();
                    if (queryDocumentSnapshots != null) {
                        mActiveChats.addAll(queryDocumentSnapshots.toObjects(ActiveChat.class));
                        mChatAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void initWidgets(View view) {

        mChatAdapter = new ActiveChatAdapter(getActivity(), mActiveChats);
        mChatAdapter.setListener(this);

        mRecyclerView = view.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mChatAdapter);

    }

    @Override
    public void onChatClicked(ActiveChat activeChat) {
        ChatUser chatUser = new ChatUser();
        chatUser.setUser_id(activeChat.getUser_id());
        chatUser.setName(activeChat.getUser_name());
        chatUser.setImage(activeChat.getProfile_pic());

        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(getString(R.string.chat_user), (Parcelable) chatUser);
        intent.putExtra(getString(R.string.current_user), (Parcelable) mCurrentUser);
        intent.putExtra(getString(R.string.user_type),mUserType);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentUserId != null) {
            retrieveActiveChats();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActiveChatListener != null) {
            mActiveChatListener.remove();
        }
    }
}
