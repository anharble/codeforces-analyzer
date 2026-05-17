package com.codeforces.analyzer.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

public class BarChartPanel extends JPanel {
    private Map<String, Integer> data = new LinkedHashMap<>();

    public BarChartPanel() {
        setPreferredSize(new Dimension(520, 240));
        setBackground(Color.WHITE);
    }

    public void setData(Map<String, Integer> data) {
        this.data = data == null ? new LinkedHashMap<>() : new LinkedHashMap<>(data);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int left = 150;
        int top = 20;
        int rowHeight = 26;
        int barMaxWidth = Math.max(80, width - left - 35);

        if (data.isEmpty()) {
            g2.setColor(new Color(90, 90, 90));
            g2.drawString("Chưa có dữ liệu để vẽ biểu đồ.", 20, height / 2);
            g2.dispose();
            return;
        }

        int max = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        FontMetrics metrics = g2.getFontMetrics();
        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int y = top + index * rowHeight;
            if (y + rowHeight > height - 10) {
                break;
            }
            String label = trimLabel(entry.getKey(), metrics, left - 20);
            int barWidth = (int) Math.round(entry.getValue() * 1.0 / max * barMaxWidth);
            g2.setColor(new Color(55, 107, 170));
            g2.fillRoundRect(left, y, barWidth, 18, 6, 6);
            g2.setColor(new Color(35, 35, 35));
            g2.drawString(label, 12, y + 14);
            g2.drawString(String.valueOf(entry.getValue()), left + barWidth + 8, y + 14);
            index++;
        }
        g2.dispose();
    }

    private String trimLabel(String label, FontMetrics metrics, int maxWidth) {
        if (label == null) {
            return "";
        }
        if (metrics.stringWidth(label) <= maxWidth) {
            return label;
        }
        String suffix = "...";
        String result = label;
        while (!result.isEmpty() && metrics.stringWidth(result + suffix) > maxWidth) {
            result = result.substring(0, result.length() - 1);
        }
        return result + suffix;
    }
}
