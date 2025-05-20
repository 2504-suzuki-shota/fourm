package com.example.forum.service;

import com.example.forum.controller.form.CommentForm;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.entity.Comment;
import com.example.forum.repository.entity.Report;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;

    /*
     * 返信全件取得処理
     */
    public List<CommentForm> findAllComment() {
        List<Comment> results = commentRepository.findAllByOrderByIdDesc();
        List<CommentForm> comments = setCommentForm(results);
        return comments;
    }

    /*
     * DBから取得したデータをCommentに設定
     */
    private List<CommentForm> setCommentForm(List<Comment> results) {
        List<CommentForm> comments = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            CommentForm comment = new CommentForm();
            Comment result = results.get(i);
            comment.setId(result.getId());
            comment.setText(result.getText());
            comment.setContentId(result.getContentId());
            comment.setCreatedDate(result.getCreatedDate());
            comment.setUpdatedDate(result.getUpdatedDate());
            //時間をDate→String型に変えたverもセットしとく
            comment.setStringCreatedDate(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(comment.getCreatedDate()));
            comment.setStringUpdatedDate(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(comment.getUpdatedDate()));
            comments.add(comment);
        }
        return comments;
    }


    /*
     * 返信の登録、更新
     */
    public void saveComment(CommentForm reqComment) {
        //saveメソッドの引数はEntity型だからCommentForm型をComment型に変換できるメソッドに飛ばす
        Comment saveComment = setCommentEntity(reqComment);
        //saveメソッドでDBに登録または更新
        commentRepository.save(saveComment);
    }

    /*
     * リクエストから取得した情報(CommentForm)をEntity(Comment)型に変換
     */
    private Comment setCommentEntity(CommentForm reqComment) {
        Comment comment = new Comment();
        //編集の時→idを指定しないと返信対象のレコードを特定できない
        //ちなみに登録の時→idをセットしない→id=0になる→SERIALだから自動採番で登録になる
        if(reqComment.getId() != 0){
            comment.setId(reqComment.getId());
        }
        comment.setContentId(reqComment.getContentId());
        comment.setText(reqComment.getText());
        comment.setUpdatedDate(reqComment.getUpdatedDate());
        if(StringUtils.isBlank(reqComment.getStringCreatedDate())) {
            //登録の時
            comment.setCreatedDate(reqComment.getCreatedDate());
        } else {
            //編集の時(本当は上のifとまとめた方がいいが、復習用に取っておく)
            try {
                //stringCreatedDateをcreatedDate（String→Dateに型変換）
                Date createdDate = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(reqComment.getStringCreatedDate());
                comment.setCreatedDate(createdDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return comment;
    }

    /*
     * 返信の削除
     */
    public void deleteComment(Integer id) {
        //deleteByIdはJpaRepositoryに搭載済みだから良しなにやってくれる
        commentRepository.deleteById(id);
    }

    /*
     * 返信の編集対象のレコード1件取得
     */
    public CommentForm editComment(Integer id) {
        List<Comment> results = new ArrayList<>();
        Comment comment = (Comment) commentRepository.findById(id).orElse(null);
        results.add(comment);
        List<CommentForm> comments = setCommentForm(results);
        return comments.get(0);
    }
}