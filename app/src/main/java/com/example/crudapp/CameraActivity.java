package com.example.crudapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;


public class CameraActivity extends AppCompatActivity {

    private ImageView captured_image;
    private TextView capture_image_btn, cancel_image_btn, detect_text_btn;
    private TextView pass_text_btn;
    private EditText text_view;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;

    MainActivity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cancel_image_btn = findViewById(R.id.cancel_image_btn);
        capture_image_btn = findViewById(R.id.capture_image_btn);
        detect_text_btn = findViewById(R.id.detect_text_btn);
        captured_image = findViewById(R.id.captured_image);
        text_view = findViewById(R.id.text_view);
        pass_text_btn = findViewById(R.id.pass_text_btn);

        capture_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_view.setText("");
                dispatchTakePictureIntent();
                //text_view.setText("");
            }
        });

        detect_text_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();
            }
        });

        cancel_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(intent);
                text_view.setText("");
                finish();

            }
        });
        pass_text_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String text = text_view.getText().toString();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("one",text);
                startActivity(intent);

            }
        });
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            captured_image.setImageBitmap(imageBitmap);
        }
    }

    private void detectTextFromImage() {

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer textRecognizer =
                FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> task = textRecognizer.processImage(firebaseVisionImage);

        task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CameraActivity.this,
                        "Could not get the text from the image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if (blockList.size() == 0) {
            Toast.makeText(CameraActivity.this, "No Text Found", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                String text = block.getText();
                text_view.setText(text);
            }
        }
    }

}