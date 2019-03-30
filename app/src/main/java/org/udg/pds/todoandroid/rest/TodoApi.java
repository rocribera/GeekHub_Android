package org.udg.pds.todoandroid.rest;

import org.udg.pds.todoandroid.entity.Game;
import org.udg.pds.todoandroid.entity.IdObject;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.entity.Task;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserLogin;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

/**
 * Created by imartin on 13/02/17.
 */
public interface TodoApi {
  @POST("/users/login")
  Call<User> login(@Body UserLogin login);

  @GET("/users/check")
  Call<String> check();

  @POST("/tasks")
  Call<IdObject> addTask(@Body Task task);

  @GET("/tasks")
  Call<List<Task>> getTasks();

  @GET("/tasks/{id}")
  Call<Task> getTask(@Path("id") String id);

  @GET("/games/{id}/posts")
  Call<List<Post>> getPosts(@Path("id") String id);

  @GET("/games/{id}")
  Call<Game> getGame(@Path("id") String id);

}

