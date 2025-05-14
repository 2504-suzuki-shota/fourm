package com.example.forum.controller.form;
import lombok.*;

@Getter
@Setter
public class CommentForm {

    private int id;
    private String text;
    private int contentId;
}
