package org.udg.pds.todoandroid.activity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.udg.pds.todoandroid.Constants;
import org.udg.pds.todoandroid.MyNotificationManager;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserLogin;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.services.MyFirebaseMessagingService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created with IntelliJ IDEA.
 * User: imartin
 * Date: 28/03/13
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */

// This is the Login fragment where the user enters the username and password and
// then a RESTResponder_RF is called to check the authentication
public class Login extends AppCompatActivity {

    TodoApi mTodoService;
    static Login login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        login = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Button b_log = (Button)findViewById(R.id.login_button);
        // This is the listener that will be used when the user presses the "Login" button
        b_log.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText u = (EditText) Login.this.findViewById(R.id.login_username);
                EditText p = (EditText) Login.this.findViewById(R.id.login_password);
                Login.this.checkCredentials(u.getText().toString(), p.getText().toString());
            }
        });

        Button b_reg = (Button)findViewById(R.id.register_button);
        // This is the listener that will be used when the user presses the "Register" button
        b_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Register.class);
                startActivity(i);
            }
        });


    }

    // This method is called when the "Login" button is pressed in the Login fragment
    public void checkCredentials(String username, String password) {
        UserLogin ul = new UserLogin();
        ul.username = username;
        ul.password = password;
        Call<User> call = mTodoService.login(ul);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccessful()) {
                    String TAG = "Firebase Token: ";
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                        return;
                                    }

                                    // Get new Instance ID token
                                    String token = task.getResult().getToken();

                                    // Log and toast
                                    Log.d(TAG, token);
                                    Call<String> call = ((TodoApp)Login.this.getApplication()).getAPI().sendToken(token);
                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {

                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                                }
                            });
                    Login.this.startActivity(new Intent(Login.this, NavigationActivity.class));
                    Login.this.finish();
                } else {
                    Toast toast = Toast.makeText(Login.this, "Error logging in", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast toast = Toast.makeText(Login.this, "Error logging in", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static Login getInstance(){
        return login;
    }

}