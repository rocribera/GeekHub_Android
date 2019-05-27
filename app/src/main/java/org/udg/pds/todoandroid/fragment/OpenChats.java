package org.udg.pds.todoandroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.MessageListActivity;
import org.udg.pds.todoandroid.entity.ChatInfo;
import org.udg.pds.todoandroid.entity.Game;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OpenChats extends Fragment {
    TodoApi mTodoService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView=inflater.inflate(R.layout.open_chats, container, false);
        OpenChatsRecycle fragment = new OpenChatsRecycle();
        Bundle bundle = new Bundle();
        bundle.putInt("opened",1);
        fragment.setArguments(bundle);
        final FrameLayout content = rootView.findViewById(R.id.chatOpenFrame);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.chatOpenFrame, fragment)
                .commit();
        Button buttonOpen = (Button)rootView.findViewById(R.id.chatsOpenButtonOpen);
        Button buttonClosed = (Button)rootView.findViewById(R.id.chatsOpenButtonClosed);
        buttonOpen.setTextColor(Color.BLACK);
        buttonClosed.setTextColor(Color.LTGRAY);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonOpen.setTextColor(Color.BLACK);
                buttonClosed.setTextColor(Color.LTGRAY);
                content.removeAllViews();
                OpenChatsRecycle fragment = new OpenChatsRecycle();
                Bundle bundle = new Bundle();
                bundle.putInt("opened",1);
                fragment.setArguments(bundle);
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.chatOpenFrame, fragment)
                        .commit();
            }
        });
        buttonClosed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonOpen.setTextColor(Color.LTGRAY);
                buttonClosed.setTextColor(Color.BLACK);
                content.removeAllViews();
                OpenChatsRecycle fragment = new OpenChatsRecycle();
                Bundle bundle = new Bundle();
                bundle.putInt("opened",0);
                fragment.setArguments(bundle);
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.chatOpenFrame, fragment)
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
    }
}
