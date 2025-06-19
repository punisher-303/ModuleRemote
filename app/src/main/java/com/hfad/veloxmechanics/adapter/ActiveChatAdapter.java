package com.hfad.veloxmechanics.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.hfad.veloxmechanics.Constants;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.ActiveChat;
import com.hfad.veloxmechanics.utility.ExtraMethods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActiveChatAdapter extends RecyclerView.Adapter<ActiveChatAdapter.MyViewHolder> {

    public interface OnChatClickCallback {
        void onChatClicked(ActiveChat activeChat);
    }

    private OnChatClickCallback listener;
    private Context mContext;
    private ArrayList<ActiveChat> mActiveChats;

    public ActiveChatAdapter(Context context, ArrayList<ActiveChat> activeChats) {
        mContext = context;
        mActiveChats = activeChats;
    }

    public void setListener(OnChatClickCallback listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_active_chat, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.profile_name.setText(mActiveChats.get(position).getUser_name());

        // Set Time
        if (mActiveChats.get(position).getTime_stamp() != null) {
            Timestamp timestamp = new Timestamp(mActiveChats.get(position).getTime_stamp());
            String time_past = ExtraMethods.getTimeAgo(timestamp.getSeconds());
            holder.time_stamp.setText(time_past);
        }

        switch (mActiveChats.get(position).getLast_message_type()) {
            case Constants
                    .MESSAGE_TEXT:
                holder.message.setText(mActiveChats.get(position).getLast_message());
                holder.message.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                break;
            case Constants.MESSAGE_IMAGE:
                holder.message.setText("Photo");
                holder.message.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo, 0, 0, 0);
        }

        if (!mActiveChats.get(position).getSeen()) {
            holder.new_message_bubble.setVisibility(View.VISIBLE);
            holder.time_stamp.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            holder.new_message_bubble.setVisibility(View.INVISIBLE);
            holder.time_stamp.setTextColor(Color.parseColor("#808080"));
        }
        setImage(holder, position);

        holder.container.setOnClickListener(view -> {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            view.startAnimation(animation);
            listener.onChatClicked(mActiveChats.get(position));
        });
    }

    private void setImage(MyViewHolder holder, int position) {
        if (!mActiveChats.get(position).getProfile_pic().equals("default")) {
            Picasso
                    .get()
                    .load(mActiveChats.get(position).getProfile_pic())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profile_pic);
        }
    }

    @Override
    public int getItemCount() {
        return mActiveChats.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout container;
        CircleImageView profile_pic, new_message_bubble;
        TextView time_stamp, profile_name, message;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            profile_pic = itemView.findViewById(R.id.profile_pic);
            profile_name = itemView.findViewById(R.id.profile_name);
            time_stamp = itemView.findViewById(R.id.time_stamp);
            message = itemView.findViewById(R.id.message);
            new_message_bubble = itemView.findViewById(R.id.new_message);
        }
    }
}
