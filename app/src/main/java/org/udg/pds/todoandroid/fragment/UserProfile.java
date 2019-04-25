
package org.udg.pds.todoandroid.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.GameProfile;
import org.udg.pds.todoandroid.entity.Game;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class UserProfile extends Fragment {

    TodoApi mTodoService;
    View rootView;
    RecyclerView mRecyclerView;
    private UserProfile.TRAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView=inflater.inflate(R.layout.content_user_profile, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();

        mRecyclerView = rootView.findViewById(R.id.subscribed_games);
        mAdapter = new UserProfile.TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    public void getUserInfo(){
        Call<User> call = mTodoService.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    showProfileUserInfo(response.body());
                    UserProfile.this.showGameList(response.body().games);
                } else {
                    Toast.makeText(UserProfile.this.getActivity().getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    public void showProfileUserInfo(User user){

        TextView userUsername;
        TextView userDescription;
        RatingBar userRating;

        userUsername = rootView.findViewById(R.id.user_username);
        userDescription = rootView.findViewById(R.id.user_description);
        userRating = rootView.findViewById(R.id.user_rating);

        userUsername.setText(user.name);
        userDescription.setText(user.description);
        userRating.setRating(user.valoration);
        new UserProfile.DownloadImageFromInternet((ImageView) rootView.findViewById(R.id.user_image)).execute(user.image);
    }

    public void showGameList(List<Game> tl) {
        mAdapter.clear();
        for (Game t : tl) {
            mAdapter.add(t);
        }
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView categories;
        TextView title;
        ImageView logo;
        View view;

        GameViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            categories = (TextView) itemView.findViewById(R.id.ListGameCategories);
            title = (TextView) itemView.findViewById(R.id.ListGameTitle);
            logo =  (ImageView) itemView.findViewById(R.id.listGameLogo);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<UserProfile.GameViewHolder> {

        List<Game> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public UserProfile.GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_layout, parent, false);
            UserProfile.GameViewHolder holder = new UserProfile.GameViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(UserProfile.GameViewHolder holder, final int position) {
            holder.categories.setText(list.get(position).categories.toString());
            holder.title.setText(list.get(position).name);
            new UserProfile.DownloadImageFromInternet((ImageView)holder.logo).execute(list.get(position).image);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(UserProfile.this.getActivity(), GameProfile.class);
                    i.putExtra("gameId",list.get(position).id);
                    startActivity(i);
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
        public void insert(int position, Game data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(Game data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void add(Game t) {
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
                Toast.makeText(getView().getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }


}
