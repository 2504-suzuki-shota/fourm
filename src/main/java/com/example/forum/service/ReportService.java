package com.example.forum.service;

import com.example.forum.controller.form.ReportForm;
import com.example.forum.repository.ReportRepository;
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
public class ReportService {
    @Autowired
    ReportRepository reportRepository;

    /*
     * レコード全件取得処理
     */
    public List<ReportForm> findAllReport(String start, String end) {
        //開始日の指定がない時
        if(StringUtils.isBlank(start)) {
            //デフォルトstart（開始日時）
            start = "2020-01-01 00:00:00";
        } else {
            //入力された日付(日付変わった直後から)
            start += " 00:00:00";
        }
        //終了日の指定がない時
        if(StringUtils.isBlank(end)) {
            //入力なし→デフォルトend（現在日時の取得）
            end = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
        } else {
            //入力された日付(日付変わる直前まで)
            end += " 23:59:59";
        }
        Date startDate = null;
        Date endDate = null;
        try {
            //String→Dateに型変換
            startDate = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(start);
            endDate = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(end);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //①何したい？→取得
        //findBy
        //②範囲条件→日付指定
        //CreatedDateBetween(Date startDate, Date endDate)
        //③表示設定→更新日時で降順
        //OrderByUpdatedDateDesc
        List<Report> Results = reportRepository.findByCreatedDateBetweenOrderByUpdatedDateDesc(startDate,endDate);
        List<ReportForm> reports = setReportForm(Results);
        return reports;
    }

    /*
     * DBから取得したデータをFormに設定
     */
    private List<ReportForm> setReportForm(List<Report> results) {
        List<ReportForm> reports = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            ReportForm report = new ReportForm();
            Report result = results.get(i);
            report.setId(result.getId());
            report.setContent(result.getContent());
            report.setCreatedDate(result.getCreatedDate());
            report.setUpdatedDate(result.getUpdatedDate());
            //時間をDate→String型に変えたverもセットしとく
            report.setStringCreatedDate(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(report.getCreatedDate()));
            report.setStringUpdatedDate(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(report.getUpdatedDate()));
            reports.add(report);
        }
        return reports;
    }

    /*
     * レコード追加
     */
    public void saveReport(ReportForm reqReport) {
        //saveメソッドの引数はEntityパケだからReportForm型をreport型に変換できるメソッドに飛ばす
        Report saveReport = setReportEntity(reqReport);
        //saveメソッドは登録と更新を自動で分けてくれる
        reportRepository.save(saveReport);
    }

    /*
     * リクエストから取得した情報をEntity型に変換
     */
    private Report setReportEntity(ReportForm reqReport) {
        Report report = new Report();
        report.setId(reqReport.getId());
        report.setContent(reqReport.getContent());
        report.setUpdatedDate(reqReport.getUpdatedDate());
        if(StringUtils.isBlank(reqReport.getStringCreatedDate())) {
            //登録の時
            report.setCreatedDate(reqReport.getCreatedDate());
        } else {
            //編集の時
            try {
                //stringCreatedDateをcreatedDate（String→Dateに型変換）
                Date createdDate = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(reqReport.getStringCreatedDate());
                report.setCreatedDate(createdDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return report;
    }

    /*
     * レコード削除
     */
    public void deleteReport(Integer id) {
        //deleteByIdはJpaRepositoryに搭載済みだから良しなにやってくれる
        reportRepository.deleteById(id);
    }

    /*
     * 編集対象のレコード1件取得
     */
    public ReportForm editReport(Integer id) {
        Report report = (Report) reportRepository.findById(id).orElse(null);
        List<Report> results = new ArrayList<>();
        results.add(report);
        List<ReportForm> reports = setReportForm(results);
        return reports.get(0);
    }

    /*
     * 降順にするため、updatedDateのみ更新する
     */
    public void updateUpdatedData(Integer id, Date date) {
        //updatedDataを更新したいレコードを取得する
        Report fromDatabaseReport = (Report) reportRepository.findById(id).orElse(null);
        //updatedDateのみ持ってきたdateに変更
        fromDatabaseReport.setUpdatedDate(date);
        //updatedDate以外はそのままでDBを更新
        reportRepository.save(fromDatabaseReport);
    }
}