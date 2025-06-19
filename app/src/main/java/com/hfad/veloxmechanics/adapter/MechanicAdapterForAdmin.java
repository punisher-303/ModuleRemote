package com.hfad.veloxmechanics.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.GeoPoint;
import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.Mechanic;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MechanicAdapterForAdmin extends RecyclerView.Adapter<MechanicAdapterForAdmin.MyViewHolder> implements Filterable {

    private static final String TAG = "MyTag";

    Context mContext;
    List<Mechanic> mMechanicList;
    List<Mechanic> mMechanicListFull;

    public MechanicAdapterForAdmin(Context context, List<Mechanic> mechanicList) {
        mContext = context;
        mMechanicList = mechanicList;
        mMechanicListFull = new ArrayList<>(mechanicList);
    }

    public void setMechanicCallback(MechanicCallback mMechanicCallback) {
        this.mMechanicCallback = mMechanicCallback;
    }

    public interface MechanicCallback {
        void onCallBtnClicked(Mechanic mechanic);

        void onChatBtnClicked(Mechanic mechanic);

        void onDeleteBtnClicked(Mechanic mechanic , View view);
    }

    private MechanicCallback mMechanicCallback;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_mechanic_for_admin_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(mMechanicList.get(position).getName());

        String city = getCity(position);
        holder.city.setText(city);

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

            mMechanicCallback.onChatBtnClicked(mMechanicList.get(position));
        });
        holder.callBtn.setOnClickListener(view -> {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            holder.callBtn.startAnimation(animation);

            mMechanicCallback.onCallBtnClicked(mMechanicList.get(position));
        });
        holder.deleteBtn.setOnClickListener(view -> {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            holder.deleteBtn.startAnimation(animation);
            mMechanicCallback.onDeleteBtnClicked(mMechanicList.get(position),holder.container);
        });


    }

    private String getCity(int position) {
        GeoPoint location = mMechanicList.get(position).getLocation();
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
                if (addresses.size() > 0)
                    return addresses.get(0).getLocality();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "null";
    }

    @Override
    public int getItemCount() {
        return mMechanicList.size();
    }


    @Override
    public Filter getFilter() {
        return mMechanicFilter;
    }

    private Filter mMechanicFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Mechanic> filteredList = new ArrayList<>();
            if (charSequence==null || charSequence.length()==0) {
                Log.d(TAG, "performFiltering: null text");
                filteredList.addAll(mMechanicListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                Log.d(TAG, "performFiltering: filter pattern"+filterPattern);
                for (Mechanic m : mMechanicListFull) {
                    if (m.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(m);
                        Log.d(TAG, "performFiltering: added to filter list"+m.getName());
                    }
                }
                mMechanicList = filteredList;
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mMechanicList.clear();
            mMechanicList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView container;
        ImageView profilePic;
        TextView name, city, rating;
        CircleImageView callBtn, chatBtn, deleteBtn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            profilePic = itemView.findViewById(R.id.profile_pic);
            name = itemView.findViewById(R.id.name);
            city = itemView.findViewById(R.id.city);
            callBtn = itemView.findViewById(R.id.call);
            chatBtn = itemView.findViewById(R.id.chat);
            deleteBtn = itemView.findViewById(R.id.delete);
            rating = itemView.findViewById(R.id.rating);
        }
    }
}
