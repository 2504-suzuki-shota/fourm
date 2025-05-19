package com.example.forum.controller;

import com.example.forum.controller.form.CommentForm;
import com.example.forum.controller.form.ReportForm;
import com.example.forum.service.CommentService;
import com.example.forum.service.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
    @Autowired
    HttpSession session;

    /*
     * 投稿内容表示処理
     */
    @GetMapping
    public ModelAndView top(@ModelAttribute("date") ReportForm date) {
        ModelAndView mav = new ModelAndView();
        //バリデーション③用 Sessionを別の箱に移す
        String errorMessage = (String) session.getAttribute("errorMessage");
        errorMethod(errorMessage, mav);
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
        //バリデーション①用 Sessionを別の箱に移す
        String errorMessage = (String) session.getAttribute("errorMessage");
        errorMethod(errorMessage, mav);
        // form用の空のformを準備
        ReportForm reportForm = new ReportForm();
        // 画面遷移先を指定→("/?")は??.HTMLの??に対応してる
        mav.setViewName("/new");
        // 準備した空のFormを保管
        mav.addObject("formModel", reportForm);
        return mav;
    }

    public void errorMethod(String errorMessage, ModelAndView mav){
        if(errorMessage != null){
            session.removeAttribute("errorMessage");
            mav.addObject("errorMessage", errorMessage);
        }
    }

    /*
     * 新規投稿の登録
     */
    @PostMapping("/add")
    public ModelAndView addContent(@Valid @ModelAttribute("formModel") ReportForm reportForm, BindingResult result){
        //バリデーション① contentが空の時
        if (result.hasErrors()) {
            ModelAndView mav =new ModelAndView();
            //sessionに入れる(mavのスコープはrequestと同じ)
            session.setAttribute("errorMessage", "投稿内容を入力してください");
            mav.setViewName("redirect:/new");
            return mav;
        }
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
        //バリデーション②用 Sessionを別の箱に移す
        String errorMessage = (String) session.getAttribute("errorMessage");
        errorMethod(errorMessage, mav);
            // 編集する投稿を取得
            ReportForm report = reportService.editReport(id);
            // 編集する投稿を表示するためにセット
            mav.addObject("formModel", report);
        // 画面遷移先を指定
        mav.setViewName("/edit");
        return mav;
    }

    /*
     * 編集の登録
     */
    @PutMapping("/update/{id}")
    public ModelAndView updateContent (@PathVariable Integer id,
                                       @Valid @ModelAttribute("formModel") ReportForm report, BindingResult result) {
        //バリデーション② contentが空の時
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            session.setAttribute("errorMessage", "投稿内容を入力してください");
            mav.setViewName("redirect:/edit/{id}");
            return mav;
        }
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
                                 @Valid @ModelAttribute("commentModel") CommentForm comment, BindingResult result) {

        //バリデーション③ textが空の時
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject("errorMessage", "コメントを入力してください");
            mav.setViewName("redirect:/");
            return mav;
        }

        //commentに@PathVariableで持ってきたidをcontentIdとしてセット
        comment.setContentId(id);
        //(返信の編集との差別化)@PathVariableで持ってきたidが勝手にcommentのidにセットされてしまうのでリセットする
        comment.setId(0);
        //今の時間をセット
        comment.setCreatedDate(new Date());
        comment.setUpdatedDate(comment.getCreatedDate());
        //commentに入ったもの全てを登録したいので運ぶ
        commentService.saveComment(comment);
        //(表示降順用処理)返信が追加されたら元投稿のupdateDataも更新する→変更箇所は変更して全部持って再度全部更新し直す
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
        //バリデーション④用 Sessionを別の箱に移す
        String errorMessage = (String) session.getAttribute("errorMessage");
        errorMethod(errorMessage, mav);
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
    //@ModelAttributeのおかげでcommentにtextはセット済み
    public ModelAndView updateText (@PathVariable Integer id,
                                    @Valid @ModelAttribute("formModel") CommentForm comment, BindingResult result) {
        //バリデーション④ textが空の時
        if (result.hasErrors()) {
            ModelAndView mav = new ModelAndView();
            mav.setViewName("/commentEdit/{id}");
            mav.addObject("errorMessage", "コメントを入力してください");
            return mav;
        }

        //UpdatedDateに今の時間をセット
        comment.setUpdatedDate(new Date());
        // 編集した投稿を更新
        commentService.saveComment(comment);
        // rootへリダイレクト
        return new ModelAndView("redirect:/");
    }



}