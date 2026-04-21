<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rit.placement.model.User" %>
<%@ page import="com.rit.placement.model.StudentPerformance" %>
<%@ page import="com.rit.placement.model.ProctorRemark" %>
<%@ page import="com.rit.placement.model.Application" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"PROCTOR".equals(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    StudentPerformance student = (StudentPerformance) request.getAttribute("student");
    List<ProctorRemark> remarks = (List<ProctorRemark>) request.getAttribute("remarks");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Student Detail - <%= student.getName() %></title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }
        .navbar { background: rgba(44, 62, 80, 0.95); backdrop-filter: blur(10px); color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .navbar h1 { font-size: 1.5rem; }
        .navbar a { color: white; text-decoration: none; margin-left: 1rem; padding: 0.5rem 1rem; border-radius: 4px; transition: background 0.3s; }
        .navbar a:hover { background: rgba(255,255,255,0.1); }
        .container { max-width: 1200px; margin: 2rem auto; padding: 0 2rem; }
        .card { background: white; padding: 2rem; border-radius: 12px; margin-bottom: 2rem; box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
        .card h2 { margin-bottom: 1.5rem; color: #2c3e50; display: flex; align-items: center; gap: 0.5rem; }
        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1rem; margin-bottom: 1rem; }
        .info-item { padding: 1.25rem; background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); border-radius: 8px; border-left: 4px solid #667eea; }
        .info-item label { display: block; color: #7f8c8d; font-size: 0.85rem; margin-bottom: 0.5rem; text-transform: uppercase; letter-spacing: 0.5px; }
        .info-item .value { font-size: 1.15rem; font-weight: 600; color: #2c3e50; }
        .risk-badge { padding: 0.6rem 1.2rem; border-radius: 25px; font-size: 1rem; font-weight: 600; display: inline-block; text-transform: uppercase; letter-spacing: 0.5px; }
        .risk-at-risk { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3); }
        .risk-safe { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; box-shadow: 0 4px 12px rgba(67, 233, 123, 0.3); }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid #ecf0f1; }
        th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-weight: 600; text-transform: uppercase; font-size: 0.85rem; letter-spacing: 0.5px; }
        tbody tr { transition: background 0.2s; }
        tbody tr:hover { background: #f8f9fa; }
        .remark-form { margin-top: 1.5rem; }
        .remark-form textarea { width: 100%; padding: 1rem; border: 2px solid #ecf0f1; border-radius: 8px; font-family: inherit; resize: vertical; min-height: 120px; font-size: 1rem; transition: border 0.3s; }
        .remark-form textarea:focus { outline: none; border-color: #667eea; }
        .btn { padding: 0.85rem 1.75rem; border: none; border-radius: 8px; cursor: pointer; font-size: 1rem; font-weight: 600; transition: all 0.3s; }
        .btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4); }
        .btn-back { background: linear-gradient(135deg, #95a5a6 0%, #7f8c8d 100%); color: white; text-decoration: none; display: inline-block; }
        .btn-back:hover { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(149, 165, 166, 0.4); }
        .remark-item { padding: 1.25rem; background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%); border-left: 4px solid #667eea; margin-bottom: 1rem; border-radius: 8px; transition: transform 0.2s; }
        .remark-item:hover { transform: translateX(5px); }
        .remark-header { display: flex; justify-content: space-between; margin-bottom: 0.75rem; }
        .remark-author { font-weight: 600; color: #2c3e50; display: flex; align-items: center; gap: 0.5rem; }
        .remark-date { color: #7f8c8d; font-size: 0.9rem; }
        .remark-text { color: #34495e; line-height: 1.6; }
        .alert { padding: 1.25rem; border-radius: 8px; margin-bottom: 1.5rem; display: flex; align-items: center; gap: 0.75rem; font-weight: 500; }
        .alert-success { background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%); color: #155724; border-left: 4px solid #28a745; }
        .alert-error { background: linear-gradient(135deg, #f8d7da 0%, #f5c6cb 100%); color: #721c24; border-left: 4px solid #dc3545; }
        .warning-box { background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%); padding: 1rem; border-radius: 8px; border-left: 4px solid #ffc107; margin-top: 1rem; }
        .warning-box p { color: #856404; margin: 0; display: flex; align-items: center; gap: 0.5rem; }
        @media (max-width: 768px) {
            .info-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <div class="navbar">
        <h1>🎓 Student Performance Detail</h1>
        <div>
            <a href="<%= request.getContextPath() %>/proctor/dashboard" class="btn-back">← Back to Dashboard</a>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>
    
    <div class="container">
        <% if ("true".equals(request.getParameter("success"))) { %>
        <div class="alert alert-success">✓ Remark added successfully!</div>
        <% } else if ("true".equals(request.getParameter("error"))) { %>
        <div class="alert alert-error">✗ Failed to add remark. Please try again.</div>
        <% } %>
        
        <div class="card">
            <h2>📋 Academic Information</h2>
            <div class="info-grid">
                <div class="info-item">
                    <label>USN</label>
                    <div class="value"><%= student.getUsn() %></div>
                </div>
                <div class="info-item">
                    <label>Student Name</label>
                    <div class="value"><%= student.getName() %></div>
                </div>
                <div class="info-item">
                    <label>Email</label>
                    <div class="value"><%= student.getEmail() %></div>
                </div>
                <div class="info-item">
                    <label>Branch</label>
                    <div class="value"><%= student.getBranch() %></div>
                </div>
                <div class="info-item">
                    <label>CGPA</label>
                    <div class="value"><%= String.format("%.2f", student.getCgpa()) %></div>
                </div>
                <div class="info-item">
                    <label>Applications Count</label>
                    <div class="value"><%= student.getApplicationsCount() %></div>
                </div>
                <div class="info-item">
                    <label>Placement Status</label>
                    <div class="value"><%= student.isPlaced() ? "✓ PLACED" : "NOT PLACED" %></div>
                </div>
                <div class="info-item">
                    <label>Skills</label>
                    <div class="value"><%= student.getSkills() != null ? student.getSkills() : "Not specified" %></div>
                </div>
            </div>
            <div style="margin-top: 1rem;">
                <label style="display: block; margin-bottom: 0.5rem; color: #7f8c8d;">Risk Status</label>
                <span class="risk-badge <%= "AT RISK".equals(student.getRiskStatus()) ? "risk-at-risk" : "risk-safe" %>">
                    <%= student.getRiskStatus() %>
                </span>
                <% if ("AT RISK".equals(student.getRiskStatus())) { %>
                <div class="warning-box">
                    <p>
                        <span>⚠️</span>
                        <span>This student needs attention: 
                        <%= student.getCgpa() < 7.0 ? "Low CGPA (" + String.format("%.2f", student.getCgpa()) + ")" : "" %>
                        <%= student.getApplicationsCount() == 0 ? " No applications submitted" : "" %>
                        </span>
                    </p>
                </div>
                <% } %>
            </div>
        </div>
        
        <div class="card">
            <h2>📝 Applications History</h2>
            <% if (applications != null && !applications.isEmpty()) { %>
            <table>
                <thead>
                    <tr>
                        <th>Company</th>
                        <th>Job Title</th>
                        <th>Applied Date</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Application app : applications) { %>
                    <tr>
                        <td><%= app.getCompanyName() != null ? app.getCompanyName() : "N/A" %></td>
                        <td><%= app.getJobTitle() != null ? app.getJobTitle() : "N/A" %></td>
                        <td><%= app.getAppliedDate() %></td>
                        <td><%= app.getStatus() %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
            <% } else { %>
            <p style="color: #7f8c8d; text-align: center; padding: 2rem;">No applications found.</p>
            <% } %>
        </div>
        
        <div class="card">
            <h2>💬 Proctor Remarks</h2>
            <% if (remarks != null && !remarks.isEmpty()) { %>
                <% for (ProctorRemark remark : remarks) { %>
                <div class="remark-item">
                    <div class="remark-header">
                        <span class="remark-author"><span>👤</span> <%= remark.getProctorName() %></span>
                        <span class="remark-date">🕒 <%= remark.getCreatedAt() %></span>
                    </div>
                    <div class="remark-text"><%= remark.getRemark() %></div>
                </div>
                <% } %>
            <% } else { %>
            <p style="color: #7f8c8d; margin-bottom: 1rem;">No remarks yet.</p>
            <% } %>
            
            <form action="<%= request.getContextPath() %>/proctor/remark" method="post" class="remark-form">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="studentId" value="<%= student.getStudentId() %>">
                <label for="remark" style="display: block; margin-bottom: 0.5rem; font-weight: 600;">Add New Remark</label>
                <textarea name="remark" id="remark" required placeholder="Enter your observation or advice for this student..."></textarea>
                <button type="submit" class="btn btn-primary" style="margin-top: 1rem;">Submit Remark</button>
            </form>
        </div>
    </div>
</body>
</html>
