package com.hackharvard.petsafeandroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ayushranjan on 20/10/17.
 */

public class GetImageActivity extends AppCompatActivity{
    private static final String PACKAGE_NAME = "com.hackharvard.petsafeandroid";
    private static final int CAMERA_CODE = 1470;
    private static final int MY_PERMISSION_REQUEST_READ_COARSE_LOCATION = 102;
    private static final int MY_CAMERA_REQUEST_CODE = 1469;
    private Button captureButton;
    private String pathToPhoto;
    private double latitude;
    private double longitude;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_image_activity);
        captureButton = findViewById(R.id.button);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFromCamera();
            }
        });
    }

    /**
     * This method gets a valid file in external memory to save the picture and starts ActivityForResult
     * which fills the file with the complete full size image from the camera.
     * WORKED FOR HOURS ON THE PERMISSIONS!!!!!!!!!!!
     *
     * @see <a href="https://developer.android.com/training/camera/photobasics.html#TaskPath">Full Size Pic from Camera</a>
     */
    public void getImageFromCamera() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            return;
        }

        Intent takePictureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Context context = getApplicationContext();

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            photoFile = createImageFile();

            if (photoFile == null)
                return;

            Uri photoURI = FileProvider.getUriForFile(context,
                    PACKAGE_NAME + ".fileprovider",
                    photoFile);
            context.grantUriPermission(PACKAGE_NAME,
                    photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.grantUriPermission(PACKAGE_NAME,
                    photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            setResult(CAMERA_CODE, takePictureIntent);
            startActivityForResult(takePictureIntent, CAMERA_CODE);
        }
    }

    /**
     * This method creates a new file in the external storage to save a full size photo. I have used
     * getExternalFilesDir method so that the file is private only to the app. If the photo needs
     * to be saved in a public directory so that it is accessible to other apps then look at link.
     *
     * @return file where the photo has to be saved
     * @throws IOException throws generic exception if error occurs
     * @see <a href="https://developer.android.com/training/camera/photobasics.html#TaskPath">Saving Full Photo</a>
     */
    protected File createImageFile() {
        // File name is current time and date to avoid conflict.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists())
            storageDir.mkdir();

        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.file_create_fail),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }

        // Save a file: path for use with ACTION_VIEW intents
        pathToPhoto = image.getAbsolutePath();
        return image;
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Return if result is not okay
        if (resultCode != RESULT_OK) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.operation_cancel), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.image_get_fail), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (pathToPhoto != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermission();
            else
                saveResultToDB();
        } else {
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            String imageEncoded = encodeTobase64(BitmapFactory.decodeFile(pathToPhoto));
            SharedPreferences setting = getSharedPreferences(Constants.PREF_NAME, 0);
            String email = setting.getString(Constants.EMAIL, "");
            Post post = new Post(email, latitude, longitude, imageEncoded);
            String node = pathToPhoto.substring(pathToPhoto.lastIndexOf("/") + 1,
                    pathToPhoto.lastIndexOf('.')) + email;
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference postRef = firebaseDatabase.getReference("posts").child(node.replace('.','_'));
            postRef.setValue(post);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_READ_COARSE_LOCATION);
    }

    private void saveResultToDB() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), "Location Permission not granted", Toast.LENGTH_LONG).show();
                else
                    saveResultToDB();
                break;
            case MY_CAMERA_REQUEST_CODE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getApplicationContext(), "Camera Permission not granted", Toast.LENGTH_LONG).show();
                else
                    getImageFromCamera();
        }
    }
}
