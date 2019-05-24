package org.udg.pds.todoandroid.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.entity.UserMessage;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageListActivity extends AppCompatActivity {

    RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this.getApplication());
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        getMessages();

    }

    private void getMessages(){
        Long otherUserId = getIntent().getExtras().getLong("userId");
        Call<List<UserMessage>> call = mTodoService.getMyMessagesWithUser(otherUserId.toString());
        call.enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(Call<List<UserMessage>> call, Response<List<UserMessage>> response) {
                if(response.isSuccessful()){
                    mMessageAdapter.addAll(response.body());
                } else {
                    Toast.makeText(getBaseContext(), "Error reading messages", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserMessage>> call, Throwable t) {
            }
        });
    }

    public class MessageListAdapter extends RecyclerView.Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private Context mContext;
        private List<UserMessage> mMessageList = new ArrayList<>();

        public MessageListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            UserMessage message = (UserMessage) mMessageList.get(position);

            if (message.sendByMe) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageHolder(view);
            }

            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserMessage message = (UserMessage) mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
            }
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage mes) {
                messageText.setText(mes.message);

                // Format the stored timestamp into a readable String using method.
                timeText.setText(new Date(mes.createdAt).toString());
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText;
            ImageView profileImage;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
                nameText = (TextView) itemView.findViewById(R.id.text_message_name);
                profileImage = (ImageView) itemView.findViewById(R.id.image_message_profile);
            }

            void bind(UserMessage message) {
                messageText.setText(message.message);

                // Format the stored timestamp into a readable String using method.
                timeText.setText(new Date(message.createdAt).toString());

                nameText.setText(message.sender.name);
            }
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(Long messageId) {
            int position = 0;
            while(mMessageList.get(position).id != messageId){
                position++;
            }
            mMessageList.remove(position);
            notifyItemRemoved(position);
        }

        public void add(UserMessage t) {
            mMessageList.add(0,t);
            this.notifyItemInserted(mMessageList.size() - 1);
        }

        public void clear() {
            int size = mMessageList.size();
            mMessageList.clear();
            this.notifyItemRangeRemoved(0, size);
        }

        public void addAll(List<UserMessage> messages){
            mMessageList = messages;
            this.notifyItemRangeInserted(0,messages.size());
        }
    }
}
