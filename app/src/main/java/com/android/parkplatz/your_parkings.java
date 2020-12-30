package com.android.parkplatz;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class your_parkings extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> arrayList= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_parkings);

        recyclerView = findViewById(R.id.parkingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String firebaseUser = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users_data");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser).exists()){
                    DatabaseReference refer = FirebaseDatabase.getInstance().getReference("Users_data").child(firebaseUser).child("My_places");
                    refer.addListenerForSingleValueEvent(valueEventListener);
                }
                else {
                    startActivity(new Intent(your_parkings.this,user_profile.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    ValueEventListener valueEventListener= new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                String S = dataSnapshot.getKey();
                arrayList.add(S);
            }

            ParkingAdapter parkingAdapter = new ParkingAdapter(your_parkings.this,arrayList);
            recyclerView.setAdapter(parkingAdapter);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
}