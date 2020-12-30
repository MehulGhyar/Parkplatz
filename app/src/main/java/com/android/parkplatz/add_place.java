package com.android.parkplatz;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class add_place extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText title, address;
    Button  submit,browse;
    ImageView add_image;
    TextView lat , lang;
    DatabaseReference databaseReference;
    StorageReference reference;
    Uri  FilePathUri;
    int Image_Rquest_Code = 7;
    ProgressDialog progr;
    FirebaseStorage storage;
    StorageReference storageReference;
    String phone_;
    String firebaseUser;
    DatabaseReference references;
    TextView openTime, closeTime;
    int tHour , tMinute, cHour , cMinute;

    String openingTime, closingTime, parkingType;

    Spinner parkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        title = findViewById(R.id.place_name);
        address = findViewById(R.id.addresss);
        lat = findViewById(R.id.Lat);
        lang = findViewById(R.id.Lang);
        browse =findViewById(R.id.browse);
        parkType = findViewById(R.id.parkType);
        add_image = findViewById(R.id.add_place);
        submit = findViewById(R.id.submit);
        progr = new ProgressDialog(add_place.this);
        openTime = findViewById(R.id.openTime);
        closeTime = findViewById(R.id.closeTime);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        lat.setText(String.valueOf(getIntent().getDoubleExtra("lat",0.0)));
        lang.setText(String.valueOf(getIntent().getDoubleExtra("lang",0.0)));
        address.setText(getIntent().getStringExtra("address"));

        reference = FirebaseStorage.getInstance().getReference("Images");
        databaseReference = FirebaseDatabase.getInstance().getReference("location");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parkType.setAdapter(adapter);
        parkType.setOnItemSelectedListener(this);

        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Image"), Image_Rquest_Code);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser().getUid();

        references = FirebaseDatabase.getInstance().getReference("Users_data").child(firebaseUser);

        references.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                phone_ = snapshot.child("phone").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        openTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(add_place.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                tHour = hourOfDay;
                                tMinute = minute;

                                String time = tHour + ":" + tMinute;

                                SimpleDateFormat f24Hour = new SimpleDateFormat("HH:mm");
                                try {
                                    Date date = f24Hour.parse(time);
                                    SimpleDateFormat f12Hour = new SimpleDateFormat("hh:mm:aa");

                                    openingTime = f12Hour.format(date);
                                    openTime.setText(f12Hour.format(date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        },12,0,false);

                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                timePickerDialog.updateTime(tHour,tMinute);

                timePickerDialog.show();
            }
        });

        closeTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(add_place.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                cHour = hourOfDay;
                                cMinute = minute;

                                String time = cHour + ":" + cMinute;

                                SimpleDateFormat f24Hour = new SimpleDateFormat("HH:mm");
                                try {
                                    Date date = f24Hour.parse(time);
                                    SimpleDateFormat f12Hour = new SimpleDateFormat("hh:mm:aa");

                                    closingTime = f12Hour.format(date);
                                    closeTime.setText(f12Hour.format(date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        },12,0,false);

                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                timePickerDialog.updateTime(cHour,cMinute);

                timePickerDialog.show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Image_Rquest_Code && resultCode == RESULT_OK && data != null && data.getData() != null){
            FilePathUri = data.getData();

            try {
                add_image.setVisibility(View.VISIBLE);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),FilePathUri);
                add_image.setImageBitmap(bitmap);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }



    private void UploadImage() {
        if (FilePathUri != null){

         //   imageRef = storageReference.child("Images/"+System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));

            progr.setMax(100);
            progr.setTitle("Uploading....");
            progr.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progr.show();
            progr.setCancelable(false);

            StorageReference storageReference = reference.child("Images").child(System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));
            storageReference.putFile(FilePathUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            progr.dismiss();

                            String place = title.getText().toString();

                           // DecimalFormat numberFormat = new DecimalFormat("#.00000");

                            double lat = getIntent().getDoubleExtra("lat",0.0);
                            double lang = getIntent().getDoubleExtra("lang",0.0);
                            String add= address.getText().toString();

                            String time = openingTime + " - "+ closingTime;

                            Toast.makeText(getApplicationContext(), "Location Added Successfully",Toast.LENGTH_LONG).show();
                            @SuppressWarnings("VisibleForTests")

                            uploadinfo imageUploadInfo = new uploadinfo(place,lat,lang,add, taskSnapshot.getMetadata().getName(),phone_,time,parkingType);

                         databaseReference.child(place).setValue(imageUploadInfo);

                         references.child("My_places").child(place).setValue(phone_);

                        finish();

                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100*snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progr.incrementProgressBy((int) progress);
                }
            });
        }
        else {
            Toast.makeText(add_place.this, "Please Select Image or Add Image Name",Toast.LENGTH_LONG).show();
        }

    }

    private String  GetFileExtension(Uri filePathUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(filePathUri));
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parkingType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
