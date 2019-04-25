package org.udg.pds.todoandroid.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostPage extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_page);

        Switch follow = (Switch) findViewById(R.id.follow_switch);

        follow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        getPostInfo();

        Button deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(view -> {
            //Estàs segur d'eliminar el post??
        });
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
        TextView postUser;
        TextView postFollowers;

        Switch activeSwitch;
        Switch followSwitch;

        Button deleteButton;
        Button followersButton;

        postTitle = findViewById(R.id.post_title_text);
        postDesc = findViewById(R.id.Description);
        postUser = findViewById(R.id.author);
        postFollowers = findViewById(R.id.followers_text);

        activeSwitch = findViewById(R.id.active_switch);
        followSwitch = findViewById(R.id.follow_switch);

        deleteButton = findViewById(R.id.delete_button);
        followersButton = findViewById(R.id.chatButton);

        if(user.id == post.userId){
            deleteButton.setVisibility(View.VISIBLE);
            activeSwitch.setVisibility(View.VISIBLE);
            followSwitch.setVisibility(View.GONE);
            followersButton.setVisibility(View.VISIBLE);
        }
        else{
            deleteButton.setVisibility(View.GONE);
            followersButton.setVisibility(View.GONE);
            activeSwitch.setVisibility(View.GONE);
            followSwitch.setVisibility(View.VISIBLE);
        }

        if(post.active) activeSwitch.setChecked(true);

        /*activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        });*/

        for(User i : post.followers){
            if(i.id == user.id){
                followSwitch.setChecked(true);
                break;
            }
        }

        postTitle.setText(post.title);
        postDesc.setText(post.description);
        postUser.setText(post.username);
        postFollowers.setText(post.followers.size() + " followers");

        //FALTA BOTO D'ESBORRAR POST
    }
}
