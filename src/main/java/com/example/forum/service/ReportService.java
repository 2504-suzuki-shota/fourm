package com.example.forum.service;

import com.example.forum.controller.form.ReportForm;
import com.example.forum.repository.ReportRepository;
import com.example.forum.repository.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {
    @Autowired
    ReportRepository reportRepository;

    /*
     * レコード全件取得処理
     */
    public List<ReportForm> findAllReport() {
        List<Report> results = reportRepository.findAllByOrderByIdDesc();
        List<ReportForm> reports = setReportForm(results);
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
            reports.add(report);
        }
        return reports;
    }

    /*
     * レコード追加
     */
    public void saveReport(ReportForm reqReport) {
        //saveメソッドの引数はEntity型だからReportForm型をreport型に変換できるメソッドに飛ばす
        Report saveReport = setReportEntity(reqReport);
        //saveメソッドは登録と更新を自動で分けてくれる
        Report insert = reportRepository.save(saveReport);
        //今回は使わないけどsaveメソッドの戻り値は登録した情報
        //System.out.println(insert.getId());
        //System.out.println(insert.getContent());
    }

    /*
     * リクエストから取得した情報をEntity型に変換
     */
    private Report setReportEntity(ReportForm reqReport) {
        Report report = new Report();
        report.setId(reqReport.getId());
        report.setContent(reqReport.getContent());
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
     * レコード1件取得
     */
    public ReportForm editReport(Integer id) {
        List<Report> results = new ArrayList<>();
        Report report = (Report) reportRepository.findById(id).orElse(null);
        results.add(report);
        List<ReportForm> reports = setReportForm(results);
        return reports.get(0);
    }
}
