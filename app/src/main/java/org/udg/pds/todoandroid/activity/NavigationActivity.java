package org.udg.pds.todoandroid.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.fragment.FavoritesFragment;
import org.udg.pds.todoandroid.fragment.GamesDirectory;
import org.udg.pds.todoandroid.fragment.TaskList;
import org.udg.pds.todoandroid.fragment.UserProfile;
import org.udg.pds.todoandroid.rest.TodoApi;

// FragmentActivity is a base class for activities that want to use the support-based Fragment and Loader APIs.
// http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
public class NavigationActivity extends AppCompatActivity {

    final String SWITCH_STATE = "switchState";
    TodoApi mTodoService;
    int switchReminder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                item -> {
                    switchView(item.getItemId());
                    return true;
                });

        if (getIntent().hasExtra("goToProfile") && getIntent().getExtras().getBoolean("goToProfile")) {
            bottomNavigationView.setSelectedItemId(R.id.action_profile);
            getIntent().putExtra("goToProfile", false);
        } else if (savedInstanceState==null){
            bottomNavigationView.setSelectedItemId(R.id.action_games);
        }
        else {
            bottomNavigationView.setSelectedItemId(savedInstanceState.getInt(SWITCH_STATE,R.id.action_games));
        }
        switchView(bottomNavigationView.getSelectedItemId());
    }

    private void switchView(int itemId) {
        final FrameLayout content = findViewById(R.id.main_content);
        switch (itemId) {
            case R.id.action_favorites:
                content.removeAllViews();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, new FavoritesFragment())
                        .commit();
                switchReminder=R.id.action_favorites;
                break;
            case R.id.action_games:
                content.removeAllViews();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, new GamesDirectory())
                        .commit();
                switchReminder=R.id.action_games;
                break;
            case R.id.action_profile:
                content.removeAllViews();
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, new UserProfile())
                        .commit();
                switchReminder=R.id.action_profile;
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SWITCH_STATE, switchReminder);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}