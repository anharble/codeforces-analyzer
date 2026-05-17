package com.codeforces.analyzer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.codeforces.analyzer.view.MainFrame;
import com.codeforces.analyzer.view.UiUtils;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } catch (Exception e) {
                UiUtils.showError(null, "Không khởi động được ứng dụng.", e);
            }
        });
    }
}
