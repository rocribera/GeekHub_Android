
package org.udg.pds.todoandroid.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.Login;
import org.udg.pds.todoandroid.activity.ProfileSettings;
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
        getChildFragmentManager()
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
                getChildFragmentManager()
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
                getChildFragmentManager()
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
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.userProfileContent, fragment)
                        .commit();
            }
        });

        ImageView buttonSettings = (ImageView) rootView.findViewById(R.id.settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
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

    public void showPopup(View v) {
        PopupMenu pm = new PopupMenu(this.getContext(), v);
        pm.getMenuInflater().inflate(R.menu.popup_menu_settings_profile,pm.getMenu());

        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.modify_profile:
                        Intent i = new Intent(UserProfile.this.getActivity(), ProfileSettings.class);
                        startActivityForResult(i,1);
                        break;
                    case R.id.log_out:
                        Call<String> call = mTodoService.logout();
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if(response.isSuccessful()){
                                    Intent intent = new Intent(UserProfile.this.getActivity(), Login.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(UserProfile.this.getActivity().getBaseContext(), "Error logging out", Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                            }
                        });
                        break;
                    case R.id.delete_account:
                        LayoutInflater layoutInflater = (LayoutInflater) UserProfile.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View popupDelete = layoutInflater.inflate(R.layout.delete_confirmation, null);
                        TextView deleteText = popupDelete.findViewById(R.id.delete_text);
                        deleteText.setText("Are you sure you want to delete your account?");
                        PopupWindow popupWindow = new PopupWindow(UserProfile.this.getActivity());
                        popupWindow.setContentView(popupDelete);
                        popupWindow.showAtLocation(popupDelete, Gravity.CENTER,0,0);
                        Button deleteConfirm = popupDelete.findViewById(R.id.delete_confirm);
                        deleteConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Call<String> call = mTodoService.deleteMyUser();
                                call.enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        if(response.isSuccessful()){
                                            popupWindow.dismiss();
                                            Intent intent = new Intent(UserProfile.this.getActivity(), Login.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(UserProfile.this.getContext(), "Error deleting user", Toast.LENGTH_LONG).show();
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
                        break;
                }
                return true;
            }
        });

        pm.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(UserProfile.this.getActivity().getBaseContext(), "Changes saved!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void getUserInfo(){
        Call<User> call = mTodoService.getMe();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    showProfileUserInfo(response.body());
                } else {
                    Toast.makeText(UserProfile.this.getActivity().getBaseContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    public void showProfileUserInfo(User user)
    {
        TextView userUsername;
        TextView userDescription;
        RatingBar userRating;

        userUsername = rootView.findViewById(R.id.user_username);
        userDescription = rootView.findViewById(R.id.user_description);
        userRating = rootView.findViewById(R.id.user_rating);

        userUsername.setText(user.name);
        userDescription.setText(user.description);
        userRating.setRating(user.valoration);
        if(user.image!=null) new UserProfile.DownloadImageFromInternet((ImageView) rootView.findViewById(R.id.user_image)).execute(user.image);
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
