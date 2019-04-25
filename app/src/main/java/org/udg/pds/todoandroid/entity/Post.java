package org.udg.pds.todoandroid.entity;

import java.util.List;

public class Post {
    public Long id;
    public Long userId;
    public String username;
    public String title;
    public Boolean active;
    public String description;
    public List<User> followers;
}
