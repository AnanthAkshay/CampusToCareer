<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.rit.placement.model.Student" %>
<%
    // Security check
    if (session == null || session.getAttribute("user_id") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }

    String role = (String) session.getAttribute("role");
    if (!"PROCTOR".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/pages/error/403.jsp");
        return;
    }

    String proctorName = (String) request.getAttribute("proctorName");
    Integer studentCount = (Integer) request.getAttribute("studentCount");
    List<Student> students = (List<Student>) request.getAttribute("students");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Proctor Dashboard - RIT Placement Portal</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        .header {
            background: white;
            padding: 20px 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            color: #333;
            font-size: 28px;
        }

        .header .user-info {
            display: flex;
            align-items: center;
            gap: 20px;
        }

        .header .user-name {
            color: #666;
            font-size: 16px;
        }

        .logout-btn {
            background: #dc3545;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            font-size: 14px;
            transition: background 0.3s;
        }

        .logout-btn:hover {
            background: #c82333;
        }

        .stats-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }

        .stats-card h2 {
            color: #667eea;
            font-size: 20px;
            margin-bottom: 10px;
        }

        .stats-card .count {
            font-size: 36px;
            font-weight: bold;
            color: #333;
        }

        .students-section {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .students-section h2 {
            color: #333;
            font-size: 24px;
            margin-bottom: 20px;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }

        .table-container {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        table thead {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        table th {
            padding: 15px;
            text-align: left;
            font-weight: 600;
            font-size: 14px;
            text-transform: uppercase;
        }

        table td {
            padding: 15px;
            border-bottom: 1px solid #e0e0e0;
            color: #333;
        }

        table tbody tr:hover {
            background: #f8f9fa;
        }

        .cgpa-badge {
            display: inline-block;
            padding: 5px 12px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 14px;
        }

        .cgpa-high {
            background: #d4edda;
            color: #155724;
        }

        .cgpa-medium {
            background: #fff3cd;
            color: #856404;
        }

        .cgpa-low {
            background: #f8d7da;
            color: #721c24;
        }

        .no-students {
            text-align: center;
            padding: 40px;
            color: #666;
            font-size: 16px;
        }

        .no-students-icon {
            font-size: 48px;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 15px;
                text-align: center;
            }

            table {
                font-size: 14px;
            }

            table th, table td {
                padding: 10px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>🎓 Proctor Dashboard</h1>
            <div class="user-info">
                <span class="user-name">Welcome, <strong><%= proctorName %></strong></span>
                <a href="<%= request.getContextPath() %>/logout" class="logout-btn">Logout</a>
            </div>
        </div>

        <!-- Stats Card -->
        <div class="stats-card">
            <h2>Total Assigned Students</h2>
            <div class="count"><%= studentCount %></div>
        </div>

        <!-- Students Section -->
        <div class="students-section">
            <h2>My Students</h2>

            <% if (students != null && !students.isEmpty()) { %>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>USN</th>
                                <th>Name</th>
                                <th>Branch</th>
                                <th>Semester</th>
                                <th>CGPA</th>
                                <th>Email</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                            int index = 1;
                            for (Student student : students) { 
                                double cgpa = student.getCgpa();
                                String cgpaClass = cgpa >= 8.0 ? "cgpa-high" : 
                                                   cgpa >= 6.0 ? "cgpa-medium" : "cgpa-low";
                            %>
                            <tr>
                                <td><%= index++ %></td>
                                <td><strong><%= student.getUsn() %></strong></td>
                                <td><%= student.getName() %></td>
                                <td><%= student.getBranch() %></td>
                                <td><%= student.getCurrentSem() %></td>
                                <td>
                                    <span class="cgpa-badge <%= cgpaClass %>">
                                        <%= String.format("%.2f", cgpa) %>
                                    </span>
                                </td>
                                <td><%= student.getEmail() != null ? student.getEmail() : "N/A" %></td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="no-students">
                    <div class="no-students-icon">📋</div>
                    <p>No students assigned yet.</p>
                    <p style="color: #999; font-size: 14px; margin-top: 10px;">
                        Contact the administrator to assign students to your account.
                    </p>
                </div>
            <% } %>
        </div>
    </div>
</body>
</html>
