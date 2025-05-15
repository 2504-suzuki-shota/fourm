package com.example.forum.controller.form;
import lombok.*;

import java.util.Date;

@Getter
@Setter
public class ReportForm {

    private int id;
    private String content;
    private Date createdDate;
    private Date updatedDate;
    private Date start;
    private Date goal;
}
