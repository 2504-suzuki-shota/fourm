package com.example.forum.controller.form;
import lombok.*;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class ReportForm {

    private int id;
    @NotBlank
    private String content;
    private Date createdDate;
    private Date updatedDate;
    private String start;
    private String end;
    private String stringCreatedDate;
    private String stringUpdatedDate;
}