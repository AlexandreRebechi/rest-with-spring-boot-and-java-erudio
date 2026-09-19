package com.example.erudio.vo.request;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String role;
    private String content; //prompt

    public Message() {}

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(role, message.role) && Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content);
    }
}