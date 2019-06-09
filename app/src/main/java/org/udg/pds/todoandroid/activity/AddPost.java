package org.udg.pds.todoandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.udg.pds.todoandroid.R;
import org.udg.pds.todoandroid.TodoApp;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddPost extends AppCompatActivity implements Callback<String> {

    TodoApi mTodoService;
    Post newPost;

    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if (response.isSuccessful()) {
            Intent intent = getIntent();
            Gson gson = new Gson();
            intent.putExtra("post", gson.toJson(newPost));
            setResult(RESULT_OK,intent);
            finish();
        } else {
            Toast.makeText(AddPost.this.getBaseContext(), "Error adding post", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Button save = (Button) findViewById(R.id.ap_save_button);
        // When the "Save" button is pressed, we make the call to the responder
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView title = (TextView) findViewById(R.id.ap_title_text);
                TextView description = (TextView) findViewById(R.id.ap_description);

                try {
                    String tit = title.getText().toString();
                    String desc = description.getText().toString();
                    newPost = new Post();
                    newPost.title = tit;
                    newPost.description = desc;
                    Long gameId = getIntent().getExtras().getLong("gameId");
                    Call<String> call = mTodoService.addPost(gameId.toString(),newPost);
                    call.enqueue(AddPost.this);
                } catch (Exception ex) {
                    return;
                }

            }
        });
    }
}
