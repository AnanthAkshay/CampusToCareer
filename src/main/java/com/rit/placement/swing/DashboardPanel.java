package com.rit.placement.swing;

import com.rit.placement.model.User;
import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private SwingApp app;
    private JLabel welcomeLabel;
    private JLabel roleLabel;
    private JLabel usnLabel;
    private JLabel dateLabel;

    public DashboardPanel(SwingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(new Color(236, 240, 241));

        // Top Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94)); // Dark Blue-Gray
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBackground(new Color(231, 76, 60)); // Red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        logoutButton.addActionListener(e -> app.showLogin());
        headerPanel.add(logoutButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Main Content Area
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(236, 240, 241));
        
        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Icon/Avatar placeholder
        JLabel avatarLabel = new JLabel("🎓");
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridheight = 3;
        cardPanel.add(avatarLabel, gbc);

        gbc.gridheight = 1;
        
        roleLabel = new JLabel("Role: ");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        roleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 1; gbc.gridy = 0;
        cardPanel.add(roleLabel, gbc);

        usnLabel = new JLabel("USN / ID: ");
        usnLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usnLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 1; gbc.gridy = 1;
        cardPanel.add(usnLabel, gbc);

        dateLabel = new JLabel("Member Since: ");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(new Color(127, 140, 141));
        gbc.gridx = 1; gbc.gridy = 2;
        cardPanel.add(dateLabel, gbc);

        contentPanel.add(cardPanel);
        add(contentPanel, BorderLayout.CENTER);
        
        // Footer
        JLabel footerLabel = new JLabel("CampusToCareer © 2024", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(149, 165, 166));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(footerLabel, BorderLayout.SOUTH);
    }

    public void setUser(User user) {
        welcomeLabel.setText("Welcome, " + user.getName());
        roleLabel.setText("System Role: " + user.getRole());
        usnLabel.setText("Identifier: " + user.getUsn());
        
        if (user.getCreatedAt() != null) {
            dateLabel.setText("Joined: " + user.getCreatedAt().toString().substring(0, 10));
        } else {
            dateLabel.setText("");
        }
    }
}
