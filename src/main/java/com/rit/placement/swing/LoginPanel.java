package com.rit.placement.swing;

import com.rit.placement.dao.UserDAO;
import com.rit.placement.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {
    private SwingApp app;
    private UserDAO userDAO;

    // Login Fields
    private JTextField loginUsnField;
    private JPasswordField loginPasswordField;

    // Signup Fields
    private JTextField signupUsnField;
    private JTextField signupNameField;
    private JTextField signupEmailField;
    private JPasswordField signupPasswordField;
    private JPasswordField signupConfirmPasswordField;

    public LoginPanel(SwingApp app) {
        this.app = app;
        this.userDAO = new UserDAO();
        
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245)); // Light background

        // Main Container for centering
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(240, 242, 245));

        // Create the card panel with custom styling
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(450, 550));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));

        // Title
        JLabel titleLabel = new JLabel("CampusToCareer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(26, 115, 232)); // Google Blue
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        cardPanel.add(titleLabel, BorderLayout.NORTH);

        // Tabbed Pane for Sign In and Sign Up
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setFocusable(false);
        tabbedPane.setBackground(Color.WHITE);

        // Add tabs
        tabbedPane.addTab("Sign In", createSignInPanel());
        tabbedPane.addTab("Sign Up", createSignUpPanel());

        cardPanel.add(tabbedPane, BorderLayout.CENTER);

        centerContainer.add(cardPanel);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.weightx = 1.0;

        // Welcome Text
        JLabel welcomeLabel = new JLabel("Welcome Back!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(new Color(60, 64, 67));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(welcomeLabel, gbc);

        JLabel subLabel = new JLabel("Please enter your details to sign in.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(128, 134, 139));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(subLabel, gbc);

        // USN Field
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        JLabel usnLabel = new JLabel("USN / Username");
        usnLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        usnLabel.setForeground(new Color(95, 99, 104));
        panel.add(usnLabel, gbc);

        gbc.gridy = 3;
        loginUsnField = createStyledTextField();
        panel.add(loginUsnField, gbc);

        // Password Field
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 5, 0);
        JLabel pwdLabel = new JLabel("Password");
        pwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pwdLabel.setForeground(new Color(95, 99, 104));
        panel.add(pwdLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 25, 0);
        loginPasswordField = createStyledPasswordField();
        panel.add(loginPasswordField, gbc);

        // Login Button
        gbc.gridy = 6;
        JButton loginBtn = createStyledButton("SIGN IN", new Color(26, 115, 232));
        loginBtn.addActionListener(e -> attemptLogin());
        
        // Ensure enter triggers login when focused on this tab
        app.getRootPane().setDefaultButton(loginBtn);
        panel.add(loginBtn, gbc);

        // Push everything up
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        panel.add(new JLabel(""), gbc);

        return panel;
    }

    private JPanel createSignUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 2, 0);
        gbc.weightx = 1.0;

        // Header
        JLabel headerLabel = new JLabel("Create an Account");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(60, 64, 67));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(headerLabel, gbc);

        // USN Field
        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 2, 0);
        JLabel usnLabel = new JLabel("USN");
        usnLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        usnLabel.setForeground(new Color(95, 99, 104));
        panel.add(usnLabel, gbc);

        gbc.gridy++;
        signupUsnField = createStyledTextField();
        panel.add(signupUsnField, gbc);

        // Name Field
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(95, 99, 104));
        panel.add(nameLabel, gbc);

        gbc.gridy++;
        signupNameField = createStyledTextField();
        panel.add(signupNameField, gbc);
        
        // Email Field
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLabel.setForeground(new Color(95, 99, 104));
        panel.add(emailLabel, gbc);

        gbc.gridy++;
        signupEmailField = createStyledTextField();
        panel.add(signupEmailField, gbc);

        // Password Field
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel pwdLabel = new JLabel("Password");
        pwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pwdLabel.setForeground(new Color(95, 99, 104));
        panel.add(pwdLabel, gbc);

        gbc.gridy++;
        signupPasswordField = createStyledPasswordField();
        panel.add(signupPasswordField, gbc);

        // Confirm Password Field
        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 2, 0);
        JLabel cpwdLabel = new JLabel("Confirm Password");
        cpwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cpwdLabel.setForeground(new Color(95, 99, 104));
        panel.add(cpwdLabel, gbc);

        gbc.gridy++;
        signupConfirmPasswordField = createStyledPasswordField();
        panel.add(signupConfirmPasswordField, gbc);

        // Sign Up Button
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        JButton signupBtn = createStyledButton("SIGN UP", new Color(15, 157, 88)); // Google Green
        signupBtn.addActionListener(e -> attemptSignUp());
        panel.add(signupBtn, gbc);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 220, 224)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder());
        
        // Add subtle hover effect (basic implementation)
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    private void attemptLogin() {
        String usn = loginUsnField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (usn.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both USN and Password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(usn, password);
            if (user != null) {
                loginUsnField.setText("");
                loginPasswordField.setText("");
                app.onLoginSuccess(user);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or account is inactive.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptSignUp() {
        String usn = signupUsnField.getText().trim();
        String name = signupNameField.getText().trim();
        String email = signupEmailField.getText().trim();
        String password = new String(signupPasswordField.getPassword());
        String confirmPassword = new String(signupConfirmPasswordField.getPassword());

        if (usn.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = userDAO.registerStudent(usn, name, email, password);
            if (success) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now sign in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear fields
                signupUsnField.setText("");
                signupNameField.setText("");
                signupEmailField.setText("");
                signupPasswordField.setText("");
                signupConfirmPasswordField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. The USN might already exist.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
