package com.android.parkplatz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class user_profile extends AppCompatActivity {

    TextView user;
    TextInputLayout name, phone, address;
    String user_name;
    Button update;
    TextInputEditText _phone,_address;
    EditText _name;
    RelativeLayout parkPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ImageView photo = findViewById(R.id.userphoto);
        user = findViewById(R.id.user_name);
        name = findViewById(R.id.username_field);
        phone = findViewById(R.id.phone_field);
        address = findViewById(R.id.address_field);
        update = findViewById(R.id.profile_update);
        _name = findViewById(R.id.name_text);
        _phone = findViewById(R.id.phone_text);
        _address = findViewById(R.id.address_text);
        parkPlaces = findViewById(R.id.parkPlaces);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String firebaseUser = firebaseAuth.getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users_data").child(firebaseUser);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String fullName = snapshot.child("full_name").getValue(String.class);
                    String phone_ = snapshot.child("phone").getValue(String.class);
                    String address_ = snapshot.child("address").getValue(String.class);

                    _name.setText(fullName);
                    _phone.setText(phone_);
                    _address.setText(address_);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null) {
            user_name = signInAccount.getDisplayName();
            user.setText(user_name);

            String pic = String.valueOf(signInAccount.getPhotoUrl());
            Picasso.get().load(pic).into(photo);
        }

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validatename() | !validatePhone() | !validateAddress()){
                    return;
                }

                String name_ = name.getEditText().getText().toString().trim();
                String phone_ = phone.getEditText().getText().toString().trim();
                String address_ = address.getEditText().getText().toString().trim();

                FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
                DatabaseReference reference = rootNode.getReference("Users_data").child(firebaseUser);


                reference.child("full_name").setValue(name_);
                reference.child("phone").setValue("+91"+phone_);
                reference.child("address").setValue(address_);

                Toast.makeText(user_profile.this,"Update Successfully",Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        parkPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(user_profile.this,your_parkings.class));
            }
        });
    }

    private boolean validatename(){
        String val = name.getEditText().getText().toString().trim();

        if (val.isEmpty()){
            name.setError("Field can not be empty");
            return false;
        }
        else {
            name.setError(null);
            name.setErrorEnabled(false);
            return  true;
        }
    }

    private boolean validatePhone(){
        String val = phone.getEditText().getText().toString().trim();

        if (val.isEmpty()){
            phone.setError("Field can not be empty");
            return false;
        }
        else {
            phone.setError(null);
            phone.setErrorEnabled(false);
            return  true;
        }
    }

    private boolean validateAddress(){
        String val = address.getEditText().getText().toString().trim();

        if (val.isEmpty()){
            address.setError("Field can not be empty");
            return false;
        }
        else {
            address.setError(null);
            address.setErrorEnabled(false);
            return  true;
        }
    }
}