package com.android.parkplatz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    public GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;

    private View mapView;

    private final float DEFAULT_ZOOM = 14;

    private Button info;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    DatabaseReference reference;
    LinearLayout linearLayout;
    Button arrow;
    public String user;

    double lat1, long1, lat2, long2;
    public SearchView search;

    Button menubtn;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        info = findViewById(R.id.btn_info);
        menubtn = findViewById(R.id.menu);

        search = findViewById(R.id.input_search);

        info.setVisibility(View.INVISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();


        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        final TextView name = headerView.findViewById(R.id.username__);

        TextView mail = headerView.findViewById(R.id.useremail);
        ImageView photo = headerView.findViewById(R.id.userphoto);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        reference = FirebaseDatabase.getInstance().getReference("location");
        reference.addListenerForSingleValueEvent(valueEventListener);

        navigationView.setNavigationItemSelectedListener(this);

        menubtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null) {
            user = signInAccount.getDisplayName();
            name.setText(String.format("Hi,%s", signInAccount.getDisplayName()));
            mail.setText(signInAccount.getEmail());

            String pic = String.valueOf(signInAccount.getPhotoUrl());

            Picasso.get().load(pic).into(photo);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.internet_dialog);

            dialog.setCanceledOnTouchOutside(false);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

            Button tryagain = dialog.findViewById(R.id.try_again);

            tryagain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });

            dialog.show();
        }


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = search.getQuery().toString();

                List<Address> addressList = null;
                if (location != null && !location.equals("")) {

                    Geocoder geocoder = new Geocoder(MapActivity.this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address address = addressList.get(0);

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }

        });


    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile: startActivity(new Intent(MapActivity.this,user_profile.class));
                break;
            case R.id.your_places:
                Intent i = new Intent(getApplicationContext(), RetriveData.class);
                startActivity(i);
                break;

            case R.id.parking_place:
                startActivity(new Intent(MapActivity.this,your_parkings.class));
                break;
            case R.id.simple:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.sattelite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                break;
            case R.id.share:
                break;
            case R.id.feedback:
                Intent add_place = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "mehulghyar321@gmail.com"));
                startActivity(add_place);
                break;
            case R.id.aboutUs:
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.about_us);

                dialog.setCanceledOnTouchOutside(false);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

                dialog.show();
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                LatLng latLng = new LatLng((double) dataSnapshot1.child("lat").getValue(), (double) dataSnapshot1.child("lang").getValue());
                mMap.addMarker(new MarkerOptions().position(latLng).title(dataSnapshot1.child("title").getValue().toString()));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                info.setVisibility(View.INVISIBLE);
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                String userId = firebaseAuth.getCurrentUser().getUid();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users_data");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(userId).exists()){
                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                final String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                                final Dialog dial= new Dialog(MapActivity.this);
                                dial.setContentView(R.layout.custom_confirm_address);

                                dial.setCanceledOnTouchOutside(true);
                                dial.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                                dial.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dial.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

                                Button select = dial.findViewById(R.id.Select);
                                Button change = dial.findViewById(R.id.Change);
                                TextView Lat = dial.findViewById(R.id.Lat);
                                TextView Lang = dial.findViewById(R.id.Lang);
                                TextView add = dial.findViewById(R.id.myAddress);

                                select.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MapActivity.this, add_place.class);

                                        intent.putExtra("lat",latLng.latitude);
                                        intent.putExtra("lang",latLng.longitude);
                                        intent.putExtra("address",address);

                                        dial.dismiss();
                                        startActivity(intent);
                                    }
                                });

                                change.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        dial.dismiss();
                                    }
                                });
                                Lat.setText(String.valueOf(latLng.latitude));
                                Lang.setText(String.valueOf(latLng.longitude));
                                add.setText(address);

                                dial.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            startActivity(new Intent(MapActivity.this,user_profile.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                    info.setVisibility(View.VISIBLE);
                   info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                                    MapActivity.this, R.style.BottomSheetDialogTheme);
                            final View bottomSheetView = LayoutInflater.from(getApplicationContext())
                                    .inflate(R.layout.layout_bottom_motorcycles_stand,(LinearLayout) findViewById(R.id.bottomSheetContainer));

                            arrow= bottomSheetView.findViewById(R.id.expand);
                            linearLayout = bottomSheetView.findViewById(R.id.expandable);

                            TextView textView= bottomSheetView.findViewById(R.id.title);
                            final TextView add = bottomSheetView.findViewById(R.id.address);
                            ImageView imageView = bottomSheetView.findViewById(R.id.loc_image);
                            final TextView dist = bottomSheetView.findViewById(R.id.distance);
                            final ImageButton save = bottomSheetView.findViewById(R.id.save);
                            TextView phone = bottomSheetView.findViewById(R.id.phone);
                            TextView time = bottomSheetView.findViewById(R.id.time);
                            TextView type = bottomSheetView.findViewById(R.id.type);

                            textView.setText(marker.getTitle());

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("location").child(marker.getTitle());
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    add.setText(dataSnapshot.child("address").getValue(String.class));
                                    String phone_ = dataSnapshot.child("phone").getValue(String.class);
                                    phone.setText(phone_);
                                    time.setText(dataSnapshot.child("time").getValue(String.class));
                                    type.setText(dataSnapshot.child("type").getValue(String.class));

                                    if (dataSnapshot.child("image").exists()){

                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference storageReference = storage.getReferenceFromUrl("gs://parking-app-1603a.appspot.com/").child("Images/"+dataSnapshot.child("image").getValue(String.class));

                                        try {
                                            final File localFile = File.createTempFile("image","jpg");
                                            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                    imageView.setImageBitmap(bitmap);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(),"Failed to load image!",Toast.LENGTH_SHORT).show();
                                                    imageView.setImageResource(R.drawable.place_image);
                                                }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                            /*            storageReference.child("Images/" + dataSnapshot.child("image").getValue(String.class)).
                                                getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Picasso.get().load(uri.toString()).into(imageView);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(),"Failed to load image!",Toast.LENGTH_SHORT).show();
                                            }
                                        }); */

                                    }

                                    else {
                                        imageView.setImageResource(R.drawable.place_image);
                                    }

                                    phone.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (!checkPhonePermission()){
                                                return;
                                            }
                                            Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+phone_));
                                            startActivity(intent);

                                        }
                                    });

                                    lat2=(double) dataSnapshot.child("lat").getValue();
                                    long2=(double) dataSnapshot.child("lang").getValue();

                                    double longDiff = long1-long2;

                                    double distance = Math.sin(deg(lat1))
                                            *Math.sin(deg(lat2))
                                            +Math.cos(deg(lat1))
                                            *Math.cos(deg(lat2))
                                            *Math.cos(deg(longDiff));

                                    distance = Math.acos(distance);
                                    distance = rad(distance);
                                    distance = distance*60*1.1515;
                                    distance = distance*1.609344;

                                    dist.setText(String.format(Locale.UK, "%.2f Km" , distance));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            arrow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (linearLayout.getVisibility() == View.GONE) {
                                        linearLayout.setVisibility(View.VISIBLE);
                                        arrow.setBackgroundResource(R.drawable.arrow_up);
                                    }
                                    else {
                                        linearLayout.setVisibility(View.GONE);
                                        arrow.setBackgroundResource(R.drawable.down_arrow);
                                    }
                                }
                            });

                            FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
                            final DatabaseReference reference = rootNode.getReference("userInfo");
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child(user).child(marker.getTitle()).exists()){
                                        save.setImageResource(R.drawable.saved);
                                        save.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                save.setImageResource(R.drawable.unsaved);
                                                reference.child(user).child(marker.getTitle()).removeValue();
                                                Toast.makeText(MapActivity.this,"Removed from Saved",Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                    else {
                                        save.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                reference.child(user).child(marker.getTitle()).setValue(marker.getTitle());
                                                save.setImageResource(R.drawable.saved);
                                                Toast.makeText(MapActivity.this,"Added to Saved",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            bottomSheetDialog.setContentView(bottomSheetView);
                            bottomSheetDialog.show();
                        }
                    });

                return false;
            }
        });



        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 250);
        }

        LocationRequest locationRequest=LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder= new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient= LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException){
                    ResolvableApiException resolvable= (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

    }

    private boolean checkPhonePermission() {
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED){
           return true;
        }
        else{
            Dexter.withActivity(MapActivity.this)
                    .withPermission(Manifest.permission.CALL_PHONE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            return;
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            if (permissionDeniedResponse.isPermanentlyDenied()){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                                builder.setTitle("Permission Denied")
                                        .setMessage("Permission to access device location is permanently denied.")
                                        .setNegativeButton("Cancle",null)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent= new Intent();
                                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                intent.setData(Uri.fromParts("package",getPackageName(),null));

                                            }
                                        }).show();

                            }else {
                                Toast.makeText(MapActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        }
        return false;
    };

    private double rad(double distance) {
        return (distance*180.0/Math.PI);
    }

    private double deg(double lat1) {
        return (lat1* Math.PI/180.0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==51){
            if (resultCode== RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            mLastKnownLocation =task.getResult();
                            if (mLastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));
                                lat1= mLastKnownLocation.getLatitude();
                                long1=mLastKnownLocation.getLongitude();
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);

                                        if (locationResult == null){
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLatitude()),DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }

                        }
                        else {
                            Toast.makeText(MapActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
