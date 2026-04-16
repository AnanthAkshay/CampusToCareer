<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Application" %>
<%@ page import="com.rit.placement.dao.ApplicationDAO.ApplicationStats" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }

  // Read data from request attributes
  @SuppressWarnings("unchecked")
  List<Application> applications = (List<Application>) request.getAttribute("applications");
  ApplicationStats stats = (ApplicationStats) request.getAttribute("stats");

  // Get success/error messages
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  
  if (successMessage != null) {
    session.removeAttribute("successMessage");
  }
  if (errorMessage != null) {
    session.removeAttribute("errorMessage");
  }
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>My Applications — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Loading applications...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">My Applications 📋</h1>
        <p class="page-subtitle">Track your job application status</p>
      </div>
      <div class="header-actions">
        <a href="${pageContext.request.contextPath}/apply" class="btn-primary">
          <span>➕</span> Apply for Jobs
        </a>
      </div>
    </div>

    <!-- Success/Error Messages -->
    <% if (successMessage != null) { %>
      <div class="alert alert-success" role="status" aria-live="polite">
        <span class="alert-icon">✓</span>
        <%=successMessage%>
      </div>
    <% } %>
    
    <% if (errorMessage != null) { %>
      <div class="alert alert-error">
        <span class="alert-icon">✕</span>
        <%=errorMessage%>
      </div>
    <% } %>

    <!-- Statistics Cards -->
    <% if (stats != null) { %>
    <div class="stats-grid">
      <div class="stat-card stat-card-blue">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <div class="stat-label">Total Applications</div>
          <div class="stat-value"><%=stats.getTotal()%></div>
        </div>
      </div>

      <div class="stat-card stat-card-purple">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <div class="stat-label">Applied</div>
          <div class="stat-value"><%=stats.getApplied()%></div>
          <div class="stat-trend">
            <span class="trend-indicator">Under Review</span>
          </div>
        </div>
      </div>

      <div class="stat-card stat-card-gradient">
        <div class="stat-icon">✓</div>
        <div class="stat-content">
          <div class="stat-label">Shortlisted</div>
          <div class="stat-value"><%=stats.getShortlisted()%></div>
          <div class="stat-trend">
            <span class="trend-indicator">Good Progress!</span>
          </div>
        </div>
      </div>

      <div class="stat-card" style="border-top: 4px solid #16a34a;">
        <div class="stat-icon">🎉</div>
        <div class="stat-content">
          <div class="stat-label">Selected</div>
          <div class="stat-value"><%=stats.getSelected()%></div>
        </div>
      </div>
    </div>
    <% } %>

    <!-- Applications Table -->
    <% if (applications != null && !applications.isEmpty()) { %>
      <div class="table-wrap">
        <table class="applications-table">
          <thead>
            <tr>
              <th>Application ID</th>
              <th>Company</th>
              <th>Job Role</th>
              <th>Applied On</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <% for (Application app : applications) { 
                 String statusClass = "";
                 String statusIcon = "";
                 if ("APPLIED".equals(app.getStatus())) {
                   statusClass = "badge-info";
                   statusIcon = "⏳";
                 } else if ("SHORTLISTED".equals(app.getStatus())) {
                   statusClass = "badge-success";
                   statusIcon = "✓";
                 } else if ("SELECTED".equals(app.getStatus())) {
                   statusClass = "badge-selected";
                   statusIcon = "🎉";
                 } else if ("REJECTED".equals(app.getStatus())) {
                   statusClass = "badge-danger";
                   statusIcon = "✕";
                 }
            %>
              <tr class="application-row">
                <td class="app-id-cell">#<%=app.getApplicationId()%></td>
                <td class="company-cell">
                  <strong><%=app.getCompanyName()%></strong>
                </td>
                <td class="role-cell"><%=app.getJobRole()%></td>
                <td class="date-cell">
                  <%= app.getAppliedAt() != null ? dateFormat.format(app.getAppliedAt()) : "N/A" %>
                </td>
                <td class="status-cell">
                  <span class="badge <%=statusClass%>">
                    <%=statusIcon%> <%=app.getStatus()%>
                  </span>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    <% } else { %>
      <!-- Empty State -->
      <div class="empty-state">
        <div class="empty-state-icon">📋</div>
        <h4 class="empty-state-title">No Applications Yet</h4>
        <p class="empty-state-text">
          You haven't applied to any jobs yet. Start by browsing available opportunities!
        </p>
        <a href="${pageContext.request.contextPath}/apply" class="btn-primary" style="margin-top: 20px;">
          <span>💼</span> Browse Jobs
        </a>
      </div>
    <% } %>

    <!-- Status Legend -->
    <% if (applications != null && !applications.isEmpty()) { %>
    <div class="status-legend">
      <h3 class="legend-title">Status Guide</h3>
      <div class="legend-items">
        <div class="legend-item">
          <span class="badge badge-info">⏳ APPLIED</span>
          <span class="legend-text">Your application is under review</span>
        </div>
        <div class="legend-item">
          <span class="badge badge-success">✓ SHORTLISTED</span>
          <span class="legend-text">You've been shortlisted for the next round</span>
        </div>
        <div class="legend-item">
          <span class="badge badge-selected">🎉 SELECTED</span>
          <span class="legend-text">Congratulations! You've been selected</span>
        </div>
        <div class="legend-item">
          <span class="badge badge-danger">✕ REJECTED</span>
          <span class="legend-text">Unfortunately, your application was not successful</span>
        </div>
      </div>
    </div>
    <% } %>
  </main>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
  <script>
    // Page Load Handler
    window.addEventListener('load', function() {
      setTimeout(function() {
        document.getElementById('loadingOverlay').classList.add('fade-out');
        document.querySelector('.page-content').classList.add('fade-in');
        setTimeout(function() {
          document.getElementById('loadingOverlay').style.display = 'none';
        }, 300);
      }, 400);
    });

    // Auto-hide messages
    window.addEventListener('DOMContentLoaded', function() {
      var successAlert = document.querySelector('.alert-success');
      if (successAlert) {
        setTimeout(function() {
          successAlert.style.opacity = '0';
          successAlert.style.transform = 'translateY(-10px)';
          setTimeout(function() {
            successAlert.style.display = 'none';
          }, 400);
        }, 8000);
      }
    });
  </script>
</body>
</html>
