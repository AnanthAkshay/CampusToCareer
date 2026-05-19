<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Application" %>
<%@ page import="com.rit.placement.dao.ApplicationDAO.ApplicationStats" %>
<%@ page import="java.util.List" %>
<%
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }
  
  @SuppressWarnings("unchecked")
  List<Application> applications = (List<Application>) request.getAttribute("applications");
  ApplicationStats stats = (ApplicationStats) request.getAttribute("stats");
  
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  if (successMessage != null) session.removeAttribute("successMessage");
  if (errorMessage != null) session.removeAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Manage Applications — Admin Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Manage Applications 📝</h1>
        <p class="page-subtitle">Total: <%= com.rit.placement.util.XSSUtil.escape(stats != null ? stats.getTotal() : 0) %> applications</p>
      </div>
    </div>

    <% if (successMessage != null) { %>
      <div class="alert alert-success">✓ <%= com.rit.placement.util.XSSUtil.escape(successMessage) %></div>
    <% } %>
    <% if (errorMessage != null) { %>
      <div class="alert alert-error">✕ <%= com.rit.placement.util.XSSUtil.escape(errorMessage) %></div>
    <% } %>

    <% if (stats != null) { %>
    <div class="stats-grid" style="margin-bottom: 20px;">
      <div class="stat-card"><div class="stat-label">Applied</div><div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(stats.getApplied()) %></div></div>
      <div class="stat-card"><div class="stat-label">Shortlisted</div><div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(stats.getShortlisted()) %></div></div>
      <div class="stat-card"><div class="stat-label">Selected</div><div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(stats.getSelected()) %></div></div>
      <div class="stat-card"><div class="stat-label">Rejected</div><div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(stats.getRejected()) %></div></div>
    </div>
    <% } %>

    <% if (applications != null && !applications.isEmpty()) { %>
      <div class="table-wrap">
        <table class="applications-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Student</th>
              <th>USN</th>
              <th>Company</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <% for (Application app : applications) { %>
              <tr>
                <td>#<%= com.rit.placement.util.XSSUtil.escape(String.valueOf(app.getId())) %></td>
                <td><%= com.rit.placement.util.XSSUtil.escape(app.getStudentName()) %></td>
                <td><%= com.rit.placement.util.XSSUtil.escape(app.getStudentUsn()) %></td>
                <td><%= com.rit.placement.util.XSSUtil.escape(app.getCompanyName()) %></td>
                <td><%= com.rit.placement.util.XSSUtil.escape(app.getJobTitle()) %></td>
                <td>
                  <span class="badge badge-<%= com.rit.placement.util.XSSUtil.escape(app.getStatus().toLowerCase()) %>">
                    <%= com.rit.placement.util.XSSUtil.escape(app.getStatus()) %>
                  </span>
                </td>
                <td>
                  <form method="post" style="display: inline;">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                    <input type="hidden" name="action" value="update_status">
                    <input type="hidden" name="application_id" value="<%= com.rit.placement.util.XSSUtil.escape(String.valueOf(app.getId())) %>">
                    <select name="status" onchange="this.form.submit()" class="status-select">
                      <option value="">Change Status</option>
                      <option value="APPLIED">Applied</option>
                      <option value="SHORTLISTED">Shortlisted</option>
                      <option value="SELECTED">Selected</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                  </form>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    <% } else { %>
      <div class="empty-state">
        <div class="empty-state-icon">📝</div>
        <h4 class="empty-state-title">No Applications Found</h4>
      </div>
    <% } %>
  </main>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', function() {
      const pageContent = document.querySelector('.page-content');
      if (pageContent) {
        pageContent.classList.add('fade-in');
      }
    });
  </script>
  <style>
    .status-select {
      padding: 5px 10px;
      border: 1px solid #d1d5db;
      border-radius: 4px;
      font-size: 14px;
    }
  </style>
</body>
</html>
