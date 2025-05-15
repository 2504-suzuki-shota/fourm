package com.example.forum.controller.form;
import lombok.*;

import java.util.Date;

@Getter
@Setter
public class CommentForm {

    private int id;
    private String text;
    private int contentId;
    private Date createdDate;
    private Date updatedDate;
}
