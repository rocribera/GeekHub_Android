package org.udg.pds.todoandroid.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import org.udg.pds.todoandroid.util.Global;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileSettings extends AppCompatActivity {

    TodoApi mTodoService;
    String originalUsername;
    String originalDescription;
    String originalImageLink;
    Uri uriImage;
    boolean uploadImage;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=0;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        uploadImage = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        EditText description = (EditText) findViewById(R.id.settings_description);
        description.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        // To send the new settings to server
        Button uploadButton = (Button) findViewById(R.id.update_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateUserSettings();
            }
        });

        // To clear focus when last input has been done
        EditText linkText = findViewById(R.id.settings_link);
        linkText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // To clear focus, probably exists a better way for just remove the cursor
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    ConstraintLayout cl = findViewById(R.id.focusable_settings_layout);
                    cl.requestFocus();
                }
                return false;
            }
        });
        // Update the image when link text loses focus
        linkText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // To update the image
                    EditText linkImage = findViewById(R.id.settings_link);
                    String link = linkImage.getText().toString();
                    if (!link.isEmpty())
                        new ProfileSettings.DownloadImageFromInternet((ImageView) findViewById(R.id.settings_image)).execute(link);
                    else
                        new ProfileSettings.DownloadImageFromInternet((ImageView) findViewById(R.id.settings_image)).execute(originalImageLink);
                    uploadImage = false;
                }
            }
        });

        ImageView gallery = (ImageView) findViewById(R.id.gallery_icon);
        gallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ProfileSettings.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileSettings.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    }
                    else {
                        ActivityCompat.requestPermissions(ProfileSettings.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                }
            }
        });

        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        getOriginalUserInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode)
        {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery();
                }
                else {}
                break;
            }
        }
    }

    public void updateUserSettings() {
        User user = new User();
        EditText username = findViewById(R.id.settings_username);
        EditText description = findViewById(R.id.settings_description);
        EditText image = findViewById(R.id.settings_link);

        if (username.getText().toString().isEmpty())
            user.name = originalUsername;
        else
            user.name = username.getText().toString();

        if (description.getText().toString().isEmpty())
            user.description = originalDescription;
        else
            user.description = description.getText().toString();

        if (image.getText().toString().isEmpty())
            user.image = originalImageLink;
        else
            user.image = image.getText().toString();

        user.updatedImage = uploadImage;

        if (username.getText().toString().isEmpty() && description.getText().toString().isEmpty() && image.getText().toString().isEmpty() && !uploadImage)
            Toast.makeText(ProfileSettings.this.getBaseContext(), "Nothing changed", Toast.LENGTH_LONG).show();
        else {
            setUploadImage();
            /*
            String realPath = getRealPathFromURI(uriImage);
            File file = new File(realPath);
            Toast.makeText(ProfileSettings.this.getBaseContext(), file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            FileUploadService.uploadFile(file, "file");
            // normalment setUserSettings(user);
            */
        }
    }

    public void setUserSettings(User user)
    {
        Call<String> postCall = mTodoService.updateUser(user);
        postCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> postCall, Response<String> response) {
                if (response.isSuccessful()) {
                    if (uploadImage)
                        setUploadImage();
                        // FileUploadService.uploadFile(new File(uriImage.getPath()), "file");
                    else
                    {
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                }
            }
            @Override
            public void onFailure(Call<String> postCall, Throwable t) {
                Toast.makeText(ProfileSettings.this.getBaseContext(), "Fail updating the user", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setUploadImage()
    {
        String realPath = getRealPathFromURI(uriImage);
        File file = new File(realPath);
        Toast.makeText(ProfileSettings.this.getBaseContext(), file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file",file.getName(),requestFile);

        Call<String> call = mTodoService.uploadImage(multipartBody);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();
                }
                else {
                    Toast.makeText(ProfileSettings.this.getBaseContext(), "Error uploading image", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println(t.getMessage());
                System.out.println(t.getCause());
                System.out.println(t.getLocalizedMessage());
                System.out.println(t.getStackTrace().toString());
                Toast.makeText(ProfileSettings.this.getBaseContext(), "Error uploading image", Toast.LENGTH_LONG).show();
            }
        });
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

        String link = linkImage.getText().toString();
        if (!u.updatedImage)
        {
            if (link.isEmpty())
                new ProfileSettings.DownloadImageFromInternet((ImageView) this.findViewById(R.id.settings_image)).execute(originalImageLink);
            else
                new ProfileSettings.DownloadImageFromInternet((ImageView) this.findViewById(R.id.settings_image)).execute(link);
        }
        else
            showImage(u);
    }

    private void showImage(User u)
    {
        Call<ResponseBody> call = mTodoService.getImage(u.image);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Bitmap bi = BitmapFactory.decodeStream(response.body().byteStream());
                    ImageView iv=findViewById(R.id.settings_image);
                    iv.setImageBitmap(bi);
                } else {
                    Toast.makeText(getBaseContext(), "Error downloading profile image", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
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
            }
            catch (Exception e) {}
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    private void getImageFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, Global.RQ_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode) {
                case Global.RQ_GALLERY:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        ImageView settingsImage = (ImageView) findViewById(R.id.settings_image);
                        settingsImage.setImageBitmap(bitmap);
                        uriImage = selectedImage;
                        uploadImage=true;
                    } catch (IOException e) {
                        Toast.makeText(ProfileSettings.this.getBaseContext(), "Error loading image", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    // And to convert the image URI to the direct file system path of the image file
    public String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri,
                proj,                // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,    // WHERE clause selection arguments (none)
                null);      // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

}