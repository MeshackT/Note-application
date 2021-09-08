package com.example.crudapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ReminderActivity extends AppCompatActivity {

    //Time Peeker
//    private ImageView show_time_btn;
    private TextView set_time_btn;
    private TextView cancel_time_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        //time peeker
//        show_time_btn = findViewById(R.id.show_time_set_btn);
        set_time_btn = findViewById(R.id.set_time_btn);
        cancel_time_btn = findViewById(R.id.cancel_time_btn);



    }
}
