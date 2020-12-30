package com.android.parkplatz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> arrayList = new ArrayList<>();

    public ParkingAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_park_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String s = this.arrayList.get(position);
        holder.name.setText(arrayList.get(position));

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("location").child(s);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address_ = dataSnapshot.child("address").getValue(String.class);

                holder.address.setText(address_);
                holder.time.setText(dataSnapshot.child("time").getValue(String.class));
                holder.type.setText(dataSnapshot.child("type").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, address, time , type;
        ImageView placeImage, edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.parkingName);
            address = itemView.findViewById(R.id.parkingAddress);
            time = itemView.findViewById(R.id.parkingTime);
            type = itemView.findViewById(R.id.parkingType);
            placeImage = itemView.findViewById(R.id.parkingImage);
            edit = itemView.findViewById(R.id.parkingEdit);
        }
    }
}
