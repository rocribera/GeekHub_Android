package org.udg.pds.todoandroid.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostPage extends AppCompatActivity {

    TodoApi mTodoService;
    public static Activity postPage;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postPage = this;

        setContentView(R.layout.post_page);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        getPostInfo();
    }

    public void getPostInfo(){
        Long postId = getIntent().getExtras().getLong("postId");
        Call<Post> call = mTodoService.getPostInfo(postId.toString());

        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    setThisUser(response.body());
                } else {
                    Toast.makeText(PostPage.this.getBaseContext(), "Error reading post", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Post> call, Throwable t) {
            }
        });
    }

    public void setThisUser(Post post){
        Call<User> call = mTodoService.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    showPostInfo(post, response.body());

                } else {
                    Toast.makeText(PostPage.this.getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    public void showPostInfo(Post post, User user){
        TextView postTitle;
        TextView postDesc;

        CheckBox followCheck;
        CheckBox activeCheck;

        Button editButton;
        Button deleteButton;
        Button followersButton;
        Button creatorProfile;

        postTitle = findViewById(R.id.post_title_text);
        postDesc = findViewById(R.id.Description);
        creatorProfile = findViewById(R.id.author);

        activeCheck = findViewById(R.id.active_checkBox);
        followCheck = findViewById(R.id.follow_checkBox);

        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);
        followersButton = findViewById(R.id.chatButton);

        if(user.id == post.userId){
            deleteButton.setVisibility(View.VISIBLE);
            activeCheck.setVisibility(View.VISIBLE);
            followCheck.setVisibility(View.GONE);
            followersButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.VISIBLE);
        }
        else{
            deleteButton.setVisibility(View.GONE);
            followersButton.setVisibility(View.GONE);
            activeCheck.setVisibility(View.GONE);
            followCheck.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        }

        if(post.active) activeCheck.setChecked(true);

        activeCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Call<String> postCall = mTodoService.toggleActivePost(((Long)post.id).toString());

                postCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> postCall, Response<String> response) {
                        if (response.isSuccessful()) {
                        }
                    }

                    @Override
                    public void onFailure(Call<String> postCall, Throwable t) {
                    }
                });
            }
        });

        for(Post i : user.followedPosts){
            if(i.id == post.id){
                followCheck.setChecked(true);
                break;
            }
        }

        followCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Long postId = getIntent().getExtras().getLong("postId");
                if(isChecked){
                    Call<String> followCall = mTodoService.followPost(postId.toString());

                    followCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()){

                            }
                            else{
                                Toast.makeText(PostPage.this.getBaseContext(), "Error following post", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                }
                else{
                    Call<String> call = mTodoService.unfollowPost(postId.toString());

                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {

                            } else {
                                Toast.makeText(PostPage.this.getBaseContext(), "Error unfollowing post", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<String> call, Throwable t) {}
                    });
                }
            }
        });

        postTitle.setText(post.title);
        postDesc.setText(post.description);
        creatorProfile.setText("Offer by: " + post.username);
        followersButton.setText(post.followers.size() + " interested");

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupDelete = layoutInflater.inflate(R.layout.delete_confirmation, null);
        PopupWindow popupWindow = new PopupWindow(this);
        popupWindow.setContentView(popupDelete);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),EditPost.class);
                i.putExtra("postId", post.id);
                i.putExtra("title", post.title);
                i.putExtra("body", post.description);
                startActivityForResult(i, Global.RQ_ADD_POST);
            }
        });
        followersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),FollowersList.class);
                    i.putExtra("postId", post.id);
                    startActivity(i);
            }
        });
        creatorProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.id == post.userId)
                {
                    Intent i = new Intent(getApplicationContext(),NavigationActivity.class);
                    i.putExtra("goToProfile", true);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                else if (getIntent().hasExtra("comeFromOtherUserProfile") && getIntent().getExtras().getBoolean("comeFromOtherUserProfile"))
                {
                    getIntent().putExtra("comeFromOtherUserProfile", false);
                    finish();
                }
                else
                {
                    Intent i = new Intent(getApplicationContext(),OtherUserProfile.class);
                    i.putExtra("userId",post.userId);
                    startActivity(i);
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(popupDelete, Gravity.CENTER,0,0);
                Button deleteConfirm = popupDelete.findViewById(R.id.delete_confirm);
                deleteConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Call<String> call = mTodoService.deletePost(post.id.toString());
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if(response.isSuccessful()){
                                    popupWindow.dismiss();
                                    Intent data = getIntent();
                                    data.setData(Uri.parse(post.id.toString()));
                                    setResult(RESULT_OK, data);
                                    finish();
                                } else {
                                    Toast.makeText(PostPage.this.getBaseContext(), "Error deleting post", Toast.LENGTH_LONG).show();
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
        });
    }
}
