package com.example.crudapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private Button mSavebtn, mMicrophone;
    private EditText mTitle, mDesc;
    public String uTitle, uDesc, uId;
    private String imageCapture;
    private FirebaseFirestore db;
    BottomNavigationView bottomNavigationView;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //check if permission is granted to use the mic on ur device
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO))
                != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }

//        recyclerView = findViewById(R.id.recycleView);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.activity_main);
        bottomNavigationView.getMenu().findItem(R.id.activity_reload).setVisible(false);

//        recyclerView = findViewById(R.id.recycleViewmain);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mTitle = findViewById(R.id.edit_title);
        mDesc = findViewById(R.id.edit_desc);
        mSavebtn = findViewById(R.id.save_btn);
        mMicrophone = findViewById(R.id.microphone);

        Intent intent = getIntent();
        String text = intent.getStringExtra("one");
        mTitle.setText(text);
        Log.i("", mTitle.toString());

        db = FirebaseFirestore.getInstance();

        //get information from MyAdapter class
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mSavebtn.setText("Update");
            uTitle = bundle.getString("uTitle");
            uDesc = bundle.getString("uDesc");
            uId = bundle.getString("uId");
            mTitle.setText(uTitle);
            mDesc.setText(uDesc);

        } else {
            mSavebtn.setText("Save");
        }

        mMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //prompt the user to speak
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

                try {
                    //call the method to speak
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                } catch (Exception e) {
                    Toast
                            .makeText(MainActivity.this, " " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                }
            }

        });

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText().toString();
                String desc = mDesc.getText().toString();

                Bundle bundle1 = getIntent().getExtras();
                if (bundle != null) {
                    String id = uId;
                    updateToFireStore(id, title, desc);
                } else {
                    String id = UUID.randomUUID().toString();
                    saveToFireStore(id, title, desc);

                }
                clearControls();

            }
        });


    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                mDesc.setText(
                        Objects.requireNonNull(result).get(0));
            }
        }
    }

    private void clearControls() {

        mTitle.setText("");
        mDesc.setText("");
    }

    private void updateToFireStore(String id, String title, String desc) {

        db.collection("Documents").document(id).update("title", title
                , "desc", desc).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isComplete()) {
                    Toast.makeText(MainActivity.this, "Data Updated ",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "ERROR!: "
                            + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFireStore(String id, String title, String desc) {
        if (!title.trim().isEmpty() && !desc.trim().isEmpty()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("title", title);
            map.put("desc", desc);

            //add a collection of documents with Id then sub title and description
            db.collection("Documents").document(id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()) {
                        Toast.makeText(MainActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to save data", Toast.LENGTH_SHORT).show();

                }
            });


        } else {
            Toast.makeText(MainActivity.this, "Enter title and description", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.activity_show) {
            Intent intent = new Intent(MainActivity.this, ShowActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.activity_camera){
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        }
        return false;
    }
}