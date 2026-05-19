<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.JobPosting" %>
<%@ page import="com.rit.placement.model.JobApplicationStatus" %>
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
  List<JobApplicationStatus> jobStatuses = (List<JobApplicationStatus>) request.getAttribute("jobStatuses");

  // Get success/error messages
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  
  if (successMessage != null) {
    session.removeAttribute("successMessage");
  }
  if (errorMessage != null) {
    session.removeAttribute("errorMessage");
  }
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Apply for Jobs — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Loading jobs...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Apply for Jobs 💼</h1>
        <p class="page-subtitle">Browse and apply to placement opportunities</p>
      </div>
      <div class="header-actions">
        <a href="${pageContext.request.contextPath}/my-applications" class="btn-primary">
          <span>📋</span> My Applications
        </a>
      </div>
    </div>

    <!-- Success/Error Messages -->
    <% if (successMessage != null) { %>
      <div class="alert alert-success" role="status" aria-live="polite">
        <span class="alert-icon">✓</span>
        <%= com.rit.placement.util.XSSUtil.escape(successMessage) %>
      </div>
    <% } %>
    
    <% if (errorMessage != null) { %>
      <div class="alert alert-error">
        <span class="alert-icon">✕</span>
        <%= com.rit.placement.util.XSSUtil.escape(errorMessage) %>
      </div>
    <% } %>

    <!-- Jobs Grid -->
    <% if (jobStatuses != null && !jobStatuses.isEmpty()) { %>
      <div class="jobs-grid">
        <% for (JobApplicationStatus jobStatus : jobStatuses) { 
             JobPosting job = jobStatus.getJob();
             boolean isExpired = job.getDeadline() != null && 
                                 job.getDeadline().before(new java.util.Date());
             boolean hasApplied = jobStatus.isHasApplied();
             boolean isEligible = jobStatus.isEligible();
        %>
          <div class="job-card <%= com.rit.placement.util.XSSUtil.escape(isExpired ? "job-expired" : "") %>">
            <div class="job-card-header">
              <div class="job-company">
                <span class="job-icon">💼</span>
                <div>
                  <h3 class="job-title"><%= com.rit.placement.util.XSSUtil.escape(job.getRole()) %></h3>
                  <p class="job-company-name"><%= com.rit.placement.util.XSSUtil.escape(job.getCompanyName()) %></p>
                </div>
              </div>
              <% if (hasApplied) { %>
                <span class="badge badge-info">✓ Applied</span>
              <% } else if (isExpired) { %>
                <span class="badge badge-danger">Expired</span>
              <% } else if (!isEligible) { %>
                <span class="badge badge-warning">Not Eligible</span>
              <% } else { %>
                <span class="badge badge-success">Open</span>
              <% } %>
            </div>

            <div class="job-details">
              <div class="job-detail-item">
                <span class="job-detail-label">💰 Package:</span>
                <span class="job-detail-value">
                  <%= com.rit.placement.util.XSSUtil.escape(job.getPackageAmount() != null ? job.getPackageAmount() + " LPA" : "Not disclosed") %>
                </span>
              </div>
              
              <div class="job-detail-item">
                <span class="job-detail-label">📊 Min CGPA:</span>
                <span class="job-detail-value">
                  <%= com.rit.placement.util.XSSUtil.escape(job.getMinCgpa() != null ? job.getMinCgpa() : "N/A") %>
                </span>
              </div>
              
              <% if (job.getAllowedBranches() != null && !job.getAllowedBranches().isEmpty()) { %>
              <div class="job-detail-item">
                <span class="job-detail-label">🎓 Branches:</span>
                <span class="job-detail-value"><%= com.rit.placement.util.XSSUtil.escape(job.getAllowedBranches()) %></span>
              </div>
              <% } %>
              
              <div class="job-detail-item">
                <span class="job-detail-label">📅 Deadline:</span>
                <span class="job-detail-value <%= com.rit.placement.util.XSSUtil.escape(isExpired ? "text-danger" : "") %>">
                  <%= com.rit.placement.util.XSSUtil.escape(job.getDeadline() != null ? dateFormat.format(job.getDeadline()) : "N/A") %>
                </span>
              </div>
            </div>

            <% if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty()) { %>
            <div class="job-skills">
              <span class="job-skills-label">🔧 Skills:</span>
              <div class="skills-tags">
                <% 
                  String[] skills = job.getRequiredSkills().split(",");
                  for (String skill : skills) {
                    if (skill.trim().length() > 0) {
                %>
                  <span class="skill-tag"><%= com.rit.placement.util.XSSUtil.escape(skill.trim()) %></span>
                <% } } %>
              </div>
            </div>
            <% } %>

            <div class="job-card-footer">
              <span class="job-id">Job ID: <%= com.rit.placement.util.XSSUtil.escape(job.getJobId()) %></span>
              <% if (hasApplied) { %>
                <button class="btn-action" disabled>Already Applied</button>
              <% } else if (isExpired) { %>
                <button class="btn-action" disabled>Expired</button>
              <% } else if (!isEligible) { %>
                <button class="btn-action" disabled>Not Eligible</button>
              <% } else { %>
                <form action="${pageContext.request.contextPath}/apply" method="post" style="display: inline;">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                  <input type="hidden" name="job_id" value="<%= com.rit.placement.util.XSSUtil.escape(job.getJobId()) %>">
                  <button type="submit" class="btn-action" onclick="return confirm('Are you sure you want to apply for this job?')">
                    Apply Now
                  </button>
                </form>
              <% } %>
            </div>
          </div>
        <% } %>
      </div>
    <% } else { %>
      <!-- Empty State -->
      <div class="empty-state">
        <div class="empty-state-icon">💼</div>
        <h4 class="empty-state-title">No Job Postings Available</h4>
        <p class="empty-state-text">There are currently no job postings. Check back later!</p>
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
      
      var errorAlert = document.querySelector('.alert-error');
      if (errorAlert) {
        setTimeout(function() {
          errorAlert.style.opacity = '0';
          errorAlert.style.transform = 'translateY(-10px)';
          setTimeout(function() {
            errorAlert.style.display = 'none';
          }, 400);
        }, 8000);
      }
    });
  </script>
</body>
</html>
