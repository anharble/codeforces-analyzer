package com.codeforces.analyzer.service;

import java.sql.SQLException;
import java.util.List;

import com.codeforces.analyzer.dao.SubmissionDAO;
import com.codeforces.analyzer.model.Submission;

public class SubmissionService {
    private final SubmissionDAO submissionDAO;

    public SubmissionService(SubmissionDAO submissionDAO) {
        this.submissionDAO = submissionDAO;
    }

    public List<Submission> findByUser(long userId) {
        try {
            return submissionDAO.findByUser(userId);
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được danh sách bài nộp.", e);
        }
    }

    public List<Submission> findAll() {
        try {
            return submissionDAO.findAll();
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được toàn bộ bài nộp.", e);
        }
    }

    public Submission findById(long id) {
        try {
            return submissionDAO.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được bài nộp.", e);
        }
    }

    public List<Submission> findWithoutAnalysis() {
        try {
            return submissionDAO.findWithoutAnalysis();
        } catch (SQLException e) {
            throw new IllegalStateException("Không tải được bài nộp chưa phân tích.", e);
        }
    }
}
