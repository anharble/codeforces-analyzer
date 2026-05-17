package com.codeforces.analyzer.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.codeforces.analyzer.analyzer.GroqAnalyzer;
import com.codeforces.analyzer.dao.AnalysisDAO;
import com.codeforces.analyzer.dao.SubmissionDAO;
import com.codeforces.analyzer.model.AnalysisResult;
import com.codeforces.analyzer.model.Submission;

public class AnalysisService {
    private final AnalysisDAO analysisDAO;
    private final SubmissionDAO submissionDAO;
    private final GroqAnalyzer groqAnalyzer;

    public AnalysisService(AnalysisDAO analysisDAO, SubmissionDAO submissionDAO, GroqAnalyzer groqAnalyzer) {
        this.analysisDAO = analysisDAO;
        this.submissionDAO = submissionDAO;
        this.groqAnalyzer = groqAnalyzer;
    }

    public AnalysisResult analyzeSubmission(Submission submission) {
        try {
            AnalysisResult result = groqAnalyzer.analyze(submission);
            return analysisDAO.saveOrUpdate(result);
        } catch (SQLException e) {
            throw new IllegalStateException("Không lưu được kết quả phân tích.", e);
        }
    }

    public AnalysisResult analyzeSubmission(long submissionId) {
        try {
            Submission submission = submissionDAO.findById(submissionId);
            if (submission == null) {
                throw new IllegalArgumentException("Không tìm thấy bài nộp cần phân tích.");
            }
            return analyzeSubmission(submission);
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được bài nộp để phân tích.", e);
        }
    }

    public List<AnalysisResult> analyzeAllPending() {
        try {
            List<Submission> pending = submissionDAO.findWithoutAnalysis();
            List<AnalysisResult> results = new ArrayList<>();
            for (Submission submission : pending) {
                results.add(analyzeSubmission(submission));
            }
            return results;
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được danh sách bài nộp chưa phân tích.", e);
        }
    }

    public AnalysisResult findBySubmissionId(long submissionId) {
        try {
            return analysisDAO.findBySubmissionId(submissionId);
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được kết quả phân tích.", e);
        }
    }

    public List<AnalysisResult> findAll() {
        try {
            return analysisDAO.findAll();
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được danh sách phân tích.", e);
        }
    }
}
