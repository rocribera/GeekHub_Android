package org.udg.pds.todoandroid.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserMessage;
import org.udg.pds.todoandroid.rest.TodoApi;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageListActivity extends AppCompatActivity {

    RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private TodoApi mTodoService;
    static public Long active;
    private Long myId;
    public boolean closed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        mTodoService = ((TodoApp) this.getApplication()).getAPI();
        myId = getIntent().getExtras().getLong("myId");
        getOtherUser();

        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(this.getApplication());
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));

        Button sendButton = (Button) findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText chatbox = (EditText)findViewById(R.id.edittext_chatbox);
                if(!chatbox.getText().toString().isEmpty()){
                    UserMessage um = new UserMessage();
                    um.createdAt = new Date();
                    um.message=chatbox.getText().toString();
                    um.senderId = myId;
                    chatbox.setText("");
                    Long userId = getIntent().getExtras().getLong("userId");
                    Call<String> postCall = mTodoService.sendMessageToUser(userId.toString(),um);
                    postCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> postCall, Response<String> response) {
                            if (response.isSuccessful()) {
                                if(response.body().equals("Closed")) closeChat();
                                else mMessageAdapter.add(um);
                            }
                        }
                        @Override
                        public void onFailure(Call<String> postCall, Throwable t) {
                            Toast.makeText(MessageListActivity.this.getBaseContext(), "An error occurred! Try again later", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        ConstraintLayout constraint = (ConstraintLayout) findViewById(R.id.chat_constraint);
        constraint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),OtherUserProfile.class);
                i.putExtra("userId",getIntent().getLongExtra("userId",0));
                startActivity(i);
            }
        });

        ImageView buttonSettings = (ImageView) findViewById(R.id.chat_settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupSettings(view);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver,
                new IntentFilter("NewMessage"));

        LocalBroadcastManager.getInstance(this).registerReceiver(closeChatReceiver,
                new IntentFilter("CloseChat"));
    }

    public void showPopupSettings(View v) {
        PopupMenu pm = new PopupMenu(this.getBaseContext(), v);
        pm.getMenuInflater().inflate(R.menu.popup_menu_chat,pm.getMenu());

        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.action_chat_closeChat:
                        LayoutInflater layoutInflater = (LayoutInflater) MessageListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View popupDelete = layoutInflater.inflate(R.layout.delete_confirmation, null);
                        TextView deleteText = popupDelete.findViewById(R.id.delete_text);
                        deleteText.setText("Are you sure you want to close this chat?");
                        PopupWindow popupWindow = new PopupWindow(MessageListActivity.this);
                        popupWindow.setContentView(popupDelete);
                        popupWindow.showAtLocation(popupDelete, Gravity.CENTER,0,0);
                        Button deleteConfirm = popupDelete.findViewById(R.id.delete_confirm);
                        deleteConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Long userId = getIntent().getExtras().getLong("userId");
                                Call<String> postCall = mTodoService.closeChatWithUser(userId.toString());
                                postCall.enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> postCall, Response<String> response) {
                                        if (response.isSuccessful()) {
                                            closeChat();
                                            popupWindow.dismiss();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<String> postCall, Throwable t) {
                                        Toast.makeText(MessageListActivity.this.getBaseContext(), "An error occurred! Try again later", Toast.LENGTH_LONG).show();
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

    private void closeChat(){
        if(!closed) {
            EditText chatbox = (EditText) findViewById(R.id.edittext_chatbox);
            chatbox.setEnabled(false);
            chatbox.setHint("Chat is closed!");
            chatbox.setText("");
            ImageView buttonSettings = (ImageView) findViewById(R.id.chat_settings);
            buttonSettings.setEnabled(false);
            closed = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = getIntent().getExtras().getLong("userId");
        this.getMessages();
        if(!getIntent().getBooleanExtra("active",true)){
            closeChat();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = Long.valueOf(0);
    }

    private void getOtherUser(){
        active = getIntent().getExtras().getLong("userId");
        Call<User> call = mTodoService.getUser(active.toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    User u = response.body();
                    ((TextView)findViewById(R.id.chat_username)).setText(u.name);
                    if (u.updatedImage)
                        showImage(u);
                    else
                        new MessageListActivity.DownloadImageFromInternet((ImageView)findViewById(R.id.chat_userlogo)).execute(u.image);
                } else {
                    Toast.makeText(getBaseContext(), "Error reading messages", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });

    }

    private void showImage(User u)
    {
        Call<ResponseBody> call = mTodoService.getImage(u.image);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Bitmap bi = BitmapFactory.decodeStream(response.body().byteStream());
                    ImageView iv=findViewById(R.id.chat_userlogo);
                    iv.setImageBitmap(bi);
                } else {
                    Toast.makeText(getBaseContext(), "Error downloading profile image", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    public void getMessages(){
        mMessageAdapter.clear();
        Call<List<UserMessage>> call = mTodoService.getMyMessagesWithUser(active.toString());
        call.enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(Call<List<UserMessage>> call, Response<List<UserMessage>> response) {
                if(response.isSuccessful()){
                    if(!response.body().isEmpty()) mMessageAdapter.addAll(response.body());
                } else {
                }
            }

            @Override
            public void onFailure(Call<List<UserMessage>> call, Throwable t) {
            }
        });
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UserMessage um = new UserMessage();
            um.message = intent.getStringExtra("message");
            um.createdAt= new Date();
            um.senderId = intent.getLongExtra("senderId",0);
            mMessageAdapter.add(um);
            if(closed) {
                EditText chatbox = (EditText) findViewById(R.id.edittext_chatbox);
                chatbox.setEnabled(true);
                chatbox.setHint("Enter message");
                ImageView buttonSettings = (ImageView) findViewById(R.id.chat_settings);
                buttonSettings.setEnabled(true);
                closed = false;
            }
        }
    };

    private BroadcastReceiver closeChatReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            closeChat();
        }
    };

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

            if (message.senderId == myId) {
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
                messageText.setTextColor(Color.BLACK);
                // Format the stored timestamp into a readable String using method.
                String time = "";
                if(mes.createdAt.getHours()<10) time+="0";
                time+=mes.createdAt.getHours()+":";
                if(mes.createdAt.getMinutes()<10) time+="0";
                time+=mes.createdAt.getMinutes();
                timeText.setText(time);
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText;
            ImageView profileImage;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage message) {
                messageText.setText(message.message);
                messageText.setTextColor(Color.BLACK);
                // Format the stored timestamp into a readable String using method.
                String time = "";
                if(message.createdAt.getHours()<10) time+="0";
                time+=message.createdAt.getHours()+":";
                if(message.createdAt.getMinutes()<10) time+="0";
                time+=message.createdAt.getMinutes();
                timeText.setText(time);
            }
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(UserMessage userMessage) {
            int position = 0;
            while(mMessageList.get(position) != userMessage){
                position++;
            }
            mMessageList.remove(position);
            notifyItemRemoved(position);
        }

        public void add(UserMessage t) {
            mMessageList.add(t);
            this.notifyItemInserted(mMessageList.size() - 1);
            mMessageRecycler.smoothScrollToPosition(mMessageList.size()-1);
        }

        public void clear() {
            int size = mMessageList.size();
            mMessageList.clear();
            this.notifyItemRangeRemoved(0, size);
        }

        public void addAll(List<UserMessage> messages){
            mMessageList = messages;
            this.notifyItemRangeInserted(0,messages.size());
            mMessageRecycler.smoothScrollToPosition(mMessageList.size()-1);
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
            }
            catch (Exception e) {}
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}
