package com.example.forum.controller;

import com.example.forum.controller.form.CommentForm;
import com.example.forum.controller.form.ReportForm;
import com.example.forum.repository.entity.Comment;
import com.example.forum.service.CommentService;
import com.example.forum.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class ForumController {
    @Autowired
    ReportService reportService;
    @Autowired
    CommentService commentService;

    /*
     * 投稿内容表示処理
     */
    @GetMapping
    public ModelAndView top(@ModelAttribute("date") ReportForm date) {
        ModelAndView mav = new ModelAndView();
        // 返信欄用の空のformを準備
        CommentForm commentForm = new CommentForm();
        // 投稿を全件取得
        List<ReportForm> contentData = reportService.findAllReport(date.getStart(), date.getEnd());
        // 返信を全件取得
        List<CommentForm> replyData = commentService.findAllComment();
        // 画面遷移先を指定
        mav.setViewName("/top");
        //以下は全部表示させたい
        // 日付の入ったオブジェクトをviewに運ぶ
        mav.addObject("date", date);
        // 空の返信欄のオブジェクトをviewに運ぶ
        mav.addObject("commentModel", commentForm);
        // 投稿データオブジェクトをviewに運ぶ
        mav.addObject("contents", contentData);
        // 返信データオブジェクトをviewに運ぶ
        mav.addObject("replies", replyData);
        return mav;
    }
    /*
     * 新規投稿画面表示
     */
    @GetMapping("/new")
    public ModelAndView newContent() {
        ModelAndView mav = new ModelAndView();
        // form用の空のformを準備
        ReportForm reportForm = new ReportForm();
        // 画面遷移先を指定
        mav.setViewName("/new");
        // 準備した空のFormを保管
        mav.addObject("formModel", reportForm);
        return mav;
    }
    /*
     * 新規投稿の登録
     */
    @PostMapping("/add")
    public ModelAndView addContent(@ModelAttribute("formModel") ReportForm reportForm){
        //今の時間をセット
        reportForm.setCreatedDate(new Date());
        reportForm.setUpdatedDate(reportForm.getCreatedDate());
        // 投稿をテーブルに登録したい
        reportService.saveReport(reportForm);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }

    /*
     * 投稿削除処理
     */
    @DeleteMapping("/delete/{id}")
    //@PathVariableで、formタグ内のaction属性に記述されているURLのパラメータを取得できる
    //→content.idが取得できる
    public ModelAndView deleteContent(@PathVariable Integer id) {
        // テーブルから削除する投稿を特定できるidを運ぶ
        reportService.deleteReport(id);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }

    /*
     * 編集画面表示処理
     */
    @GetMapping("/edit/{id}")
    public ModelAndView editContent(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();
        // 編集する投稿を取得
        ReportForm report = reportService.editReport(id);
        // 編集する投稿をセット
        mav.addObject("formModel", report);
        // 画面遷移先を指定
        mav.setViewName("/edit");
        return mav;
    }

    /*
     * 編集処理
     */
    @PutMapping("/update/{id}")
    public ModelAndView updateContent (@PathVariable Integer id,
                                       @ModelAttribute("formModel") ReportForm report) {
        // UrlParameterのidを更新するformにセット→なくてもいける→@PathVariableで入っちゃってる
        //report.setId(id);
        //UpdatedDateに今の時間をセット
        report.setUpdatedDate(new Date());
        // 編集した投稿を更新
        reportService.saveReport(report);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }

    /*
     * 返信の登録処理
     */
    @PostMapping("/comment/{id}")
    //@PathVariable Integer idは元投稿のid → ちなみにこのidは下に書いたcommentにidでセットされる
    //@ModelAttributeのおかげでcommentにtextはセット済み
    public ModelAndView addText (@PathVariable Integer id,
                                 @ModelAttribute("commentModel") CommentForm comment) {
        //commentに@PathVariableで持ってきたidをcontentIdとしてセット
        comment.setContentId(id);
        //(返信の編集時に必要)@PathVariableで持ってきたidが勝手にcommentのidにセットされてしまうのでリセットする
        comment.setId(0);
        //今の時間をセット
        comment.setCreatedDate(new Date());
        comment.setUpdatedDate(comment.getCreatedDate());
        //commentに入ったもの全てを登録したいので運ぶ
        commentService.saveComment(comment);

        //返信が追加されたら元投稿のupdateDataも更新する→変更箇所は変更して全部持って再度全部更新し直す
        reportService.updateUpdatedData(id, comment.getCreatedDate());
        //返信の登録は終わったので、表示はお任せします
        return new ModelAndView("redirect:/");
    }

    /*
     * 返信削除処理
     */
    @DeleteMapping("/commentDelete/{id}")
    public ModelAndView deleteText(@PathVariable Integer id) {
        // テーブルから削除する投稿を特定できるidを運ぶ
        commentService.deleteComment(id);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }

    /*
     * 返信の編集画面表示処理
     */
    @GetMapping("/commentEdit/{id}")
    public ModelAndView editText(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();
        // 編集する返信を取得
        CommentForm comment = commentService.editComment(id);
        // 編集する返信をセット
        mav.addObject("formModel", comment);
        // 画面遷移先を指定
        mav.setViewName("/commentEdit");
        return mav;
    }

    /*
     * 返信の編集処理
     */
    @PutMapping("/commentUpdate/{id}")
    //今回は返信用テーブルの主キーのid → ちなみにこのidは下に書いたcommentにidでセットされる
    //@ModelAttributeのおかげでcommentにtextはセット済み
    public ModelAndView updateText (@PathVariable Integer id,
                                    @ModelAttribute("formModel") CommentForm comment) {
        // UrlParameterのidを更新するformにセット→なくてもいける
        //comment.setId(id);
        // 編集した投稿を更新
        commentService.saveComment(comment);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }



}