package com.example.jcarle1025.taskmanagerdb2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditActivity extends AppCompatActivity implements View.OnClickListener{

    Button done;
    EditText title, det;
    Intent parent;
    Bundle myBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        title = (EditText) findViewById(R.id.taskTitle);
        det = (EditText) findViewById(R.id.taskDetail);
        done = (Button) findViewById(R.id.doneButon);
        done.setOnClickListener(this);

        parent = getIntent();
        myBundle = parent.getExtras();

        setUp();
    }

    public void setUp(){
        title.setText(myBundle.getString("title").toString());
        det.setText(myBundle.getString("detail").toString());
    }

    @Override
    public void onClick(View v) {
        myBundle.putString("newT", title.getText().toString());
        myBundle.putString("newD", det.getText().toString());
        parent.putExtras(myBundle);
        setResult(Activity.RESULT_OK, parent);
        finish();
    }
}

