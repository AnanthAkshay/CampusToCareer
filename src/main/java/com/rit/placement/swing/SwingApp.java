package com.rit.placement.swing;

import javax.swing.*;
import java.awt.*;
import com.rit.placement.model.User;

public class SwingApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;

    public SwingApp() {
        setTitle("CampusToCareer - Desktop Portal");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(dashboardPanel, "DASHBOARD");

        add(mainPanel);
        
        showLogin();
    }

    public void showLogin() {
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void onLoginSuccess(User user) {
        dashboardPanel.setUser(user);
        cardLayout.show(mainPanel, "DASHBOARD");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for a cleaner UI
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SwingApp().setVisible(true);
        });
    }
}
