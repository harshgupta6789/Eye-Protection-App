package com.hzitoun.camera2SecretPictureTaker.activities;
//app
import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.auth.*;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.hzitoun.camera2SecretPictureTaker.R;
import com.hzitoun.camera2SecretPictureTaker.listeners.PictureCapturingListener;
import com.hzitoun.camera2SecretPictureTaker.services.APictureCapturingService;
import com.hzitoun.camera2SecretPictureTaker.services.PictureCapturingServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.io.*;

import pl.droidsonroids.gif.GifImageView;


/**
 * App's Main Activity showing a simple usage of the picture taking service.
 * @author hzitoun (zitoun.hamed@gmail.com)
 */
public class MainActivity extends AppCompatActivity implements PictureCapturingListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String[] requiredPermissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
    };


    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private SensorEventListener sensorEventListener;

    WebView wv;

    FirebaseVisionImage image = null;
    FirebaseVisionFaceDetectorOptions highAccuracyOpts =
            new FirebaseVisionFaceDetectorOptions.Builder()
                    .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                    .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                    .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                    .build();
    FirebaseVisionFaceLandmark leftEye = null;
    FirebaseVisionFaceLandmark rightEye = null;
    FirebaseVisionPoint leftEyePos = null;
    FirebaseVisionPoint rightEyePos = null;
    double dist, x1, x2, y1, y2;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;

    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;




    //The capture service
    private APictureCapturingService pictureService;
    Thread t = new Thread() {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(15000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            pictureService.startCapturing(MainActivity.this);
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        pl.droidsonroids.gif.GifImageView gf = (GifImageView) findViewById(R.id.abcd);
        Random r = new Random();
        switch (r.nextInt(8)){
            case 0:
                gf.setImageResource(R.drawable.giphy_1);
                break;
            case 1:
                gf.setImageResource(R.drawable.giphy_2);
                break;
            case 2:
                gf.setImageResource(R.drawable.giphy_3);
                break;
            case 3:
                gf.setImageResource(R.drawable.giphy_4);
                break;
            case 4:
                gf.setImageResource(R.drawable.giphy_5);
                break;
            case 5:
                gf.setImageResource(R.drawable.giphy_6);
                break;
            case 6:
                gf.setImageResource(R.drawable.giphy_7);
                break;
            case 7:
                gf.setImageResource(R.drawable.giphy_8);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        uploadBackPhoto = (ImageView) findViewById(R.id.backIV);
        uploadFrontPhoto = (ImageView) findViewById(R.id.frontIV);
        final Button btn = (Button) findViewById(R.id.startCaptureBtn);
        // getting instance of the Service from PictureCapturingServiceImpl
        pictureService = PictureCapturingServiceImpl.getInstance(this);
        pl.droidsonroids.gif.GifImageView gf = (GifImageView) findViewById(R.id.abcd);

//        Toast toast = new Toast(this);
//        ImageView view = new ImageView(this);
//        view.setImageResource(R.drawable.giphy_7);
//        toast.setView(view);
//        toast.show();

//        showToast("Starting capture!");
        pictureService.startCapturing(this);



//        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
//        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorEventListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent sensorEvent) {
//                if(sensorEvent.values[0] >4 || sensorEvent.values[1] > 4 || sensorEvent.values[2] > 4) {
//                    showToast("Starting capture!");
//                    pictureService.startCapturing(MainActivity.this);
//                }
//            }
//
//            @Overridex
//            public void onAccuracyChanged(Sensor sensor, int i) {
//
//            }
//        };
//        sensorManager.registerListener(sensorEventListener, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);



//        registerReceiver(br, new IntentFilter("com.example.filterMe"));
//        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent("com.example.filterMe"), 0);
//        mgr.setRepeating(AlarmManager.INTERVAL_FIFTEEN_MINUTES, SystemClock.elapsedRealtime(), 300, pi);



        t.start();

    }
    
    private void showImageToast(final int number) {
        runOnUiThread(() -> {
            Toast toast = new Toast(getApplicationContext());
            ImageView view = new ImageView(getApplicationContext());
            view.setImageResource(number);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
                    //Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void showToast(final String text) {
        runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        );
    }

    /**
    * We've finished taking pictures from all phone's cameras
    */    
    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && picturesTaken.size() == 2) {
            //showToast("Done capturing all photos!");
            //String uri = Environment.getExternalStorageDirectory() + "1_pic.jpg";
            Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/1_pic.jpg"));
            try {
                image = FirebaseVisionImage.fromFilePath(this, uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(highAccuracyOpts);

            Task<List<FirebaseVisionFace>> result =
                    detector.detectInImage(image)
                            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                                    if(firebaseVisionFaces.size() == 1) {
                                        leftEye = firebaseVisionFaces.get(0).getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                        rightEye = firebaseVisionFaces.get(0).getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                        if(leftEye != null){
                                            leftEyePos = leftEye.getPosition();
                                            x1 = leftEyePos.getX();
                                            y1 = leftEyePos.getY();
                                        }
                                        if(rightEye != null){
                                            rightEyePos = rightEye.getPosition();
                                            x2 = rightEyePos.getX();
                                            y2 = rightEyePos.getY();
                                        }
                                        dist = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));

                                        if(dist >= 900) showImageToast(R.drawable.backoff);
                                        else if (770.0 <= dist && dist < 900.0) showImageToast(R.drawable.backoff);
                                        else if (720.0 < dist && dist < 770.0) showImageToast(R.drawable.keepitup);
                                        else if (680.0 < dist && dist < 720.0) showImageToast(R.drawable.comeahead);
                                        else if (dist <= 680) showImageToast(R.drawable.comeahead);

                                    } else  if(firebaseVisionFaces.size() == 0) showToast("Too many faces");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showToast("No Faces Found");
                                }
                            });
            return;
        }
        //showToast("No camera detected!");
    }

    /**
    * Displaying the pictures taken.
    */             
    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(() -> {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                if (pictureUrl.contains("0_pic.jpg")) {
                    uploadBackPhoto.setImageBitmap(scaled);
                } else if (pictureUrl.contains("1_pic.jpg")) {
                    uploadFrontPhoto.setImageBitmap(scaled);
                }
            });
            //showToast("Picture saved to " + pictureUrl);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    /**
     * checking  permissions at Runtime.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }
}

