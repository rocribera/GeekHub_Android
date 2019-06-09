package org.udg.pds.todoandroid.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.MessageListActivity;
import org.udg.pds.todoandroid.entity.ChatInfo;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by imartin on 12/02/16.
 */
public class OpenChatsRecycle extends Fragment {

    TodoApi mTodoService;

    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;
    private int opened;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.recycle_view, container, false);
    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();

        mRecyclerView = getView().findViewById(R.id.recycleViewGeneral);
        mAdapter = new TRAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        opened = getArguments().getInt("opened");
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateOpenChatsList();
    }

    public void showOpenChatsList(List<ChatInfo> tl) {
        mAdapter.clear();
        for (ChatInfo t : tl) {
            if(opened==1&&t.chatActive || opened==0&&!t.chatActive) mAdapter.add(t);
        }
    }

    public void updateOpenChatsList() {
        Call<List<ChatInfo>> call = null;
        if(opened==1) call = mTodoService.getMyOpenChats();
        else if(opened==0) call = mTodoService.getMyClosedChats();
        call.enqueue(new Callback<List<ChatInfo>>() {
            @Override
            public void onResponse(Call<List<ChatInfo>> call, Response<List<ChatInfo>> response) {
                if (response.isSuccessful()) {
                    OpenChatsRecycle.this.showOpenChatsList(response.body());
                } else {
                    Toast.makeText(OpenChatsRecycle.this.getContext(), "Error reading chats", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChatInfo>> call, Throwable t) {

            }
        });
    }

    static class ChatInfoViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView logo;
        View view;

        ChatInfoViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = (TextView) itemView.findViewById(R.id.openChatsName);
            logo =  (ImageView) itemView.findViewById(R.id.openChatsImage);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<ChatInfoViewHolder> {

        List<ChatInfo> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public ChatInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_info, parent, false);
            ChatInfoViewHolder holder = new ChatInfoViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(ChatInfoViewHolder holder, final int position) {
            holder.name.setText(list.get(position).otherUser.name);
            new DownloadImageFromInternet((ImageView) holder.logo).execute(list.get(position).otherUser.image);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(OpenChatsRecycle.this.getActivity(), MessageListActivity.class);
                    i.putExtra("userId", list.get(position).otherUser.id);
                    i.putExtra("myId", list.get(position).myUserId);
                    i.putExtra("active", list.get(position).chatActive);
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
        public void insert(int position, ChatInfo data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(ChatInfo data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void add(ChatInfo t) {
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
                Toast.makeText(getContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}

