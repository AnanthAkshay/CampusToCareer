<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Company" %>
<%@ page import="com.rit.placement.model.Application" %>
<%@ page import="com.rit.placement.model.Student" %>
<%@ page import="com.rit.placement.model.JobPosting" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  if (session.getAttribute("user_id") == null || !"COMPANY".equals(session.getAttribute("role"))) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  Company company = (Company) request.getAttribute("company");
  List<Application> applications = (List<Application>) request.getAttribute("applications");
  List<Application> allApplications = (List<Application>) request.getAttribute("allApplications");
  Map<Integer, Student> studentMap = (Map<Integer, Student>) request.getAttribute("studentMap");
  Map<Integer, Double> cgpaMap = (Map<Integer, Double>) request.getAttribute("cgpaMap");
  Map<Integer, Integer> rankMap = (Map<Integer, Integer>) request.getAttribute("rankMap");
  Map<Integer, Integer> scoreMap = (Map<Integer, Integer>) request.getAttribute("scoreMap");
  String filterStatus = (String) request.getAttribute("filterStatus");
  String filterJobId = (String) request.getAttribute("filterJobId");
  JobPosting selectedJob = (JobPosting) request.getAttribute("selectedJob");
  List<JobPosting> companyJobs = (List<JobPosting>) request.getAttribute("companyJobs");
  Integer currentPage = (Integer) request.getAttribute("currentPage");
  Integer totalPages = (Integer) request.getAttribute("totalPages");
  Integer totalApps = (Integer) request.getAttribute("totalApplications");
  
  if (currentPage == null) currentPage = 1;
  if (totalPages == null) totalPages = 1;
  if (totalApps == null) totalApps = 0;
  
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
  
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  session.removeAttribute("successMessage");
  session.removeAttribute("errorMessage");
  
  // Count by status
  int pendingCount = 0, shortlistedCount = 0, selectedCount = 0, rejectedCount = 0;
  if (allApplications != null) {
    for (Application app : allApplications) {
      String status = app.getStatus();
      if ("PENDING".equals(status)) pendingCount++;
      else if ("SHORTLISTED".equals(status)) shortlistedCount++;
      else if ("SELECTED".equals(status)) selectedCount++;
      else if ("REJECTED".equals(status)) rejectedCount++;
    }
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Applications — <%= com.rit.placement.util.XSSUtil.escape(company != null ? company.getCompanyName() : "Company") %></title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
    }
    .navbar {
      background: rgba(44, 62, 80, 0.95);
      color: white;
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .navbar h1 { font-size: 1.5rem; }
    .navbar a {
      color: white;
      text-decoration: none;
      margin-left: 1rem;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background 0.3s;
    }
    .navbar a:hover { background: rgba(255,255,255,0.1); }
    .container { max-width: 1600px; margin: 2rem auto; padding: 0 2rem; }
    
    .alert {
      padding: 1rem;
      border-radius: 8px;
      margin-bottom: 1.5rem;
    }
    .alert-success { background: #d1fae5; color: #065f46; border: 1px solid #10b981; }
    .alert-error { background: #fee2e2; color: #991b1b; border: 1px solid #ef4444; }
    
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }
    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      cursor: pointer;
      transition: transform 0.3s;
    }
    .stat-card:hover { transform: translateY(-3px); }
    .stat-card.active { border: 2px solid #3b82f6; }
    .stat-label { color: #6b7280; font-size: 0.9rem; margin-bottom: 0.5rem; }
    .stat-value { font-size: 2rem; font-weight: bold; color: #2c3e50; }
    
    .section {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .section h3 { color: #2c3e50; margin-bottom: 1.5rem; }
    
    table {
      width: 100%;
      border-collapse: collapse;
    }
    th, td {
      padding: 1rem;
      text-align: left;
      border-bottom: 1px solid #e5e7eb;
    }
    th {
      background: #f9fafb;
      font-weight: 600;
      color: #374151;
    }
    tr:hover { background: #f9fafb; }
    
    .badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-shortlisted { background: #dbeafe; color: #1e40af; }
    .badge-interview { background: #e0e7ff; color: #4338ca; }
    .badge-selected { background: #d1fae5; color: #065f46; }
    .badge-rejected { background: #fee2e2; color: #991b1b; }
    
    /* Timeline Styles */
    .timeline-container {
      display: flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.5rem 0;
      min-width: 400px;
    }
    .timeline-step {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.25rem;
    }
    .timeline-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background: #d1d5db;
      border: 2px solid #e5e7eb;
      transition: all 0.3s;
    }
    .timeline-step.active .timeline-dot {
      background: #3b82f6;
      border-color: #2563eb;
    }
    .timeline-step.current .timeline-dot {
      width: 16px;
      height: 16px;
      background: #10b981;
      border-color: #059669;
      box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2);
    }
    .timeline-label {
      font-size: 0.7rem;
      color: #9ca3af;
      white-space: nowrap;
    }
    .timeline-step.active .timeline-label {
      color: #3b82f6;
      font-weight: 600;
    }
    .timeline-step.current .timeline-label {
      color: #10b981;
      font-weight: 700;
    }
    .timeline-line {
      flex: 1;
      height: 2px;
      background: #e5e7eb;
      min-width: 20px;
      transition: all 0.3s;
    }
    .timeline-line.active {
      background: #3b82f6;
    }
    .timeline-rejected {
      margin-left: 1rem;
    }
    
    /* Pagination Styles */
    .pagination-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 2rem;
      padding: 1rem 0;
      border-top: 1px solid #e5e7eb;
    }
    .pagination-info {
      color: #6b7280;
      font-size: 0.9rem;
    }
    .pagination {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }
    .page-link {
      padding: 0.5rem 0.75rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      color: #374151;
      text-decoration: none;
      transition: all 0.2s;
      font-size: 0.9rem;
    }
    .page-link:hover {
      background: #f3f4f6;
      border-color: #9ca3af;
    }
    .page-link.active {
      background: #3b82f6;
      color: white;
      border-color: #3b82f6;
    }
    .page-ellipsis {
      color: #9ca3af;
      padding: 0 0.25rem;
    }
    
    .status-select {
      padding: 0.5rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-size: 0.9rem;
    }
    
    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 6px;
      font-size: 0.9rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }
    .btn-primary {
      background: #3b82f6;
      color: white;
    }
    .btn-primary:hover { background: #2563eb; }
    
    .student-details {
      font-size: 0.85rem;
      color: #6b7280;
      margin-top: 0.25rem;
    }
  </style>
</head>
<body>
  <div class="navbar">
    <h1>🏢 <%= com.rit.placement.util.XSSUtil.escape(company != null ? company.getCompanyName() : "Company Portal") %></h1>
    <div>
      <a href="${pageContext.request.contextPath}/company/dashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/company/jobs">Manage Jobs</a>
      <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
  </div>

  <div class="container">
    <% if (successMessage != null) { %>
      <div class="alert alert-success"><%= com.rit.placement.util.XSSUtil.escape(successMessage) %></div>
    <% } %>
    <% if (errorMessage != null) { %>
      <div class="alert alert-error"><%= com.rit.placement.util.XSSUtil.escape(errorMessage) %></div>
    <% } %>

    <!-- Filter Stats -->
    <div class="stats-grid">
      <a href="${pageContext.request.contextPath}/company/applications" style="text-decoration: none;">
        <div class="stat-card <%= com.rit.placement.util.XSSUtil.escape(filterStatus == null || "".equals(filterStatus) ? "active" : "") %>">
          <div class="stat-label">All Applications</div>
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(allApplications != null ? allApplications.size() : 0) %></div>
        </div>
      </a>
      <a href="${pageContext.request.contextPath}/company/applications?status=PENDING" style="text-decoration: none;">
        <div class="stat-card <%= com.rit.placement.util.XSSUtil.escape("PENDING".equals(filterStatus) ? "active" : "") %>">
          <div class="stat-label">Pending Review</div>
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(pendingCount) %></div>
        </div>
      </a>
      <a href="${pageContext.request.contextPath}/company/applications?status=SHORTLISTED" style="text-decoration: none;">
        <div class="stat-card <%= com.rit.placement.util.XSSUtil.escape("SHORTLISTED".equals(filterStatus) ? "active" : "") %>">
          <div class="stat-label">Shortlisted</div>
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(shortlistedCount) %></div>
        </div>
      </a>
      <a href="${pageContext.request.contextPath}/company/applications?status=SELECTED" style="text-decoration: none;">
        <div class="stat-card <%= com.rit.placement.util.XSSUtil.escape("SELECTED".equals(filterStatus) ? "active" : "") %>">
          <div class="stat-label">Selected</div>
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(selectedCount) %></div>
        </div>
      </a>
      <a href="${pageContext.request.contextPath}/company/applications?status=REJECTED" style="text-decoration: none;">
        <div class="stat-card <%= com.rit.placement.util.XSSUtil.escape("REJECTED".equals(filterStatus) ? "active" : "") %>">
          <div class="stat-label">Rejected</div>
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(rejectedCount) %></div>
        </div>
      </a>
    </div>

    <!-- Applications Table -->
    <div class="section">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
        <h3 style="margin: 0;">📝 Applications <%= com.rit.placement.util.XSSUtil.escape(filterStatus != null && !filterStatus.isEmpty() ? "(" + filterStatus + ")" : "") %></h3>
        
        <!-- Job Filter -->
        <% if (companyJobs != null && companyJobs.size() > 1) { %>
        <div style="display: flex; gap: 1rem; align-items: center;">
          <label for="jobFilter" style="font-weight: 600; color: #374151;">Filter by Job:</label>
          <select id="jobFilter" onchange="filterByJob(this.value)" style="padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 6px; font-size: 0.9rem;">
            <option value="">All Jobs</option>
            <% for (JobPosting job : companyJobs) { %>
              <option value="<%= com.rit.placement.util.XSSUtil.escape(job.getJobId()) %>" <%= com.rit.placement.util.XSSUtil.escape(filterJobId != null && filterJobId.equals(String.valueOf(job.getJobId())) ? "selected" : "") %>>
                <%= com.rit.placement.util.XSSUtil.escape(job.getRole()) %>
              </option>
            <% } %>
          </select>
          <% if (selectedJob != null) { %>
            <span style="background: #dbeafe; color: #1e40af; padding: 0.5rem 1rem; border-radius: 6px; font-size: 0.85rem; font-weight: 600;">
              🎯 Ranked by Match Score
            </span>
          <% } %>
        </div>
        <% } %>
      </div>
      
      <% if (applications != null && !applications.isEmpty()) { %>
        <table>
          <thead>
            <tr>
              <% if (selectedJob != null) { %>
              <th style="width: 80px;">Rank</th>
              <th style="width: 80px;">Score</th>
              <% } %>
              <th>Student</th>
              <th>Job Role</th>
              <th>CGPA</th>
              <th>Skills</th>
              <th>Applied Date</th>
              <th>Application Timeline</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <% for (Application app : applications) {
                Student student = studentMap != null ? studentMap.get(app.getStudentId()) : null;
                Double cgpa = cgpaMap != null ? cgpaMap.get(app.getStudentId()) : null;
                Integer rank = rankMap != null ? rankMap.get(app.getStudentId()) : null;
                Integer score = scoreMap != null ? scoreMap.get(app.getStudentId()) : null;
            %>
            <tr>
              <% if (selectedJob != null && rank != null && score != null) { %>
              <!-- Rank Column -->
              <td style="text-align: center;">
                <div style="font-size: 1.5rem; font-weight: 700;">
                  <% if (rank == 1) { %>
                    🥇
                  <% } else if (rank == 2) { %>
                    🥈
                  <% } else if (rank == 3) { %>
                    🥉
                  <% } else { %>
                    #<%= com.rit.placement.util.XSSUtil.escape(rank) %>
                  <% } %>
                </div>
              </td>
              <!-- Score Column -->
              <td style="text-align: center;">
                <div style="font-size: 1.2rem; font-weight: 700; color: <%= com.rit.placement.util.XSSUtil.escape(score >= 85 ? "#10b981" : 
                  score >= 70 ? "#3b82f6" : 
                  score >= 55 ? "#8b5cf6" : 
                  score >= 40 ? "#f59e0b" : "#6b7280") %>;">
                  <%= com.rit.placement.util.XSSUtil.escape(score) %>
                </div>
                <div style="font-size: 0.7rem; color: #6b7280; font-weight: 600;">
                  <%= com.rit.placement.util.XSSUtil.escape(score >= 85 ? "Excellent" : 
                      score >= 70 ? "Strong" : 
                      score >= 55 ? "Good" : 
                      score >= 40 ? "Fair" : "Consider") %>
                </div>
              </td>
              <% } %>
              <td>
                <strong><%= com.rit.placement.util.XSSUtil.escape(app.getStudentName() != null ? app.getStudentName() : "N/A") %></strong>
                <div class="student-details">USN: <%= com.rit.placement.util.XSSUtil.escape(app.getStudentUsn() != null ? app.getStudentUsn() : "N/A") %></div>
              </td>
              <td><%= com.rit.placement.util.XSSUtil.escape(app.getJobTitle() != null ? app.getJobTitle() : "N/A") %></td>
              <td><%= com.rit.placement.util.XSSUtil.escape(cgpa != null ? String.format("%.2f", cgpa) : "N/A") %></td>
              <td>
                <% if (student != null && student.getSkills() != null && !student.getSkills().trim().isEmpty()) { %>
                  <%= com.rit.placement.util.XSSUtil.escape(student.getSkills().length() > 50 ? student.getSkills().substring(0, 50) + "..." : student.getSkills()) %>
                <% } else { %>
                  <span style="color: #9ca3af;">Not specified</span>
                <% } %>
              </td>
              <td><%= com.rit.placement.util.XSSUtil.escape(app.getAppliedDate() != null ? dateFormat.format(app.getAppliedDate()) : "N/A") %></td>
              <td>
                <!-- Dynamic Timeline -->
                <div class="timeline-container">
                  <% 
                    String currentStatus = app.getStatus() != null ? app.getStatus() : "PENDING";
                    boolean isPending = "PENDING".equals(currentStatus);
                    boolean isShortlisted = "SHORTLISTED".equals(currentStatus);
                    boolean isInterview = "INTERVIEW".equals(currentStatus);
                    boolean isSelected = "SELECTED".equals(currentStatus);
                    boolean isRejected = "REJECTED".equals(currentStatus);
                  %>
                  <div class="timeline-step <%= com.rit.placement.util.XSSUtil.escape(isPending || isShortlisted || isInterview || isSelected ? "active" : "") %> <%= com.rit.placement.util.XSSUtil.escape(isPending ? "current" : "") %>">
                    <div class="timeline-dot"></div>
                    <div class="timeline-label">Pending</div>
                  </div>
                  <div class="timeline-line <%= com.rit.placement.util.XSSUtil.escape(isShortlisted || isInterview || isSelected ? "active" : "") %>"></div>
                  <div class="timeline-step <%= com.rit.placement.util.XSSUtil.escape(isShortlisted || isInterview || isSelected ? "active" : "") %> <%= com.rit.placement.util.XSSUtil.escape(isShortlisted ? "current" : "") %>">
                    <div class="timeline-dot"></div>
                    <div class="timeline-label">Shortlisted</div>
                  </div>
                  <div class="timeline-line <%= com.rit.placement.util.XSSUtil.escape(isInterview || isSelected ? "active" : "") %>"></div>
                  <div class="timeline-step <%= com.rit.placement.util.XSSUtil.escape(isInterview || isSelected ? "active" : "") %> <%= com.rit.placement.util.XSSUtil.escape(isInterview ? "current" : "") %>">
                    <div class="timeline-dot"></div>
                    <div class="timeline-label">Interview</div>
                  </div>
                  <div class="timeline-line <%= com.rit.placement.util.XSSUtil.escape(isSelected ? "active" : "") %>"></div>
                  <div class="timeline-step <%= com.rit.placement.util.XSSUtil.escape(isSelected ? "active" : "") %> <%= com.rit.placement.util.XSSUtil.escape(isSelected ? "current" : "") %>">
                    <div class="timeline-dot"></div>
                    <div class="timeline-label">Selected</div>
                  </div>
                  <% if (isRejected) { %>
                  <div class="timeline-rejected">
                    <span class="badge badge-rejected">✕ Rejected</span>
                  </div>
                  <% } %>
                </div>
              </td>
              <td>
                <form method="post" action="${pageContext.request.contextPath}/company/applications" style="display: inline;">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">


                  <input type="hidden" name="action" value="update_status">
                  <input type="hidden" name="application_id" value="<%= com.rit.placement.util.XSSUtil.escape(app.getId()) %>">
                  <select name="status" class="status-select" onchange="this.form.submit()">
                    <option value="">Change Status</option>
                    <option value="PENDING" <%= com.rit.placement.util.XSSUtil.escape("PENDING".equals(app.getStatus()) ? "selected" : "") %>>Pending</option>
                    <option value="SHORTLISTED" <%= com.rit.placement.util.XSSUtil.escape("SHORTLISTED".equals(app.getStatus()) ? "selected" : "") %>>Shortlist</option>
                    <option value="INTERVIEW" <%= com.rit.placement.util.XSSUtil.escape("INTERVIEW".equals(app.getStatus()) ? "selected" : "") %>>Interview</option>
                    <option value="SELECTED" <%= com.rit.placement.util.XSSUtil.escape("SELECTED".equals(app.getStatus()) ? "selected" : "") %>>Select</option>
                    <option value="REJECTED" <%= com.rit.placement.util.XSSUtil.escape("REJECTED".equals(app.getStatus()) ? "selected" : "") %>>Reject</option>
                  </select>
                </form>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      <% } else { %>
        <p style="text-align: center; color: #6b7280; padding: 3rem;">
          No applications <%= com.rit.placement.util.XSSUtil.escape(filterStatus != null && !filterStatus.isEmpty() ? "with status " + filterStatus : "") %> found.
        </p>
      <% } %>
    </div>
    
    <!-- Pagination -->
    <% if (totalPages > 1) { %>
    <div class="pagination-container">
      <div class="pagination-info">
        Showing <%= com.rit.placement.util.XSSUtil.escape(applications != null && !applications.isEmpty() ? ((currentPage - 1) * 20 + 1) : 0) %> 
        to <%= com.rit.placement.util.XSSUtil.escape(applications != null ? Math.min(currentPage * 20, totalApps) : 0) %> 
        of <%= com.rit.placement.util.XSSUtil.escape(totalApps) %> applications
      </div>
      <div class="pagination">
        <% if (currentPage > 1) { %>
          <a href="?page=<%= com.rit.placement.util.XSSUtil.escape(currentPage - 1) %><%= com.rit.placement.util.XSSUtil.escape(filterStatus != null ? "&status=" + filterStatus : "") %>" class="page-link">← Previous</a>
        <% } %>
        
        <% 
          int startPage = Math.max(1, currentPage - 2);
          int endPage = Math.min(totalPages, currentPage + 2);
          
          if (startPage > 1) { %>
            <a href="?page=1<%= com.rit.placement.util.XSSUtil.escape(filterStatus != null ? "&status=" + filterStatus : "") %>" class="page-link">1</a>
            <% if (startPage > 2) { %>
              <span class="page-ellipsis">...</span>
            <% } %>
          <% }
          
          for (int i = startPage; i <= endPage; i++) { %>
            <a href="?page=<%= com.rit.placement.util.XSSUtil.escape(i) %><%= com.rit.placement.util.XSSUtil.escape(filterStatus != null ? "&status=" + filterStatus : "") %>" 
               class="page-link <%= com.rit.placement.util.XSSUtil.escape(i == currentPage ? "active" : "") %>"><%= com.rit.placement.util.XSSUtil.escape(i) %></a>
          <% }
          
          if (endPage < totalPages) { 
            if (endPage < totalPages - 1) { %>
              <span class="page-ellipsis">...</span>
            <% } %>
            <a href="?page=<%= com.rit.placement.util.XSSUtil.escape(totalPages) %><%= com.rit.placement.util.XSSUtil.escape(filterStatus != null ? "&status=" + filterStatus : "") %>" class="page-link"><%= com.rit.placement.util.XSSUtil.escape(totalPages) %></a>
          <% } %>
        
        <% if (currentPage < totalPages) { %>
          <a href="?page=<%= com.rit.placement.util.XSSUtil.escape(currentPage + 1) %><%= com.rit.placement.util.XSSUtil.escape(filterStatus != null ? "&status=" + filterStatus : "") %>" class="page-link">Next →</a>
        <% } %>
      </div>
    </div>
    <% } %>
  </div>
</body>
</html>

  <script>
    function filterByJob(jobId) {
      const currentUrl = new URL(window.location.href);
      if (jobId) {
        currentUrl.searchParams.set('job_id', jobId);
      } else {
        currentUrl.searchParams.delete('job_id');
      }
      // Reset to page 1 when changing filter
      currentUrl.searchParams.delete('page');
      window.location.href = currentUrl.toString();
    }
  </script>
</body>
</html>
