package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserRegister;
import org.udg.pds.todoandroid.rest.TodoApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Register extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Button b_reg = (Button)Register.this.findViewById(R.id.register_button);
        // This is the listener that will be used when the user presses the "Register" button
        b_reg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText u = (EditText) Register.this.findViewById(R.id.register_username);
                EditText p = (EditText) Register.this.findViewById(R.id.register_password);
                EditText e = (EditText) Register.this.findViewById(R.id.register_email);
                Register.this.checkCredentials(u.getText().toString(), p.getText().toString(), e.getText().toString());
            }
        });
    }

    // This method is called when the "Register" button is pressed in the Register fragment
    public void checkCredentials(String username, String password, String email) {
        UserRegister ul = new UserRegister();
        ul.username = username;
        ul.password = password;
        ul.email = email;
        Call<User> call = mTodoService.register(ul);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccessful()) {
                    Register.this.startActivity(new Intent(Register.this, NavigationActivity.class));
                    Register.this.finish();
                } else {
                    Toast toast = Toast.makeText(Register.this, "Error register in", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast toast = Toast.makeText(Register.this, "Error register in", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

}

