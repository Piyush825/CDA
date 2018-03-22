package com.example.Piyush.CDA;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;



public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
//    private static final String TAG = ;

    public GoogleApiClient apiClient;
    BroadcastReceiver myReceiver;
    TextView tv;
    int TIMER = 3000; // 3 sec
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    ImageView imageView;
    ImageButton imageButton;
    TextView editText;
    TextView editText2;
    TextView activity;
    Button get_location;
    Button upload;
    MyLocation myLocation ;
    Double lat,log;
    String act;
    MyLocation.LocationResult locationResult ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            imageView = (ImageView) findViewById(R.id.imageView);
            editText = (TextView) findViewById(R.id.editText);
            editText2 = (TextView) findViewById(R.id.editText2);
            activity = (TextView) findViewById(R.id.activity);
            get_location = (Button) findViewById(R.id.get_location);
            get_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    set_location();
                }
            });
            upload = (Button) findViewById(R.id.upload2);
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    upload();
                }
            });

            imageButton = (ImageButton) findViewById(R.id.imageButton);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("", "onClick: ");
                    dispatchTakePictureIntent();

                }
            });
            locationResult = new MyLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    lat = location.getLatitude();
                    log = location.getLongitude();
                    Log.d("MyApp", "gotLocation: " + Double.toString(lat) + " " + Double.toString(log));
                }
            };
            myLocation = new MyLocation(this, locationResult);

            apiClient = new GoogleApiClient.Builder(this)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            apiClient.connect();

            myReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String message = intent.getStringExtra(ActivityRecognizedService.LOCAL_BROADCAST_EXTRA);
                    Log.d("MyApp", "Activity: "+message);
                    activity.setText(message);
                    act=message;
                    upload.setEnabled(true);
//                    Toast.makeText(this,"You can submit photo now")
                }
            };

            upload.setEnabled(false);
            // verify Play Services is active and up-to-date
            checkGooglePlayServicesAvailable(this);

        }
        catch (Exception e)
        {
            e.printStackTrace();
//            Log.e();
        }
    }

    private void upload() {
        String url = "http://192.168.55.109:3000/upload/multipart";
        if (lat != null && log != null && mCurrentPhotoPath != null) {
            try {
                String uploadId = UUID.randomUUID().toString();

                //Creating a multi part request
                MultipartUploadRequest request = new MultipartUploadRequest(this, uploadId, url);

                request.addFileToUpload(mCurrentPhotoPath, "image_url");
                request.setNotificationConfig(new UploadNotificationConfig());
                request.setMaxRetries(1000);
                request.startUpload(); //Starting the upload

            }
            catch (Exception exc) {
                exc.printStackTrace();
                Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void set_location() {
        Log.d("aaaaa", "set_location:" + myLocation);
        try {
            if (myLocation==null)
            {
                Log.d("", "null mylocation ");
            }
//            Log.d("aaaaa", "set_location:" + lat);
            myLocation.getLocation(this, locationResult);

            if (lat != null && log != null) {
                editText.setText(Double.toString(lat));
                editText2.setText(Double.toString(log));
            } else {
                Toast.makeText(this, "Location Loading\n Please wait..", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                galleryAddPic();
            }
        }
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + " Piyush"+"_"+Double.toString(lat)+"_"+Double.toString(log)+"_"+act;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, TIMER, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter(ActivityRecognizedService.LOCAL_BROADCAST_NAME));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }


    public boolean checkGooglePlayServicesAvailable(Activity activity) {
        int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(activity, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }
}