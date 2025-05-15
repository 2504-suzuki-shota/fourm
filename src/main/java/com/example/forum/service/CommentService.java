package com.example.forum.service;

import com.example.forum.controller.form.CommentForm;
import com.example.forum.controller.form.ReportForm;
import com.example.forum.repository.CommentRepository;
import com.example.forum.repository.entity.Comment;
import com.example.forum.repository.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<CommentForm> reports = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            CommentForm report = new CommentForm();
            Comment result = results.get(i);
            report.setId(result.getId());
            report.setText(result.getText());
            report.setContentId(result.getContentId());
            reports.add(report);
        }
        return reports;
    }

    /*
     * 返信の追加、更新
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
        //編集の時はidを指定しないと返信対象のレコードを特定できない→0の時はSERIALだから自動採番(登録できる)
        if(reqComment.getId() != 0){
            comment.setId(reqComment.getId());
        }
        comment.setContentId(reqComment.getContentId());
        comment.setText(reqComment.getText());
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
