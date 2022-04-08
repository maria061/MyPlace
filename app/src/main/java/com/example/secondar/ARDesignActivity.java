package com.example.secondar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ARDesignActivity extends AppCompatActivity {
    private User user;
    private FirebaseUser firebaseUser;
    private StorageReference storageRef;
    private DatabaseReference imageUrlStore;
    private ArFragment arFragment;
    private ArrayList<Integer> imagesPath = new ArrayList<>();
    private ArrayList<String> namesPath = new ArrayList<>();
    private ArrayList<String> modelNames = new ArrayList<>();
    private AnchorNode anchorNode;
    private Button btnRemove;
    private Button btnSaveImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ardesign);

        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        firebaseUser = intent.getParcelableExtra("firebaseUser");

        storageRef = FirebaseStorage.getInstance().getReference();
        imageUrlStore = FirebaseDatabase.getInstance().getReference("images").child(firebaseUser.getUid());
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        btnRemove = (Button) findViewById(R.id.removeAR);
        btnSaveImage = findViewById(R.id.saveAR);
        getImages();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            Anchor anchor = hitResult.createAnchor();
            Log.d("ARDesignActivtiy", Common.model);
            ModelRenderable.builder()
                    .setSource(this, Uri.parse(Common.model))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable));

        });

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAnchorNode(anchorNode);
            }
        });

        btnSaveImage.setOnClickListener(view -> {

            //ask for storage permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            } else {
                //do nothing at the moment
            }

            ArSceneView arSceneView = arFragment.getArSceneView();

            // Create a bitmap the size of the scene view.
            final Bitmap bitmap = Bitmap.createBitmap(arSceneView.getWidth(), arSceneView.getHeight(),
                    Bitmap.Config.ARGB_8888);

            // Create a handler thread to offload the processing of the image.
            final HandlerThread handlerThread = new HandlerThread("PixelCopier");
            handlerThread.start();
            // Make the request to copy.
            PixelCopy.request(arSceneView, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date());
                    String imageName = "MyPlace" + timeStamp + ".png";
                    Uri imageUri = store(bitmap, imageName);
                    if (imageUri != null) {
                        uploadImage(imageUri, imageName);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Taking Screenshot Failed", Toast.LENGTH_SHORT).show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        });
    }

    public void uploadImage(Uri imageUri, String imageName) {

        StorageReference imageRef= storageRef.child("images/" + firebaseUser.getUid() + "/" + imageName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ARDesignActivity.this,  R.string.image_uploaded_success, Toast.LENGTH_LONG ).show();
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String key = imageUrlStore.push().getKey();
                                imageUrlStore.child(key).setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ARDesignActivity.this, R.string.image_uploaded_success, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ARDesignActivity.this, R.string.image_not_uploaded, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_AR_granted, Toast.LENGTH_LONG).show();
                //do nothing at the moment
            } else {
                Toast.makeText(this, R.string.permission_AR_notGranted, Toast.LENGTH_LONG).show();
            }
        }
    }


    //store the image on the device
    public Uri store(Bitmap bm, String fileName) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyFiles";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dirPath, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Uri uri = Uri.fromFile(file);
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_general_message, Toast.LENGTH_LONG).show();
        }
       return null;
    }

    private void getImages() {

        imagesPath.add(R.drawable.table);
        imagesPath.add(R.drawable.bookshelf);
        imagesPath.add(R.drawable.lamp);
        imagesPath.add(R.drawable.odltv);
        imagesPath.add(R.drawable.clothdryer);
        imagesPath.add(R.drawable.chair);
        imagesPath.add(R.drawable.drawer);
        imagesPath.add(R.drawable.sofa1);
        imagesPath.add(R.drawable.desk);
        imagesPath.add(R.drawable.bed);
        imagesPath.add(R.drawable.bench);
        imagesPath.add(R.drawable.roundtable);
        imagesPath.add(R.drawable.cofee_table);
        imagesPath.add(R.drawable.candle_stick);

        namesPath.add("Table");
        namesPath.add("BookShelf");
        namesPath.add("Lamp");
        namesPath.add("Old Tv");
        namesPath.add("Cloth Dryer");
        namesPath.add("Chair");
        namesPath.add("Drawer");
        namesPath.add("White sofa");
        namesPath.add("Desk");
        namesPath.add("Bed");
        namesPath.add("Bench");
        namesPath.add("Round Table");
        namesPath.add("Coffee Table");
        namesPath.add("Candle Stick");

        modelNames.add("table.sfb");
        modelNames.add("model.sfb");
        modelNames.add("lamp.sfb");
        modelNames.add("tv.sfb");
        modelNames.add("cloth.sfb");
        modelNames.add("chair.sfb");
        modelNames.add("Drawer.sfb");
        modelNames.add("sofa1.sfb");
        modelNames.add("desk.sfb");
        modelNames.add("bed.sfb");
        modelNames.add("bench.sfb");
        modelNames.add("table_rondi.sfb");
        modelNames.add("coffeeTable.sfb");
        modelNames.add("CandleStick.sfb");

        initiateRecyclerView();
    }

    private void initiateRecyclerView() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView =  (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerviewAdapter adapter = new RecyclerviewAdapter(this, namesPath, imagesPath, modelNames);
        recyclerView.setAdapter(adapter);

    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {

        anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    public void removeAnchorNode(AnchorNode nodeToremove) {
        if (nodeToremove != null) {
            arFragment.getArSceneView().getScene().removeChild(nodeToremove);
            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            nodeToremove = null;
        }
    }
}
