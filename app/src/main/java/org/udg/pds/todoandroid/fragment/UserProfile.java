
package org.udg.pds.todoandroid.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
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


public class UserProfile extends Fragment {

    TodoApi mTodoService;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView=inflater.inflate(R.layout.content_user_profile, container, false);

        final FrameLayout content = rootView.findViewById(R.id.userProfileContent);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.userProfileContent, new UserProfileGames())
                .commit();
        Button buttonGames = (Button)rootView.findViewById(R.id.userProfileGames);
        Button buttonOwnPosts = (Button)rootView.findViewById(R.id.userProfileOwnPosts);
        Button buttonPostsSubscribed = (Button)rootView.findViewById(R.id.userProfilePosts);
        buttonGames.setTextColor(Color.BLACK);
        buttonOwnPosts.setTextColor(Color.LTGRAY);
        buttonPostsSubscribed.setTextColor(Color.LTGRAY);
        buttonGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGames.setTextColor(Color.BLACK);
                buttonOwnPosts.setTextColor(Color.LTGRAY);
                buttonPostsSubscribed.setTextColor(Color.LTGRAY);
                content.removeAllViews();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.userProfileContent, new UserProfileGames())
                        .commit();
            }
        });
        buttonOwnPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGames.setTextColor(Color.LTGRAY);
                buttonOwnPosts.setTextColor(Color.BLACK);
                buttonPostsSubscribed.setTextColor(Color.LTGRAY);
                content.removeAllViews();
                UserProfilePosts fragment = new UserProfilePosts();
                Bundle bundle = new Bundle();
                bundle.putInt("type",1);
                fragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.userProfileContent, fragment)
                        .commit();
            }
        });
        buttonPostsSubscribed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGames.setTextColor(Color.LTGRAY);
                buttonOwnPosts.setTextColor(Color.LTGRAY);
                buttonPostsSubscribed.setTextColor(Color.BLACK);
                content.removeAllViews();
                UserProfilePosts fragment = new UserProfilePosts();
                Bundle bundle = new Bundle();
                bundle.putInt("type",2);
                fragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.userProfileContent, fragment)
                        .commit();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();
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
                    //UserProfile.this.showGameList(response.body().games);
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