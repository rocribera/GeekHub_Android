package org.udg.pds.todoandroid.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.activity.PostPage;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.rest.TodoApi;
import org.udg.pds.todoandroid.util.Global;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imartin on 12/02/16.
 */
public class UserProfilePosts extends Fragment {

    TodoApi mTodoService;

    RecyclerView mRecyclerView;
    private TRAdapter mAdapter;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.getArguments().getInt("type")==1) this.updateOwnPostList();
        else this.updatePostSubscribedList();
    }

    public void showPostsList(List<Post> tl) {
        mAdapter.clear();
        for (Post t : tl) {
            if(t.active || this.getArguments().getInt("type")==1) mAdapter.add(t);
        }
    }

    public void updateOwnPostList() {
        Call<List<Post>> call;
        if (getActivity().getIntent().hasExtra("userId")) {
            Long userId = getActivity().getIntent().getExtras().getLong("userId");
            call = mTodoService.getUserPosts(userId.toString());
        } else {
            call = mTodoService.getMyPosts();
        }
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.isSuccessful()){
                    UserProfilePosts.this.showPostsList(response.body());
                } else {
                    Toast.makeText(UserProfilePosts.this.getContext(), "Error reading user", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
            }
        });
    }

    public void updatePostSubscribedList() {
        Call<List<Post>> call = mTodoService.getMyPostsSubscribed();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.isSuccessful()){
                    UserProfilePosts.this.showPostsList(response.body());
                } else {
                    Toast.makeText(UserProfilePosts.this.getContext(), "Error reading user", Toast.LENGTH_LONG).show();
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
            title = (TextView) itemView.findViewById(R.id.post_title_text);
            username = (TextView) itemView.findViewById(R.id.post_username);
        }
    }

    class TRAdapter extends RecyclerView.Adapter<PostViewHolder> {

        List<Post> list = new ArrayList<>();
        Context context;

        public TRAdapter(Context context) {
            this.context = context;
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
            PostViewHolder holder = new PostViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(PostViewHolder holder, final int position) {
            holder.description.setText(list.get(position).description);
            holder.title.setText(list.get(position).title);
            holder.username.setText(list.get(position).username);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(UserProfilePosts.this.getActivity(), PostPage.class);
                    i.putExtra("postId",list.get(position).id);
                    //
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, Global.RQ_DELETE_POST);
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
        public void remove(Long postId) {
            int position = 0;
            while(list.get(position).id != postId){
                position++;
            }
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
}

