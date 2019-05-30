package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Game;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersList extends AppCompatActivity {
    TodoApi mTodoService;

    RecyclerView mRecyclerView;
    private FollowersList.TRAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.followers_list);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        
        mRecyclerView = findViewById(R.id.followers_recycleView);
        mAdapter = new FollowersList.TRAdapter(this.getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateUserList();
    }

    public void showUserList(List<User> tl) {
        mAdapter.clear();
        for (User t : tl) {
            mAdapter.add(t);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Global.RQ_ADD_POST) {
            if(resultCode==Global.RQ_ADD_POST){
                Gson gson = new Gson();
                User user = gson.fromJson(getIntent().getStringExtra("user"), User.class);
                mAdapter.add(user);
            }
        }
        else if(requestCode == Global.RQ_DELETE_POST){
            if(resultCode==Global.RQ_DELETE_POST) {
                mAdapter.remove(Long.parseLong(data.getData().toString()));
            }
        }
    }

    public void updateUserList() {
        Long postId = getIntent().getExtras().getLong("postId");
        Call<List<User>> call = mTodoService.getFollowers(postId.toString());

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    FollowersList.this.showUserList(response.body());
                } else {
                    Toast.makeText(FollowersList.this.getBaseContext(), "Error reading Users", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        Button username;
        Button chat;
        View view;

        UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            chat = itemView.findViewById(R.id.chatButton);
            username = itemView.findViewById(R.id.usernameButton);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<FollowersList.UserViewHolder> {

        List<User> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public FollowersList.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.followerslist_layout, parent, false);
            FollowersList.UserViewHolder holder = new FollowersList.UserViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(FollowersList.UserViewHolder holder, final int position) {
            holder.username.setText(list.get(position).name);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(FollowersList.this, OtherUserProfile.class);
                    i.putExtra("UserId",list.get(position).id);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, Global.RQ_DELETE_POST);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {

            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, User data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(Long UserId) {
            int position = 0;
            while(list.get(position).id != UserId){
                position++;
            }
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void add(User t) {
            list.add(t);
            this.notifyItemInserted(list.size() - 1);
        }

        public void clear() {
            int size = list.size();
            list.clear();
            this.notifyItemRangeRemoved(0, size);
        }
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

        protected void onUserExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
