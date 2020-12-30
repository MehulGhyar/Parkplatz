package com.android.parkplatz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<String> arrayList = new ArrayList<>();

    public UserAdapter(Context c, ArrayList<String> arrayList){
        this.context = c;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        String s = this.arrayList.get(position);
        holder.title.setText(arrayList.get(position));

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("location").child(s);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String address = dataSnapshot.child("address").getValue(String.class);
                holder.address.setText(address);
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

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView title,address,time,type;
        CardView cardView;
        public MyViewHolder(@Nullable View view) {
            super(view);

            address = view.findViewById(R.id.address);
            title = view.findViewById(R.id.title);
            time = view.findViewById(R.id.saveTime);
            type = view.findViewById(R.id.saveType);
            cardView = view.findViewById(R.id.cardView);
        }

    }


}
