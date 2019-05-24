package org.udg.pds.todoandroid.entity;

public class UserMessage {
    public long id;
    public String message;
    public boolean sendByMe;
    public User sender;
    public long createdAt;
}
