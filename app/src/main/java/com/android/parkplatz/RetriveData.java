package com.android.parkplatz;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.parkplatz.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RetriveData extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<String> arrayList;
    private UserAdapter userAdapter;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrive_data);

        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        arrayList = new ArrayList<String>();

        GoogleSignInAccount signInAccount= GoogleSignIn.getLastSignedInAccount(this);
        user = signInAccount.getDisplayName();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userInfo").child(user);
        reference.addListenerForSingleValueEvent(valueEventListener);

        Toast.makeText(this,"Please wait",Toast.LENGTH_SHORT).show();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                String S = dataSnapshot1.getKey();
                arrayList.add(S);
            }

            userAdapter = new UserAdapter(RetriveData.this,arrayList);
            recyclerView.setAdapter(userAdapter);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    String title = null;
    private ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            final int position = viewHolder.getAdapterPosition();

            if (direction == ItemTouchHelper.LEFT){

                title = arrayList.get(position);
                arrayList.remove(position);
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("userInfo").child(user).child(title);
                reference.removeValue();
                userAdapter.notifyItemRemoved(position);

                Snackbar.make(recyclerView, title , Snackbar.LENGTH_LONG ).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reference.child(title).setValue(title);
                        arrayList.add(position, title);
                        recreate();
                        userAdapter.notifyItemInserted(position);
                    }
                }).show();

            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    };

}
