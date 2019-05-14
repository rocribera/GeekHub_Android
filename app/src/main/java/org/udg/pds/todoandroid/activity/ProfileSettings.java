package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileSettings extends AppCompatActivity {

    TodoApi mTodoService;

    String originalUsername;
    String originalDescription;
    String originalImageLink;

    String newUsername;
    String newDescription;
    String newImageLink;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        EditText description = (EditText) findViewById(R.id.settings_description);
        description.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // To send the new settings to server
        Button uploadButton = (Button) findViewById(R.id.update_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });

        // To clear focus when last input has been done and update the image if link has text
        EditText linkText = findViewById(R.id.settings_link);
        linkText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId== EditorInfo.IME_ACTION_DONE) {
                    // To clear focus, probably exists a better way for just remove the cursor
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    ConstraintLayout cl =findViewById(R.id.focusable_settings_layout);
                    cl.requestFocus();
                    // To update the image
                    EditText linkImage = findViewById(R.id.settings_link);
                    String link = linkImage.getText().toString();
                    if (!link.isEmpty())
                        new ProfileSettings.DownloadImageFromInternet((ImageView) findViewById(R.id.settings_image)).execute(link);
                    else
                        new ProfileSettings.DownloadImageFromInternet((ImageView) findViewById(R.id.settings_image)).execute(originalImageLink);

                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        getOriginalUserInfo();
    }


    @Override
    public void onResume() {
        super.onResume();
        getOriginalUserInfo();
    }

    public void getOriginalUserInfo() {
        Call<User> call = mTodoService.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    showOriginalUserInfo(response.body());
                }
                else {
                    Toast.makeText(ProfileSettings.this.getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileSettings.this.getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void showOriginalUserInfo(User u) {
        EditText username = (EditText) this.findViewById(R.id.settings_username);
        EditText description = (EditText) this.findViewById(R.id.settings_description);
        EditText linkImage = (EditText) this.findViewById(R.id.settings_link);

        originalUsername=u.name;
        originalDescription=u.description;
        originalImageLink=u.image;

        username.setHint(u.name);
        description.setHint(u.description);
        linkImage.setHint(u.image);
        new ProfileSettings.DownloadImageFromInternet((ImageView) this.findViewById(R.id.settings_image)).execute(originalImageLink);
    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}
