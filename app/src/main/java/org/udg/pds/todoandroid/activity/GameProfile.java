package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Game;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameProfile extends AppCompatActivity {

    TodoApi mTodoService;

    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_profile);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        getGameInfo();

        mRecyclerView = findViewById(R.id.posts);
        mAdapter = new TRAdapter(this.getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button b = findViewById(R.id.add_Post);
        // This is the listener to the "Add Task" button
        b.setOnClickListener(view -> {
            // When we press the "Add Task" button, the AddTask activity is called, where
            // we can introduce the data of the new task
            Intent i = new Intent(GameProfile.this.getBaseContext(), AddPost.class);
            // We launch the activity with startActivityForResult because we want to know when
            // the launched activity has finished. In this case, when the AddTask activity has finished
            // we will update the list to show the new task.
            startActivityForResult(i, Global.RQ_ADD_POST);
        });
    }

    public void getGameInfo(){
        Long gameId = getIntent().getExtras().getLong("gameId");
        Call<Game> call = mTodoService.getGame(gameId.toString());

        call.enqueue(new Callback<Game>() {
            @Override
            public void onResponse(Call<Game> call, Response<Game> response) {
                if (response.isSuccessful()) {
                    showGameInfo(response.body());
                } else {
                    Toast.makeText(GameProfile.this.getBaseContext(), "Error reading game", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Game> call, Throwable t) {}
        });

    }

    public void showGameInfo(Game g){
        TextView gameName;
        TextView gameDesc;
        TextView gameCate;

        gameName = findViewById(R.id.game_name);
        gameDesc = findViewById(R.id.game_description);
        gameCate = findViewById(R.id.game_categories);

        gameName.setText(g.name);
        gameDesc.setText(g.description);
        gameCate.setText(g.categories.toString());
        new DownloadImageFromInternet((ImageView) findViewById(R.id.game_logo)).execute(g.image);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updatePostList();
    }

    public void showPostList(List<Post> tl) {
        mAdapter.clear();
        for (Post t : tl) {
            mAdapter.add(t);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Global.RQ_ADD_TASK) {
            this.updatePostList();
        }
    }

    public void updatePostList() {
        Long gameId = getIntent().getExtras().getLong("gameId");
        Call<List<Post>> call = mTodoService.getPosts(gameId.toString());

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful()) {
                    GameProfile.this.showPostList(response.body());
                } else {
                    Toast.makeText(GameProfile.this.getBaseContext(), "Error reading posts", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

            }
        });
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView username;
        TextView title;
        View view;

        PostViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            description = (TextView) itemView.findViewById(R.id.post_description);
            title = (TextView) itemView.findViewById(R.id.post_title);
            username = (TextView) itemView.findViewById(R.id.post_username);
        }
    }

    static class TRAdapter extends RecyclerView.Adapter<GameProfile.PostViewHolder> {

        List<Post> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public GameProfile.PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
            GameProfile.PostViewHolder holder = new GameProfile.PostViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(GameProfile.PostViewHolder holder, final int position) {
            holder.description.setText(list.get(position).description);
            holder.title.setText(list.get(position).title);
            holder.username.setText(list.get(position).username);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, String.format("Hey, I'm item %1d", position), duration);
                    toast.show();
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
        public void insert(int position, Post data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(Post data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void add(Post t) {
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

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }


}
