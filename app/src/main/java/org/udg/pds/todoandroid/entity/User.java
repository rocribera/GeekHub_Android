package org.udg.pds.todoandroid.entity;

import java.util.List;


public class User {
  public long id;
  public String name;
  public String image;
  public String description;
  public List<Game> games;
  public List<Post> followedPosts;
  public float valoration;
}
