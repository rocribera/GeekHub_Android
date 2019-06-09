package org.udg.pds.todoandroid.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.activity.MessageListActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment {


    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.content_favorites, container, false);
        Button button = (Button)root.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritesFragment.this.getActivity(), MessageListActivity.class);
                i.putExtra("userId",(long)1);
                i.putExtra("myId", (long)2);
                startActivity(i);
            }
        });
        Button button2 = (Button)root.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FavoritesFragment.this.getActivity(), MessageListActivity.class);
                i.putExtra("userId",(long)2);
                i.putExtra("myId", (long)1);
                startActivity(i);
            }
        });
        return root;
    }

}
