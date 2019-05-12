package org.udg.pds.todoandroid.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.fragment.UserProfileGames;
import org.udg.pds.todoandroid.fragment.UserProfilePosts;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.io.InputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OtherUserProfile extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_profile);

        final FrameLayout content = this.findViewById(R.id.userProfileContent);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.userProfileContent, new UserProfileGames())
                .commit();

        UserProfilePosts fragment = new UserProfilePosts();
        Bundle bundle = new Bundle();
        bundle.putInt("type",1);
        fragment.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.userProfileContent, fragment)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    public void getUserInfo(){

        Long userId = getIntent().getExtras().getLong("userId");
        Call<User> call = mTodoService.getUser(userId.toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    showProfileUserInfo(response.body());
                    //UserProfile.this.showGameList(response.body().games);
                } else {
                    Toast.makeText(getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showProfileUserInfo(User user){

        TextView userUsername;
        TextView userDescription;
        RatingBar userRating;

        userUsername = this.findViewById(R.id.user_username);
        userDescription = this.findViewById(R.id.user_description);
        userRating = this.findViewById(R.id.user_rating);

        userUsername.setText(user.name);
        userDescription.setText(user.description);
        userRating.setRating(user.valoration);
        new OtherUserProfile.DownloadImageFromInternet((ImageView) this.findViewById(R.id.user_image)).execute(user.image);

        // Rating popup

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupRating = layoutInflater.inflate(R.layout.rating_valoration, null);
        PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setContentView(popupRating);
        popupWindow.setFocusable(true);
        TextView tv = popupRating.findViewById(R.id.rating_text);
        tv.setText("Rank "+user.name);

        userRating.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    popupWindow.showAtLocation(popupRating, Gravity.CENTER,0,0);
                    Button ratingSend = popupRating.findViewById(R.id.rating_send);
                    ratingSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RatingBar rb = popupRating.findViewById(R.id.rating_valoration);
                            Float valoration = (Float) rb.getRating();
                            Long userId = user.id;
                            Call<String> call = mTodoService.ratingUser(userId.toString(), valoration.toString());
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.isSuccessful()){
                                        popupWindow.dismiss();
                                        getUserInfo();
                                    } else {
                                        Toast.makeText(OtherUserProfile.this.getBaseContext(), "Error rating this user", Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(OtherUserProfile.this.getBaseContext(), "Failure rating this user", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });
                }
                return true;
            }
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

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}
