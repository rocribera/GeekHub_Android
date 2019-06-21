package org.udg.pds.todoandroid.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OtherUserProfile extends AppCompatActivity {

    TodoApi mTodoService;
    Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_profile);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        userId = getIntent().getExtras().getLong("userId");

        final FrameLayout content = this.findViewById(R.id.userProfileContent);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.userProfileContent, new UserProfileGames())
                .commit();

        UserProfilePosts fragmentUserProfilePost = new UserProfilePosts();
        Bundle bundle = new Bundle();
        bundle.putInt("type",1);
        fragmentUserProfilePost.setArguments(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.userProfileContent, fragmentUserProfilePost)
                .commit();

        checkBlock();
    }

    public void checkBlock(){
        Call<String> call = mTodoService.checkUserBlock(userId.toString());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    Button blockUserButton = findViewById(R.id.user_block_button);
                    Long blockType = Long.parseLong(response.body());
                    if(response.body() == null || blockType == 0) {


                        blockUserButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                blockUser(true,userId);
                            }
                        });


                    }
                    else{
                        if(userId.equals(blockType)){ //He blocked you
                            blockUserButton.setText(getString(R.string.user_otherBlock));
                            blockUserButton.setEnabled(false);
                        }
                        else{
                            blockUserButton.setText(getString(R.string.user_unblock));
                            blockUserButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    blockUser(false,userId);
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    public void blockUser(boolean block, Long userId){
        LayoutInflater layoutInflater = (LayoutInflater) OtherUserProfile.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupDelete = layoutInflater.inflate(R.layout.delete_confirmation, null);
        TextView deleteText = popupDelete.findViewById(R.id.delete_text);
        if(block) deleteText.setText("Are you sure you want to block this user?");
        else deleteText.setText("Are you sure you want to unblock this user?");
        PopupWindow popupWindow = new PopupWindow(OtherUserProfile.this);
        popupWindow.setContentView(popupDelete);
        popupWindow.showAtLocation(popupDelete, Gravity.CENTER,0,0);
        Button deleteConfirm = popupDelete.findViewById(R.id.delete_confirm);
        deleteConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<String> call = null;
                if(block) call = mTodoService.blockUserWithId(userId.toString());
                else call = mTodoService.unblockUserWithId(userId.toString());
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()){
                            Button blockUserButton = findViewById(R.id.user_block_button);
                            if(block){
                                blockUserButton.setText(getString(R.string.user_unblock));
                                blockUserButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        blockUser(false,userId);
                                    }
                                });
                            }
                            else{
                                blockUserButton.setText(getString(R.string.user_block));
                                blockUserButton.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        blockUser(true,userId);
                                    }
                                });
                            }
                            popupWindow.dismiss();
                        } else {
                            Toast.makeText(getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                    }
                });
            }
        });
        Button deleteCancel = popupDelete.findViewById(R.id.delete_cancel);
        deleteCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    public void getUserInfo(){
        Call<User> call = mTodoService.getUser(userId.toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    User u = response.body();
                    showProfileUserInfo(u);
                    if (u.updatedImage)
                        showImage(u);
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

    private void showImage(User u)
    {
        Call<ResponseBody> call = mTodoService.getImage(u.image);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Bitmap bi = BitmapFactory.decodeStream(response.body().byteStream());
                    ImageView iv=findViewById(R.id.user_image);
                    iv.setImageBitmap(bi);
                } else {
                    Toast.makeText(getBaseContext(), "Error downloading profile image", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
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
        if(user.image!=null) new OtherUserProfile.DownloadImageFromInternet((ImageView) this.findViewById(R.id.user_image)).execute(user.image);

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
