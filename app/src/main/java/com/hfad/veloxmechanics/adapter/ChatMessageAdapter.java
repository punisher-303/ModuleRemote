package com.hfad.veloxmechanics.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.ChatMessage;
import com.hfad.veloxmechanics.utility.ExtraMethods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DebugTag";
    
    private Context mContext;
    private ArrayList<ChatMessage> chatMessages;

    private String currentUserId;


    public interface ChatCallback{
        void onRateClicked(ChatMessage message, float rate);
    }

    private ChatCallback mChatCallback;

    public ChatMessageAdapter(Context context, ArrayList<ChatMessage> chatMessages) {
        this.mContext = context;
        this.chatMessages = chatMessages;
        mChatCallback = (ChatCallback) context;
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case Constants.MESSAGE_TEXT:
                view = LayoutInflater.from(mContext).inflate(R.layout.single_chat_message, parent, false);
                return new MessageViewHolder(view);
            case Constants.MESSAGE_IMAGE:
                view = LayoutInflater.from(mContext).inflate(R.layout.single_chat_image, parent, false);
                return new ImageViewHolder(view);
            default:
                view = LayoutInflater.from(mContext).inflate(R.layout.single_chat_rating, parent, false);
                return new RatingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case Constants.MESSAGE_TEXT:
                setMessage(holder, position);
                setMessageLayout(holder, position);
                break;
            case Constants.MESSAGE_IMAGE:
                setImage((ImageViewHolder) holder, position);
                setImageLayout(holder, position);
                break;
            case Constants.MESSAGE_RATE:
                setRate(holder,position);
                setRateLayout(holder,position);
        }

    }


    private void setRateLayout(RecyclerView.ViewHolder holder, int position) {

        if (chatMessages.get(position).getFrom().equals(currentUserId)) {
            // Set Layout to Right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((RatingViewHolder) (holder)).container.getLayoutParams();

            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ((RatingViewHolder) (holder)).container.setLayoutParams(params);
            ((RatingViewHolder) (holder)).container.setBackgroundResource(R.drawable.message_background);
            ((RatingViewHolder)holder).rateBtn.setEnabled(false);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((RatingViewHolder) (holder)).container.getLayoutParams();

            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ((RatingViewHolder) (holder)).container.setLayoutParams(params);
            ((RatingViewHolder) (holder)).container.setBackgroundResource(R.drawable.message_background_sender);
            ((RatingViewHolder)holder).rateBtn.setEnabled(true);
        }
    }

    private void setRate(RecyclerView.ViewHolder holder, int position) {
        Float rate = Float.parseFloat(chatMessages.get(position).getMessage());
        if(rate>=0){
            ((RatingViewHolder)holder).ratingBar.setRating(rate);
        }
    }

    private void setImage(@NonNull ImageViewHolder holder, int position) {
        Picasso
                .get()
                .load(chatMessages.get(position).getMessage())
                .placeholder(R.drawable.default_profile)
                .into(holder.image);
    }

    private void setMessage(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (chatMessages.get(position).getTime_stamp() != null) {
            Timestamp timestamp = new Timestamp(chatMessages.get(position).getTime_stamp());
            ((MessageViewHolder) (holder)).time_stamp.setText(ExtraMethods.getTimeOnly(timestamp));
        }

        ((MessageViewHolder) (holder)).messageText.setText(chatMessages.get(position).getMessage());
    }

    private void setImageLayout(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (chatMessages.get(position).getFrom().equals(currentUserId)) {
            // Set Layout to Right
            ((ImageViewHolder) (holder)).image.setBackgroundResource(R.drawable.image_background_sender);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((ImageViewHolder) (holder)).image.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ((ImageViewHolder) (holder)).image.setLayoutParams(params);

        } else {
            ((ImageViewHolder) (holder)).image.setBackgroundResource(R.drawable.image_background);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((ImageViewHolder) (holder)).image.getLayoutParams();
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ((ImageViewHolder) (holder)).image.setLayoutParams(params);

        }
    }

    private void setMessageLayout(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (chatMessages.get(position).getFrom().equals(currentUserId)) {
            // Set Layout to Right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((MessageViewHolder) (holder)).container.getLayoutParams();

            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ((MessageViewHolder) (holder)).container.setLayoutParams(params);

            ((MessageViewHolder) (holder)).container.setBackgroundResource(R.drawable.message_background);
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((MessageViewHolder) (holder)).container.getLayoutParams();

            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ((MessageViewHolder) (holder)).container.setLayoutParams(params);
            ((MessageViewHolder) (holder)).container.setBackgroundResource(R.drawable.message_background_sender);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getMessage_type();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, time_stamp;
        LinearLayout container;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message);
            time_stamp = itemView.findViewById(R.id.time_stamp);
            container = itemView.findViewById(R.id.container);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }

    public class RatingViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout container;
        RatingBar ratingBar;
        Button rateBtn;
        TextView time_stamp;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            this.container = itemView.findViewById(R.id.container);
            this.rateBtn = itemView.findViewById(R.id.rate_btn);
            this.ratingBar = itemView.findViewById(R.id.rating_bar);
            this.time_stamp = itemView.findViewById(R.id.time_stamp);
            this.rateBtn.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                float rate = ratingBar.getRating();
                mChatCallback.onRateClicked(chatMessages.get(pos),rate);
            });
        }
    }
}
