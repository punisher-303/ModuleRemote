package com.hfad.veloxmechanics.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.adapter.ChatMessageAdapter;
import com.hfad.veloxmechanics.model.ActiveChat;
import com.hfad.veloxmechanics.model.ChatMessage;
import com.hfad.veloxmechanics.model.ChatUser;
import com.hfad.veloxmechanics.utility.ExtraMethods;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener ,ChatMessageAdapter.ChatCallback{

    //Constants
    private static final String TAG = "DebugTag";

    //Widgets
    private TextView toolbarDisplayname;
    private CircleImageView toolbarImage;
    private ImageView mImageBtn;
    private ImageView mCheckMarkBtn;
    private EditText mMessageInput;
    private RecyclerView mRecyclerView;
    private ImageView mFunctionBtn;

    //Database Related
    private FirebaseFirestore mDb;
    private StorageReference mStorageRef;

    // Variables
    private ChatUser mChatUser;
    private ChatUser mCurrentUser;
    private ChatMessageAdapter mMessageAdapter;
    private ArrayList<ChatMessage> mChatMessages = new ArrayList<>();
    private ListenerRegistration mChatEventListener;
    private ListenerRegistration mActiveChatListener;
    private Integer mUserType = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mDb = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        getIncomingIntent();

        // if A chat is opened set unseen to seen when a new message comes in
        initActiveChatsListener();
        initToolbar();
        initWidgets();
        initChatRecycler();

    }

    private void initActiveChatsListener() {
        DocumentReference reference = mDb.collection(getString(R.string.collection_active_chats))
                .document(mCurrentUser.getUser_id())
                .collection(getString(R.string.sub_collection_active_chat))
                .document(mChatUser.getUser_id());
        mActiveChatListener = reference.addSnapshotListener((documentSnapshot, e) -> {
            reference.update("seen", true);
            Log.d(TAG, "initActiveChatsListener: Called");
        });
    }

    private void initChatRecycler() {

        mMessageAdapter = new ChatMessageAdapter(this, mChatMessages);
        mRecyclerView.setAdapter(mMessageAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mChatMessages.size() > 0) {
                                mRecyclerView.smoothScrollToPosition(
                                        mRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });
    }

    private void retrieveChatMessages() {
        CollectionReference reference = mDb.collection(getString(R.string.collection_messages))
                .document(mCurrentUser.getUser_id())
                .collection(mChatUser.getUser_id());

        mChatEventListener = reference.orderBy("time_stamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "RetrieveChatMessages onEvent: ", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        mChatMessages.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            ChatMessage chatMessage = doc.toObject(ChatMessage.class);
                            mChatMessages.add(chatMessage);
                            mRecyclerView.smoothScrollToPosition(mChatMessages.size() - 1);
                        }
                        mMessageAdapter.notifyDataSetChanged();
                    }
                });

    }

    private void initWidgets() {

        mImageBtn = findViewById(R.id.image_btn);
        mImageBtn.setOnClickListener(this);

        mCheckMarkBtn = findViewById(R.id.checkmark);
        mCheckMarkBtn.setOnClickListener(this);

        mMessageInput = findViewById(R.id.input_message);

        mRecyclerView = findViewById(R.id.recycler);

        mFunctionBtn = findViewById(R.id.function_btn);
        mFunctionBtn.setOnClickListener(this);
        // Change Button Based on the user
        // Location For User . Rating For Mechanic
        switch (mUserType) {
            case Constants
                    .ADMIN:
                mFunctionBtn.setVisibility(View.INVISIBLE);
                mFunctionBtn.setEnabled(false);
                break;
            case Constants.MECHANIC:
                mFunctionBtn.setImageResource(R.drawable.ic_star);
        }
    }

    private void getIncomingIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            Toast.makeText(this, "Sorry An Error Occurred", Toast.LENGTH_SHORT).show();
            finish();
        }

        mChatUser = (ChatUser) bundle.get(getString(R.string.chat_user));
        mCurrentUser = (ChatUser) bundle.get(getString(R.string.current_user));
        mUserType = (Integer) bundle.get(getString(R.string.user_type));
        Log.d(TAG, "getIncomingIntent: " + mUserType);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View action_bar_view = LayoutInflater.from(this).inflate(R.layout.chat_custom_bar, null);
        getSupportActionBar().setCustomView(action_bar_view);

        toolbarDisplayname = findViewById(R.id.chat_display_name);
        toolbarImage = findViewById(R.id.custom_bar_profile);

        toolbarDisplayname.setText(mChatUser.getName());

        if (!mChatUser.getImage().equals("default")) {
            Picasso
                    .get()
                    .load(mChatUser.getImage())
                    .placeholder(R.drawable.default_profile)
                    .into(toolbarImage);
        }
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

    @Override
    public void onClick(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        view.startAnimation(animation);
        switch (view.getId()) {
            case R.id.image_btn:
                startGalleryIntent();
                break;
            case R.id.checkmark:
                addMessage();
                break;
            case R.id.function_btn:
                switch (mUserType) {
                    case Constants.USER:
                        Toast.makeText(this, "User Clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case Constants.MECHANIC:
                        showRatingDialog();
                        break;
                }
                break;
        }
    }

    private void addMessage() {
        String message = mMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please Write Something", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFrom(mCurrentUser.getUser_id());
        chatMessage.setMessage(message);
        chatMessage.setMessage_type(Constants.MESSAGE_TEXT);

        // Save the message for both user and receiver in database
        saveMessageToDb(chatMessage);

        clearTextInput();
    }

    private void clearTextInput() {
        mMessageInput.setText("");
    }


    private void saveMessageToDb(ChatMessage chatMessage) {

        DocumentReference documentReference = mDb.collection(getString(R.string.collection_messages))
                .document(mCurrentUser.getUser_id())
                .collection(mChatUser.getUser_id())
                .document();
        chatMessage.setMessage_id(documentReference.getId());
        documentReference.set(chatMessage).addOnSuccessListener(aVoid -> {
            // Save message for receiver
            saveMessageForReceiverToDb(chatMessage);
        });

    }

    private void saveMessageForReceiverToDb(ChatMessage chatMessage) {
        DocumentReference documentReference = mDb.collection(getString(R.string.collection_messages))
                .document(mChatUser.getUser_id())
                .collection(mCurrentUser.getUser_id())
                .document(chatMessage.getMessage_id());
        documentReference.set(chatMessage).addOnSuccessListener(aVoid -> {
            setActiveChatForSender(chatMessage);
        });

    }

    private void setActiveChatForSender(ChatMessage chatMessage) {
        ActiveChat activeChat = new ActiveChat();
        activeChat.setLast_message(chatMessage.getMessage());
        activeChat.setLast_message_type(chatMessage.getMessage_type());
        activeChat.setUser_id(mChatUser.getUser_id());
        activeChat.setUser_name(mChatUser.getName());
        activeChat.setProfile_pic(mChatUser.getImage());
        activeChat.setSeen(true);

        DocumentReference documentReference = mDb.collection(getString(R.string.collection_active_chats))
                .document(mCurrentUser.getUser_id())
                .collection(getString(R.string.sub_collection_active_chat))
                .document(mChatUser.getUser_id());
        documentReference.set(activeChat).addOnSuccessListener(aVoid -> setActiveChatForReceiver(chatMessage));

    }

    private void setActiveChatForReceiver(ChatMessage chatMessage) {
        ActiveChat activeChat = new ActiveChat();
        activeChat.setLast_message(chatMessage.getMessage());
        activeChat.setLast_message_type(chatMessage.getMessage_type());
        activeChat.setUser_id(mCurrentUser.getUser_id());
        activeChat.setUser_name(mCurrentUser.getName());
        activeChat.setProfile_pic(mCurrentUser.getImage());
        activeChat.setSeen(false);

        DocumentReference documentReference = mDb.collection(getString(R.string.collection_active_chats))
                .document(mChatUser.getUser_id())
                .collection(getString(R.string.sub_collection_active_chat))
                .document(mCurrentUser.getUser_id());
        documentReference.set(activeChat).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Active Chat added successfully"));
    }


    @Override
    protected void onResume() {
        super.onResume();
        retrieveChatMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatEventListener != null) {
            mChatEventListener.remove();
        }
        if (mActiveChatListener != null) {
            mActiveChatListener.remove();
        }
    }

    private void startGalleryIntent() {

        CropImage.activity()
                .start(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                addImageToDb(ExtraMethods.compressImage(this, resultUri));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    private void addImageToDb(byte[] compressImage) {

        DocumentReference reference = mDb.collection(getString(R.string.collection_messages))
                .document(mCurrentUser.getUser_id())
                .collection(mChatUser.getUser_id())
                .document();
        String key = reference.getId();

        final StorageReference filePath = mStorageRef.child(getString(R.string.collection_chat_images)).child(key + ".jpg");
        filePath.putBytes(compressImage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                addDownloadUrlToDb(filePath, reference);
            }
        });

    }

    private void addDownloadUrlToDb(StorageReference filePath, DocumentReference reference) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage_type(Constants.MESSAGE_IMAGE);
        chatMessage.setFrom(mCurrentUser.getUser_id());
        chatMessage.setMessage_id(reference.getId());

        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
            chatMessage.setMessage(uri.toString());
            reference.set(chatMessage).addOnSuccessListener(aVoid -> saveImageForReceiver(chatMessage));
        });
    }

    private void saveImageForReceiver(ChatMessage chatMessage) {
        DocumentReference reference = mDb.collection(getString(R.string.collection_messages))
                .document(mChatUser.getUser_id())
                .collection(mCurrentUser.getUser_id())
                .document(chatMessage.getMessage_id());
        reference.set(chatMessage).addOnSuccessListener(aVoid -> {
            setActiveChatForSender(chatMessage);
        });
    }

    private void addRatingMessage() {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setFrom(mCurrentUser.getUser_id());
        chatMessage.setMessage("-1");
        chatMessage.setMessage_type(Constants.MESSAGE_RATE);
        saveMessageToDb(chatMessage);
    }

    private void showRatingDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Add Rating")
                .setMessage("Are You sure You want to ask for rating? ")
                .setPositiveButton("Yes", (dialogInterface, i) -> addRatingMessage())
                .setNegativeButton("No", ((dialogInterface, i) -> {
                }));
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onRateClicked(ChatMessage message, float rate) {

    }
}
