<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Company" %>
<%@ page import="com.rit.placement.model.JobPosting" %>
<%@ page import="com.rit.placement.model.Application" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null || !"COMPANY".equals(session.getAttribute("role"))) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  Company company = (Company) request.getAttribute("company");
  List<JobPosting> jobPostings = (List<JobPosting>) request.getAttribute("jobPostings");
  List<Application> applications = (List<Application>) request.getAttribute("applications");
  
  Integer totalJobs = (Integer) request.getAttribute("totalJobs");
  Integer activeJobs = (Integer) request.getAttribute("activeJobs");
  Integer totalApplications = (Integer) request.getAttribute("totalApplications");
  Long pendingApplications = (Long) request.getAttribute("pendingApplications");
  Long shortlistedApplications = (Long) request.getAttribute("shortlistedApplications");
  Long selectedApplications = (Long) request.getAttribute("selectedApplications");
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Company Dashboard — <%= company != null ? company.getCompanyName() : "Company" %></title>
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
      backdrop-filter: blur(10px);
      color: white;
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
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
    .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
    .header {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      margin-bottom: 2rem;
      box-shadow: 0 8px 16px rgba(0,0,0,0.1);
    }
    .header h2 { color: #2c3e50; margin-bottom: 0.5rem; }
    .header p { color: #7f8c8d; }
    
    /* Stats Grid */
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }
    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      transition: transform 0.3s;
    }
    .stat-card:hover { transform: translateY(-5px); }
    .stat-label {
      color: #7f8c8d;
      font-size: 0.85rem;
      text-transform: uppercase;
      margin-bottom: 0.5rem;
    }
    .stat-value {
      font-size: 2rem;
      font-weight: bold;
      color: #2c3e50;
    }
    .stat-card:nth-child(1) { border-top: 4px solid #3b82f6; }
    .stat-card:nth-child(2) { border-top: 4px solid #10b981; }
    .stat-card:nth-child(3) { border-top: 4px solid #f59e0b; }
    .stat-card:nth-child(4) { border-top: 4px solid #ef4444; }
    .stat-card:nth-child(5) { border-top: 4px solid #8b5cf6; }
    .stat-card:nth-child(6) { border-top: 4px solid #06b6d4; }
    
    /* Section */
    .section {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      margin-bottom: 2rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .section h3 {
      color: #2c3e50;
      margin-bottom: 1.5rem;
      font-size: 1.3rem;
    }
    
    /* Table */
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
    
    /* Status Badge */
    .badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .badge-pending { background: #fef3c7; color: #92400e; }
    .badge-shortlisted { background: #dbeafe; color: #1e40af; }
    .badge-selected { background: #d1fae5; color: #065f46; }
    .badge-rejected { background: #fee2e2; color: #991b1b; }
    .badge-active { background: #d1fae5; color: #065f46; }
    .badge-expired { background: #f3f4f6; color: #6b7280; }
    
    .empty-state {
      text-align: center;
      padding: 3rem;
      color: #6b7280;
    }
    .empty-state-icon { font-size: 3rem; margin-bottom: 1rem; }
  </style>
</head>
<body>
  <div class="navbar">
    <h1>🏢 <%= company != null ? company.getCompanyName() : "Company Portal" %></h1>
    <div>
      <span>Welcome, <strong><%= session.getAttribute("name") %></strong></span>
      <a href="${pageContext.request.contextPath}/company/jobs">Manage Jobs</a>
      <a href="${pageContext.request.contextPath}/company/applications">Applications</a>
      <a href="${pageContext.request.contextPath}/company/interviews">Interviews</a>
      <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
  </div>

  <div class="container">
    <div class="header">
      <h2>Company Dashboard</h2>
      <p><%= company != null ? company.getDescription() : "" %></p>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">Total Jobs Posted</div>
        <div class="stat-value"><%= totalJobs != null ? totalJobs : 0 %></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Active Jobs</div>
        <div class="stat-value"><%= activeJobs != null ? activeJobs : 0 %></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Total Applications</div>
        <div class="stat-value"><%= totalApplications != null ? totalApplications : 0 %></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Pending Review</div>
        <div class="stat-value"><%= pendingApplications != null ? pendingApplications : 0 %></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Shortlisted</div>
        <div class="stat-value"><%= shortlistedApplications != null ? shortlistedApplications : 0 %></div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Selected</div>
        <div class="stat-value"><%= selectedApplications != null ? selectedApplications : 0 %></div>
      </div>
    </div>

    <!-- Job Postings Section -->
    <div class="section">
      <h3>📋 Your Job Postings</h3>
      <% if (jobPostings != null && !jobPostings.isEmpty()) { %>
        <table>
          <thead>
            <tr>
              <th>Role</th>
              <th>Package (LPA)</th>
              <th>Min CGPA</th>
              <th>Deadline</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <% for (JobPosting job : jobPostings) {
                boolean isActive = job.getDeadline() != null && 
                                  !job.getDeadline().before(new java.sql.Date(System.currentTimeMillis()));
            %>
            <tr>
              <td><strong><%= job.getRole() %></strong></td>
              <td><%= job.getPackageAmount() %></td>
              <td><%= job.getMinCgpa() %></td>
              <td><%= job.getDeadline() != null ? dateFormat.format(job.getDeadline()) : "N/A" %></td>
              <td>
                <span class="badge <%= isActive ? "badge-active" : "badge-expired" %>">
                  <%= isActive ? "Active" : "Expired" %>
                </span>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      <% } else { %>
        <div class="empty-state">
          <div class="empty-state-icon">📭</div>
          <p>No job postings yet. Contact admin to post jobs.</p>
        </div>
      <% } %>
    </div>

    <!-- Applications Section -->
    <div class="section">
      <h3>📝 Applications Received</h3>
      <% if (applications != null && !applications.isEmpty()) { %>
        <table>
          <thead>
            <tr>
              <th>Student Name</th>
              <th>USN</th>
              <th>Job Role</th>
              <th>Applied Date</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <% for (Application app : applications) { %>
            <tr>
              <td><%= app.getStudentName() != null ? app.getStudentName() : "N/A" %></td>
              <td><%= app.getStudentUsn() != null ? app.getStudentUsn() : "N/A" %></td>
              <td><%= app.getJobTitle() != null ? app.getJobTitle() : "N/A" %></td>
              <td><%= app.getAppliedDate() != null ? dateFormat.format(app.getAppliedDate()) : "N/A" %></td>
              <td>
                <span class="badge badge-<%= app.getStatus() != null ? app.getStatus().toLowerCase() : "pending" %>">
                  <%= app.getStatus() != null ? app.getStatus() : "PENDING" %>
                </span>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      <% } else { %>
        <div class="empty-state">
          <div class="empty-state-icon">📭</div>
          <p>No applications received yet.</p>
        </div>
      <% } %>
    </div>
  </div>
</body>
</html>
