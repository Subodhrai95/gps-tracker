package com.subodh.googlemaps;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
//import com.google.android.gms.location.places.Places;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private static final int SELECT_PICTURE = 0;
    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final LatLngBounds LAT_LNG_BOUNDS=new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    //widgets
    private AutoCompleteTextView mSearchText;
    private TextView userId;
    private ImageView mGPS;
    private ImageView user_profile;
    private Toolbar toolbar;
    private ImageView menu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    //vars
    //private Autocomplete autocomplete;
    private boolean mLocation_Permission_granted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15F;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int AUTOCOMPLETE_REQUEST_CODE=1;
    //private PlaceAutocompleteAdapter mplaceAutocompleteAdapter;
    private GoogleApiClient mgoogleApiClient;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private Place place;

    //Firebase
    private Uri filePath;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    // user profile
    UserProfileActivity userProfile=new UserProfileActivity();

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        if (mLocation_Permission_granted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.
                    PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.
                    ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGPS=(ImageView)findViewById(R.id.ic_gps);
        if (isServicesOK())
        {
            getLocationPermission();
        }

        menu=(ImageView)findViewById(R.id.ic_menu);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();

            }
        });

    }

        public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version...");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user cane make map requests
            Log.d(TAG, "isServicesOK: Google play services working... ");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // an error occur but we can resolve it
            Log.d(TAG, "isServicesOK: en error occurred but we can resolve it! ");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else
        {
            Toast.makeText(this, "you can't make map request!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void init(){

        Log.d(TAG,"init: initialising...");

        /*autocompleteSupportFragment=(AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.input_search);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.


                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NonNull Status status) {

                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });*/

       /*mgoogleApiClient=new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this).build();

        mplaceAutocompleteAdapter=new PlaceAutocompleteAdapter(this, mgoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchText.setAdapter(mplaceAutocompleteAdapter);*/

       mSearchText.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               if (!Places.isInitialized())
               {
                   Places.initialize(MapsActivity.this, getString(R.string.google_maps_key));

               }

               List<Place.Field> fields=Arrays.asList(Place.Field.ID, Place.Field.NAME);
               PlacesClient placesClient = Places.createClient(MapsActivity.this);

               Intent intent=new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields )
                       .setTypeFilter(TypeFilter.ADDRESS)
                       .setTypeFilter(TypeFilter.CITIES)
                       .setTypeFilter(TypeFilter.REGIONS)
                       .build(MapsActivity.this);
               startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

           }
       });
         mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId== EditorInfo.IME_ACTION_SEARCH || actionId==EditorInfo.IME_ACTION_DONE
                || keyEvent.getAction()==KeyEvent.ACTION_DOWN|| keyEvent.getAction()==KeyEvent.KEYCODE_ENTER)
                {
                    // execute method for searching
                    geoLocate();

                }

                return false;
            }
        });
        mGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();
    }

    protected void geoLocate(){

        Log.d(TAG,"geoLocate: geo-locating...");
        //String searchString=mSearchText.getText().toString();
        String searchString=place.toString();
        Geocoder geocoder= new Geocoder(MapsActivity.this);
        List<Address> list= new ArrayList<>();
        try{
            list=geocoder.getFromLocationName(searchString, 1);

        }catch (IOException e){
            Log.e(TAG,"getLocate: IOException: "+e.getMessage());
        }
        if (list.size()>0)
        {
            Address address=list.get(0);
            Log.d(TAG,"geoLocate: found a location"+address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    private void getDeviceLocation()
    {
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        try{

            if (mLocation_Permission_granted)
            {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful())
                        {
                            Log.d(TAG,"onComplete: Found location!");
                            Location currentLocation=(Location)task.getResult();
                            if (currentLocation!=null)
                            {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM,"you are here");

                            }
                        }else {
                            Log.d(TAG,"onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }catch (SecurityException e){

            Log.e(TAG, "getDeviceLocation: SecurityException: "+e.getMessage());
        }
    }

    protected void moveCamera(LatLng latLng, Float zoom, String title)
    {
        Log.d(TAG,"moveCamera: moving to camera to Lat:"+ latLng.latitude + ", Lng:" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
       // mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
        //mMap.moveCamera(CameraUpdateFactory.zoomIn());
       // mMap.moveCamera(CameraUpdateFactory.zoomOut());
        if (!title.equals("you are here")){
            MarkerOptions options=new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }
            hideSoftKeyboard();
    }

    private void initMap()
    {
        Log.d(TAG, "initMap: initializing map...");
        SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
     }

    private void getLocationPermission()
    {
        Log.d(TAG,"getLocationPermission: getting location...");
        String[] permission={Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
            {
                 mLocation_Permission_granted=true;
                initMap();
            }else
            {
                ActivityCompat.requestPermissions(this, permission,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else
        {
            ActivityCompat.requestPermissions(this, permission,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    //Callback for the result from requesting permissions
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionResult: called");
        mLocation_Permission_granted=false;
        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    for(int i=0; i<grantResults.length; i++)
                    {
                        if (grantResults[0]!=PackageManager.PERMISSION_GRANTED)
                        {
                            return;
                        }
                    }
                    Log.d(TAG,"onRequestPermissionsResult: Permission Granted");
                    mLocation_Permission_granted=true;
                    //initializing map
                    initMap();
                }{

                }
            }
        }
    }

    private void hideSoftKeyboard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.tracking:
            {
                Intent intent = new Intent(this, TrackerActivity.class);
                startActivity(intent);

                break;
            }
            case R.id.join_circle:
            {
                Toast.makeText(MapsActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                //databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        //.setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"Online"));
                break;
            }
            case R.id.my_circle:
            {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.joined_circle:
            {
                Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.logout: {
                try {
                    //progressBar.setVisibility(View.VISIBLE);
                    //progressDialog.setMessage("Logging out..");
                    // Toast.makeText(this,"logout", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    //progressDialog.dismiss();
                   // progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(this,"Logout successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }catch (Exception e)
                {
                    Toast.makeText(this," "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
            }
            case R.id.share_to:
            {
                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"GPS Tracker:");
                intent.setType("plan/text");
                startActivity(Intent.createChooser(intent, "Share To"));

                break;
            }
            case R.id.invite_friend:
            {
                inviteFriend();
                break;
            }
        }
        return true;
    }
    private void inviteFriend() {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void SelectImage(View view)
    {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_PICK);
        //startActivityForResult(Intent.createChooser(intent,"select image:"),SELECT_PICTURE);
        startActivityForResult(intent, SELECT_PICTURE);

    }

    private Bitmap getPath(Uri uri) {

        String[] filePathColumn={MediaStore.Images.Media.DATA};
        Cursor cursor=(Cursor) managedQuery(uri, filePathColumn,null,null,null);
        int column_index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String file_path=cursor.getString(column_index);
        Bitmap bitmap= BitmapFactory.decodeFile(file_path);
        return bitmap;
    }

    private void uploadImage()
    {
        if(filePath!=null)
        {
            StorageReference reference=storageReference.child("images/"+ UUID.randomUUID().toString());
            reference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MapsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case AUTOCOMPLETE_REQUEST_CODE:
            {
                if (resultCode == RESULT_OK) {

                    place = Autocomplete.getPlaceFromIntent(data);

                    geoLocate();
                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.i(TAG, status.getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
            }
            case SELECT_PICTURE:
            {
                if (resultCode == RESULT_OK && data!= null&& data.getData()!=null) {

                    NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
                    View header=navigationView.getHeaderView(0);
                    user_profile=(ImageView)header.findViewById(R.id.profile);
                    filePath=data.getData();
                    /*try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                        user_profile.setImageBitmap(bitmap);
                        uploadImage();
                    }catch (IOException e){e.getStackTrace();}*/
                    Bitmap bitmap=getPath(data.getData());
                    user_profile.setImageBitmap(bitmap);
                    uploadImage();

                }
            }

        }

    }
}
