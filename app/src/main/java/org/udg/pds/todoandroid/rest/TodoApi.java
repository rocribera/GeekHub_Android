package org.udg.pds.todoandroid.rest;

import org.udg.pds.todoandroid.entity.ChatInfo;
import org.udg.pds.todoandroid.entity.Game;
import org.udg.pds.todoandroid.entity.IdObject;
import org.udg.pds.todoandroid.entity.Post;
import org.udg.pds.todoandroid.entity.Task;
import org.udg.pds.todoandroid.entity.User;
import org.udg.pds.todoandroid.entity.UserLogin;
import org.udg.pds.todoandroid.entity.UserMessage;
import org.udg.pds.todoandroid.entity.UserRegister;
import org.udg.pds.todoandroid.entity.Game;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @POST("/users/register")
    Call<User> register(@Body UserRegister register);

    @POST("/tasks")
    Call<IdObject> addTask(@Body Task task);

    @GET("/tasks")
    Call<List<Task>> getTasks();

    @GET("/tasks/{id}")
    Call<Task> getTask(@Path("id") String id);

    @GET("/games/{id}/posts")
    Call<List<Post>> getPosts(@Path("id") String id);

    @GET("/posts/{id}/followers")
    Call<List<User>> getFollowers(@Path("id") String id);

    @POST("/games/{id}/posts")
    Call<String> addPost(@Path("id") String id, @Body Post post);

    @GET("/games/{id}")
    Call<Game> getGame(@Path("id") String id);

    @GET("/games")
    Call<List<Game>> getGames();

    @GET("/posts/{id}")
    Call<Post> getPostInfo(@Path("id") String id);

    @POST("users/me/posts/{id}")
    Call<String> toggleActivePost(@Path("id") String id);

    @DELETE("/posts/{id}")
    Call<String> deletePost(@Path("id") String id);

    @GET("/users/me")
    Call<User> getMe();

    @POST("/users/me/games/{id}")
    Call<String> bookmarkGame(@Path("id") String id);

    @DELETE("/users/me/games/{id}")
    Call<String> deleteBookmark(@Path("id") String id);

    @POST("/users/me/follows/{id}")
    Call<String> followPost(@Path("id") String id);

    @DELETE("/users/me/follows/{id}")
    Call<String> unfollowPost(@Path("id") String id);

    @GET("/users/{id}")
    Call<User> getUser(@Path("id") String id);

    @GET("/users/me/posts")
    Call<List<Post>> getMyPosts();

    @GET("/users/me/postsFollowing")
    Call<List<Post>> getMyPostsSubscribed();

    @GET("/users/{id}/posts")
    Call<List<Post>> getUserPosts(@Path("id") String id);

    @POST("/users/{id}/valoration")
    Call<String> ratingUser(@Path("id") String id, @Body String value);

    @POST("users/me/token")
    Call<String> sendToken(@Body String token);

    @POST("users/me")
    Call<String> updateUser(@Body User u);

    @POST("users/logout")
    Call<String> logout();

    @DELETE("users/me")
    Call<String> deleteMyUser();

    @GET("/message/me/{id}")
    Call<List<UserMessage>> getMyMessagesWithUser(@Path("id") String id);

    @POST("/message/me/{id}")
    Call<String> sendMessageToUser(@Path("id") String id,@Body UserMessage value);

    @GET("/message/me")
    Call<List<ChatInfo>> getAllMyChats();

    @GET("/message/me/open")
    Call<List<ChatInfo>> getMyOpenChats();

    @GET("/message/me/closed")
    Call<List<ChatInfo>> getMyClosedChats();

    @POST("/message/me/{id}/close")
    Call<String> closeChatWithUser(@Path("id") String id);

    @POST("/message/me/{id}/open")
    Call<ChatInfo> openChatWithUser(@Path("id") String id);

    @GET("/users/me/{id}/block")
    Call<String> checkUserBlock(@Path("id") String id);

    @POST("/users/me/{id}/block")
    Call<String> blockUserWithId(@Path("id") String id);

    @DELETE("/users/me/{id}/block")
    Call<String> unblockUserWithId(@Path("id") String id);

    @POST("/posts/{id}")
    Call<String> editPost(@Path("id") String id,@Body Post desc);
}

