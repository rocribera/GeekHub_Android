package org.udg.pds.todoandroid.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.fragment.FavoritesFragment;
import org.udg.pds.todoandroid.fragment.GamesDirectory;
import org.udg.pds.todoandroid.fragment.UserProfile;
import org.udg.pds.todoandroid.rest.TodoApi;

// FragmentActivity is a base class for activities that want to use the support-based Fragment and Loader APIs.
// http://developer.android.com/reference/android/support/v4/app/FragmentActivity.html
public class NavigationActivity extends AppCompatActivity {

    final String SWITCH_STATE = "switchState";
    TodoApi mTodoService;
    int switchReminder;
    ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));

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
        viewPager.setCurrentItem(itemId);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SWITCH_STATE, switchReminder);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private Fragment[] childFragments;

        private ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            childFragments = new Fragment[] {
                    new FavoritesFragment(),
                    new GamesDirectory(),
                    new UserProfile()
            };
        }

        @Override
        public Fragment getItem(int position) {
            return childFragments[position];
        }

        @Override
        public int getCount() {
            return childFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = getItem(position).getClass().getName();
            return title.subSequence(title.lastIndexOf(".") + 1, title.length());
        }
    }
}