<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Company" %>
<%@ page import="com.rit.placement.model.JobPosting" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  if (session.getAttribute("user_id") == null || !"COMPANY".equals(session.getAttribute("role"))) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  Company company = (Company) request.getAttribute("company");
  List<JobPosting> jobs = (List<JobPosting>) request.getAttribute("jobs");
  JobPosting editJob = (JobPosting) request.getAttribute("editJob");
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  session.removeAttribute("successMessage");
  session.removeAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Manage Jobs — <%= com.rit.placement.util.XSSUtil.escape(company != null ? company.getCompanyName() : "Company") %></title>
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
    .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
    
    .alert {
      padding: 1rem;
      border-radius: 8px;
      margin-bottom: 1.5rem;
    }
    .alert-success { background: #d1fae5; color: #065f46; border: 1px solid #10b981; }
    .alert-error { background: #fee2e2; color: #991b1b; border: 1px solid #ef4444; }
    
    .section {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      margin-bottom: 2rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .section h3 { color: #2c3e50; margin-bottom: 1.5rem; }
    
    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .form-group {
      display: flex;
      flex-direction: column;
    }
    .form-group label {
      font-weight: 600;
      margin-bottom: 0.5rem;
      color: #374151;
    }
    .form-group input, .form-group textarea, .form-group select {
      padding: 0.75rem;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-size: 1rem;
    }
    .form-group textarea { min-height: 80px; resize: vertical; }
    
    .btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 6px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
    }
    .btn-primary {
      background: #3b82f6;
      color: white;
    }
    .btn-primary:hover { background: #2563eb; }
    .btn-secondary {
      background: #6b7280;
      color: white;
    }
    .btn-secondary:hover { background: #4b5563; }
    .btn-danger {
      background: #ef4444;
      color: white;
    }
    .btn-danger:hover { background: #dc2626; }
    .btn-small {
      padding: 0.5rem 1rem;
      font-size: 0.9rem;
    }
    
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
    .badge-active { background: #d1fae5; color: #065f46; }
    .badge-expired { background: #f3f4f6; color: #6b7280; }
    
    .actions {
      display: flex;
      gap: 0.5rem;
    }
  </style>
</head>
<body>
  <div class="navbar">
    <h1>🏢 <%= com.rit.placement.util.XSSUtil.escape(company != null ? company.getCompanyName() : "Company Portal") %></h1>
    <div>
      <a href="${pageContext.request.contextPath}/company/dashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/company/applications">Applications</a>
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

    <!-- Add/Edit Job Form -->
    <div class="section">
      <h3><%= com.rit.placement.util.XSSUtil.escape(editJob != null ? "✏️ Edit Job Posting" : "➕ Post New Job") %></h3>
      <form method="post" action="${pageContext.request.contextPath}/company/jobs">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

        <input type="hidden" name="action" value="<%= com.rit.placement.util.XSSUtil.escape(editJob != null ? "update" : "add") %>">
        <% if (editJob != null) { %>
          <input type="hidden" name="job_id" value="<%= com.rit.placement.util.XSSUtil.escape(editJob.getJobId()) %>">
        <% } %>
        
        <div class="form-grid">
          <div class="form-group">
            <label>Job Role *</label>
            <input type="text" name="role" required 
                   value="<%= com.rit.placement.util.XSSUtil.escape(editJob != null ? editJob.getRole() : "") %>">
          </div>
          <div class="form-group">
            <label>Package (LPA) *</label>
            <input type="number" name="package" step="0.01" required 
                   value="<%= com.rit.placement.util.XSSUtil.escape(editJob != null ? editJob.getPackageAmount() : "") %>">
          </div>
          <div class="form-group">
            <label>Min CGPA *</label>
            <input type="number" name="min_cgpa" step="0.01" min="0" max="10" required 
                   value="<%= com.rit.placement.util.XSSUtil.escape(editJob != null ? editJob.getMinCgpa() : "") %>">
          </div>
          <div class="form-group">
            <label>Deadline *</label>
            <input type="date" name="deadline" required 
                   value="<%= com.rit.placement.util.XSSUtil.escape(editJob != null && editJob.getDeadline() != null ? dateFormat.format(editJob.getDeadline()) : "") %>">
          </div>
        </div>
        
        <div class="form-grid">
          <div class="form-group">
            <label>Allowed Branches (comma-separated)</label>
            <input type="text" name="allowed_branches" placeholder="e.g., CSE, ISE, ECE" 
                   value="<%= com.rit.placement.util.XSSUtil.escape(editJob != null && editJob.getAllowedBranches() != null ? editJob.getAllowedBranches() : "") %>">
          </div>
          <div class="form-group">
            <label>Required Skills</label>
            <textarea name="required_skills" placeholder="e.g., Java, Python, SQL"><%= com.rit.placement.util.XSSUtil.escape(editJob != null && editJob.getRequiredSkills() != null ? editJob.getRequiredSkills() : "") %></textarea>
          </div>
        </div>
        
        <div style="display: flex; gap: 1rem; margin-top: 1rem;">
          <button type="submit" class="btn btn-primary">
            <%= com.rit.placement.util.XSSUtil.escape(editJob != null ? "Update Job" : "Post Job") %>
          </button>
          <% if (editJob != null) { %>
            <a href="${pageContext.request.contextPath}/company/jobs" class="btn btn-secondary">Cancel</a>
          <% } %>
        </div>
      </form>
    </div>

    <!-- Job Listings -->
    <div class="section">
      <h3>📋 Your Job Postings</h3>
      <% if (jobs != null && !jobs.isEmpty()) { %>
        <table>
          <thead>
            <tr>
              <th>Role</th>
              <th>Package</th>
              <th>Min CGPA</th>
              <th>Deadline</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <% for (JobPosting job : jobs) {
                boolean isActive = job.getDeadline() != null && 
                                  !job.getDeadline().before(new java.sql.Date(System.currentTimeMillis()));
            %>
            <tr>
              <td><strong><%= com.rit.placement.util.XSSUtil.escape(job.getRole()) %></strong></td>
              <td><%= com.rit.placement.util.XSSUtil.escape(job.getPackageAmount()) %> LPA</td>
              <td><%= com.rit.placement.util.XSSUtil.escape(job.getMinCgpa()) %></td>
              <td><%= com.rit.placement.util.XSSUtil.escape(job.getDeadline() != null ? dateFormat.format(job.getDeadline()) : "N/A") %></td>
              <td>
                <span class="badge <%= com.rit.placement.util.XSSUtil.escape(isActive ? "badge-active" : "badge-expired") %>">
                  <%= com.rit.placement.util.XSSUtil.escape(isActive ? "Active" : "Expired") %>
                </span>
              </td>
              <td>
                <div class="actions">
                  <a href="${pageContext.request.contextPath}/company/jobs?action=edit&id=<%= com.rit.placement.util.XSSUtil.escape(job.getJobId()) %>" 
                     class="btn btn-primary btn-small">Edit</a>
                  <a href="${pageContext.request.contextPath}/company/jobs?action=delete&id=<%= com.rit.placement.util.XSSUtil.escape(job.getJobId()) %>" 
                     class="btn btn-danger btn-small"
                     onclick="return confirm('Are you sure you want to delete this job?')">Delete</a>
                </div>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      <% } else { %>
        <p style="text-align: center; color: #6b7280; padding: 2rem;">No job postings yet. Create your first job above!</p>
      <% } %>
    </div>
  </div>
</body>
</html>
