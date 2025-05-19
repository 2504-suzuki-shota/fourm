package com.example.forum.controller.form;
import lombok.*;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class CommentForm {

    private int id;
    @NotBlank
    private String text;
    private int contentId;
    private Date createdDate;
    private Date updatedDate;
    private String stringCreatedDate;
    private String stringUpdatedDate;
}