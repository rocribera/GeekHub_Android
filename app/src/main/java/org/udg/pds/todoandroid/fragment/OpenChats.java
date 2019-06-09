package org.udg.pds.todoandroid.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import org.udg.pds.todoandroid.R;


public class OpenChats extends Fragment {

    private boolean openChat = true;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView=inflater.inflate(R.layout.open_chats, container, false);
        OpenChatsRecycle fragment = new OpenChatsRecycle();
        updateChats();
        Button buttonOpen = (Button)rootView.findViewById(R.id.chatsOpenButtonOpen);
        Button buttonClosed = (Button)rootView.findViewById(R.id.chatsOpenButtonClosed);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChat = true;
                updateChats();
            }
        });
        buttonClosed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChat = false;
                updateChats();
            }
        });

        return rootView;
    }

    private void updateChats(){
        Button buttonOpen = (Button)rootView.findViewById(R.id.chatsOpenButtonOpen);
        Button buttonClosed = (Button)rootView.findViewById(R.id.chatsOpenButtonClosed);
        final FrameLayout content = rootView.findViewById(R.id.chatOpenFrame);
        if(openChat){
            buttonOpen.setTextColor(Color.BLACK);
            buttonClosed.setTextColor(Color.LTGRAY);
        }
        else {
            buttonOpen.setTextColor(Color.LTGRAY);
            buttonClosed.setTextColor(Color.BLACK);
        }
        content.removeAllViews();
        OpenChatsRecycle fragment = new OpenChatsRecycle();
        Bundle bundle = new Bundle();
        if(openChat) bundle.putInt("opened",1);
        else bundle.putInt("opened",0);
        fragment.setArguments(bundle);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.chatOpenFrame, fragment)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateChats();
    }
}
