package com.hfad.veloxmechanics.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.GeoPoint;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.Mechanic;
import com.hfad.veloxmechanics.ui.MapActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MechanicAdapter extends RecyclerView.Adapter<MechanicAdapter.MyViewHolder> {

    private Context mContext;
    private List<Mechanic> mMechanicList;
    private GeoPoint mUserLocation = null;


    public interface MechanicListener {
        void onChatBtnClicked(Mechanic mechanic);
        void showMapPage();
        void onCallBtnClicked(Mechanic mechanic);
    }

    private MechanicListener mMechaniclistener;

    public void setmUserLocation(GeoPoint mUserLocation) {
        this.mUserLocation = mUserLocation;
    }

    public MechanicAdapter(Context context, List<Mechanic> list, MechanicListener mechanicListener) {
        mContext = context;
        mMechanicList = list;
        this.mMechaniclistener = mechanicListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_mechanic_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(mMechanicList.get(position).getName());

        String pic = mMechanicList.get(position).getImage();
        if (!pic.equals("default")) {
            Picasso picasso = Picasso.get();
            picasso.setIndicatorsEnabled(false);
            picasso.load(pic)
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profilePic)
            ;
        }
        holder.chatBtn.setOnClickListener(view -> {
            // Set Animation for the button
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            holder.chatBtn.startAnimation(animation);

            mMechaniclistener.onChatBtnClicked(mMechanicList.get(position));
        });
        holder.callBtn.setOnClickListener(view -> {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            holder.callBtn.startAnimation(animation);

            mMechaniclistener.onCallBtnClicked(mMechanicList.get(position));
        });
        holder.mapBtn.setOnClickListener(view->{
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            holder.mapBtn.startAnimation(animation);
            mMechaniclistener.showMapPage();
        });

        // Calculating the distance
            String distance = findDistance( mMechanicList.get(position).getLocation());
            holder.distance.setText(distance );

    }


    private String findDistance( GeoPoint mechanicLocation) {

        if(mUserLocation==null || mechanicLocation==null){
            return "Null";
        }

/*        float[] distance = new float[2];
        Location.distanceBetween(mUserLocation.getLatitude(),mUserLocation.getLongitude(),mechanicLocation.getLatitude(),mechanicLocation.getLongitude(),distance);
        return distance[0]+"";*/

        Location location1 = new Location("Location 1");
        location1.setLatitude(mUserLocation.getLatitude());
        location1.setLongitude(mUserLocation.getLongitude());

        Location location2 = new Location("Location 2");
        location2.setLatitude(mechanicLocation.getLatitude());
        location2.setLongitude(mechanicLocation.getLongitude());

        double distance = location1.distanceTo(location2);
        if(distance<1000){
            distance = (int)(distance*10) / 10.0;
            return distance + " m";
        }
        else{
            distance /= 1000.0;
            distance = (int)(distance*10) / 10.0;
            return distance + " km";

        }
    }

    @Override
    public int getItemCount() {
        return mMechanicList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePic;
        TextView name, distance;
        CircleImageView chatBtn, callBtn , mapBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic);
            name = itemView.findViewById(R.id.name);
            distance = itemView.findViewById(R.id.distance);
            chatBtn = itemView.findViewById(R.id.chat);
            callBtn = itemView.findViewById(R.id.call);
            mapBtn = itemView.findViewById(R.id.map);
        }
    }
}
