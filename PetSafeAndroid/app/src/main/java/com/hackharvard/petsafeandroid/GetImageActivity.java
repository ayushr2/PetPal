package com.hackharvard.petsafeandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ayushranjan on 20/10/17.
 */

class GetImageActivity extends Activity {
    private static final String PACKAGE_NAME = "com.hackharvard.petsafeandroid";
    private static final int CAMERA_CODE = 1470;
    private Button captureButton;
    private String pathToPhoto;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.get_image_activity);

        captureButton = findViewById(R.id.button);
        
    }

    /**
     * This method gets a valid file in external memory to save the picture and starts ActivityForResult
     * which fills the file with the complete full size image from the camera.
     * WORKED FOR HOURS ON THE PERMISSIONS!!!!!!!!!!!
     *
     * @see <a href="https://developer.android.com/training/camera/photobasics.html#TaskPath">Full Size Pic from Camera</a>
     */
    public void getImageFromCamera() {
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

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                List<ResolveInfo> resolvedIntentActivities = context
                        .getPackageManager()
                        .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;

                    context.grantUriPermission(packageName,
                            photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            setResult(CAMERA_CODE, takePictureIntent);
            startActivityForResult(takePictureIntent, CAMERA_CODE);

            //revoke permissions after use
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                context.revokeUriPermission(photoURI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
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
}
