<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.JobPosting" %>
<%@ page import="com.rit.placement.model.StudentEligibility" %>
<%@ page import="java.util.List" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }

  // Read data from request attributes
  JobPosting job = (JobPosting) request.getAttribute("job");
  
  @SuppressWarnings("unchecked")
  List<StudentEligibility> eligibleStudents = (List<StudentEligibility>) request.getAttribute("eligibleStudents");
  
  @SuppressWarnings("unchecked")
  List<StudentEligibility> ineligibleStudents = (List<StudentEligibility>) request.getAttribute("ineligibleStudents");
  
  Integer totalStudents = (Integer) request.getAttribute("totalStudents");
  Integer eligibleCount = (Integer) request.getAttribute("eligibleCount");
  Integer ineligibleCount = (Integer) request.getAttribute("ineligibleCount");
  
  // Calculate percentage
  double eligibilityPercentage = totalStudents > 0 ? (eligibleCount * 100.0 / totalStudents) : 0;
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Eligible Students — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Calculating eligibility...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Eligible Students 🎯</h1>
        <p class="page-subtitle">
          <% if (job != null) { %>
            <%=job.getRole()%> at <%=job.getCompanyName()%>
          <% } %>
        </p>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" onclick="window.history.back()">
          <span>←</span> Back to Jobs
        </button>
      </div>
    </div>

    <!-- Job Details Card -->
    <% if (job != null) { %>
    <div class="job-details-card">
      <div class="job-details-header">
        <h3>📋 Job Requirements</h3>
      </div>
      <div class="job-requirements-grid">
        <div class="requirement-item">
          <span class="requirement-label">Min CGPA:</span>
          <span class="requirement-value">
            <%= job.getMinCgpa() != null ? job.getMinCgpa() : "N/A" %>
          </span>
        </div>
        <div class="requirement-item">
          <span class="requirement-label">Allowed Branches:</span>
          <span class="requirement-value">
            <%= job.getAllowedBranches() != null && !job.getAllowedBranches().isEmpty() 
                ? job.getAllowedBranches() : "All Branches" %>
          </span>
        </div>
        <div class="requirement-item">
          <span class="requirement-label">Required Skills:</span>
          <span class="requirement-value">
            <%= job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty() 
                ? job.getRequiredSkills() : "Not specified" %>
          </span>
        </div>
        <div class="requirement-item">
          <span class="requirement-label">Package:</span>
          <span class="requirement-value">
            <%= job.getPackageAmount() != null ? job.getPackageAmount() + " LPA" : "Not disclosed" %>
          </span>
        </div>
      </div>
    </div>
    <% } %>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card stat-card-blue">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-label">Total Students</div>
          <div class="stat-value"><%=totalStudents%></div>
        </div>
      </div>

      <div class="stat-card stat-card-gradient">
        <div class="stat-icon">✓</div>
        <div class="stat-content">
          <div class="stat-label">Eligible Students</div>
          <div class="stat-value"><%=eligibleCount%></div>
          <div class="stat-trend">
            <span class="trend-indicator"><%=String.format("%.1f", eligibilityPercentage)%>% of total</span>
          </div>
        </div>
      </div>

      <div class="stat-card stat-card-purple">
        <div class="stat-icon">✕</div>
        <div class="stat-content">
          <div class="stat-label">Not Eligible</div>
          <div class="stat-value"><%=ineligibleCount%></div>
        </div>
      </div>
    </div>

    <!-- Filter Tabs -->
    <div class="filter-tabs">
      <button class="filter-tab active" onclick="showTab('eligible')">
        ✓ Eligible (<%=eligibleCount%>)
      </button>
      <button class="filter-tab" onclick="showTab('ineligible')">
        ✕ Not Eligible (<%=ineligibleCount%>)
      </button>
      <button class="filter-tab" onclick="showTab('all')">
        👥 All Students (<%=totalStudents%>)
      </button>
    </div>

    <!-- Eligible Students Table -->
    <div id="eligibleTab" class="students-table-container">
      <% if (eligibleStudents != null && !eligibleStudents.isEmpty()) { %>
        <div class="table-wrap">
          <table class="students-table">
            <thead>
              <tr>
                <th>USN</th>
                <th>Name</th>
                <th>Branch</th>
                <th>CGPA</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <% for (StudentEligibility student : eligibleStudents) { %>
                <tr class="student-row eligible-row">
                  <td class="usn-cell"><%=student.getUsn()%></td>
                  <td class="name-cell"><%=student.getName()%></td>
                  <td class="branch-cell"><%=student.getBranch()%></td>
                  <td class="cgpa-cell">
                    <span class="cgpa-badge"><%=String.format("%.2f", student.getCgpa())%></span>
                  </td>
                  <td class="status-cell">
                    <span class="badge badge-success">✓ Eligible</span>
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      <% } else { %>
        <div class="empty-state">
          <div class="empty-state-icon">🎯</div>
          <h4 class="empty-state-title">No Eligible Students</h4>
          <p class="empty-state-text">No students meet the eligibility criteria for this job posting.</p>
        </div>
      <% } %>
    </div>

    <!-- Ineligible Students Table -->
    <div id="ineligibleTab" class="students-table-container" style="display: none;">
      <% if (ineligibleStudents != null && !ineligibleStudents.isEmpty()) { %>
        <div class="table-wrap">
          <table class="students-table">
            <thead>
              <tr>
                <th>USN</th>
                <th>Name</th>
                <th>Branch</th>
                <th>CGPA</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <% for (StudentEligibility student : ineligibleStudents) { %>
                <tr class="student-row ineligible-row">
                  <td class="usn-cell"><%=student.getUsn()%></td>
                  <td class="name-cell"><%=student.getName()%></td>
                  <td class="branch-cell"><%=student.getBranch()%></td>
                  <td class="cgpa-cell">
                    <span class="cgpa-badge"><%=String.format("%.2f", student.getCgpa())%></span>
                  </td>
                  <td class="status-cell">
                    <span class="badge badge-danger">✕ Not Eligible</span>
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      <% } else { %>
        <div class="empty-state">
          <div class="empty-state-icon">✓</div>
          <h4 class="empty-state-title">All Students Eligible</h4>
          <p class="empty-state-text">All students meet the eligibility criteria!</p>
        </div>
      <% } %>
    </div>

    <!-- All Students Table -->
    <div id="allTab" class="students-table-container" style="display: none;">
      <% if ((eligibleStudents != null && !eligibleStudents.isEmpty()) || 
             (ineligibleStudents != null && !ineligibleStudents.isEmpty())) { %>
        <div class="table-wrap">
          <table class="students-table">
            <thead>
              <tr>
                <th>USN</th>
                <th>Name</th>
                <th>Branch</th>
                <th>CGPA</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <% if (eligibleStudents != null) {
                   for (StudentEligibility student : eligibleStudents) { %>
                <tr class="student-row eligible-row">
                  <td class="usn-cell"><%=student.getUsn()%></td>
                  <td class="name-cell"><%=student.getName()%></td>
                  <td class="branch-cell"><%=student.getBranch()%></td>
                  <td class="cgpa-cell">
                    <span class="cgpa-badge"><%=String.format("%.2f", student.getCgpa())%></span>
                  </td>
                  <td class="status-cell">
                    <span class="badge badge-success">✓ Eligible</span>
                  </td>
                </tr>
              <% } } %>
              
              <% if (ineligibleStudents != null) {
                   for (StudentEligibility student : ineligibleStudents) { %>
                <tr class="student-row ineligible-row">
                  <td class="usn-cell"><%=student.getUsn()%></td>
                  <td class="name-cell"><%=student.getName()%></td>
                  <td class="branch-cell"><%=student.getBranch()%></td>
                  <td class="cgpa-cell">
                    <span class="cgpa-badge"><%=String.format("%.2f", student.getCgpa())%></span>
                  </td>
                  <td class="status-cell">
                    <span class="badge badge-danger">✕ Not Eligible</span>
                  </td>
                </tr>
              <% } } %>
            </tbody>
          </table>
        </div>
      <% } %>
    </div>
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

    // Tab Switching
    function showTab(tabName) {
      // Hide all tabs
      document.getElementById('eligibleTab').style.display = 'none';
      document.getElementById('ineligibleTab').style.display = 'none';
      document.getElementById('allTab').style.display = 'none';

      // Remove active class from all buttons
      var tabs = document.querySelectorAll('.filter-tab');
      tabs.forEach(function(tab) {
        tab.classList.remove('active');
      });

      // Show selected tab
      if (tabName === 'eligible') {
        document.getElementById('eligibleTab').style.display = 'block';
        tabs[0].classList.add('active');
      } else if (tabName === 'ineligible') {
        document.getElementById('ineligibleTab').style.display = 'block';
        tabs[1].classList.add('active');
      } else if (tabName === 'all') {
        document.getElementById('allTab').style.display = 'block';
        tabs[2].classList.add('active');
      }
    }
  </script>
</body>
</html>
