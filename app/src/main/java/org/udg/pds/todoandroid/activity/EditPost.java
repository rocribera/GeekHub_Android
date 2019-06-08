package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPost extends AppCompatActivity {

    TodoApi mTodoService;
    Post editPost;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_post);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        EditText body = findViewById(R.id.body_text);
        TextView title = findViewById(R.id.title_textView);

        title.setText(extras.getString("title"));
        body.setText(extras.getString("body"));
        Long postId = extras.getLong("postId");

        Button save = findViewById(R.id.edit_button);
        // When the "Save" button is pressed, we make the call to the responder
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    String desc = body.getText().toString();

                    editPost = new Post();
                    editPost.title = title.getText().toString();
                    editPost.description = desc;

                    Call<String> call = mTodoService.editPost(postId.toString(),editPost);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()){
                                Intent i = new Intent(EditPost.this, PostPage.class);
                                i.putExtra("postId",postId);
                                finish();
                                PostPage.postPage.finish();
                                startActivity(i);
                            } else {
                                Toast.makeText(EditPost.this.getBaseContext(), "Error editing post", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                } catch (Exception ex) {
                    return;
                }
            }
        });
    }
}
