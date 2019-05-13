package org.udg.pds.todoandroid.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.rest.TodoApi;

public class ProfileSettings extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);

        EditText description = (EditText) findViewById(R.id.settings_description);
        description.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

}
