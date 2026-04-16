<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.JobPosting" %>
<%@ page import="com.rit.placement.model.Company" %>
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
  List<JobPosting> jobPostings = (List<JobPosting>) request.getAttribute("jobPostings");
  
  @SuppressWarnings("unchecked")
  List<Company> companies = (List<Company>) request.getAttribute("companies");
  
  String role = (String) session.getAttribute("role");
  boolean canAdd = "COORDINATOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);

  // Get success/error messages
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) request.getAttribute("error");
  
  if (successMessage != null) {
    session.removeAttribute("successMessage");
  }
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Job Postings — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Loading job postings...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Job Postings 💼</h1>
        <p class="page-subtitle">Browse and manage placement opportunities</p>
      </div>
      <% if (canAdd) { %>
      <div class="header-actions">
        <button class="btn-primary" onclick="toggleAddForm()">
          <span>➕</span> Add Job Posting
        </button>
      </div>
      <% } %>
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

    <!-- Add Job Posting Form (Hidden by default) -->
    <% if (canAdd) { %>
    <div id="addJobForm" class="form-card" style="display: none;">
      <div class="form-card-header">
        <h2 class="form-card-title">➕ Add New Job Posting</h2>
        <button class="btn-close" onclick="toggleAddForm()">✕</button>
      </div>
      <form action="${pageContext.request.contextPath}/job-postings" method="post" class="job-form">
        <div class="form-row">
          <div class="form-group">
            <label for="company_id">Company <span class="required">*</span></label>
            <select id="company_id" name="company_id" class="form-select" required>
              <option value="">Select Company</option>
              <% if (companies != null) {
                   for (Company company : companies) { %>
                <option value="<%=company.getCompanyId()%>"><%=company.getCompanyName()%></option>
              <% } } %>
            </select>
          </div>
          
          <div class="form-group">
            <label for="role">Job Role <span class="required">*</span></label>
            <input type="text" id="role" name="role" required 
                   placeholder="e.g., Software Engineer, Data Analyst">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="package">Package (LPA)</label>
            <input type="number" id="package" name="package" step="0.01" min="0" 
                   placeholder="e.g., 12.5">
          </div>
          
          <div class="form-group">
            <label for="min_cgpa">Min CGPA <span class="required">*</span></label>
            <input type="number" id="min_cgpa" name="min_cgpa" step="0.01" min="0" max="10" required 
                   placeholder="e.g., 7.5">
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="allowed_branches">Allowed Branches</label>
            <input type="text" id="allowed_branches" name="allowed_branches" 
                   placeholder="e.g., ISE, CSE, ECE (comma-separated)">
          </div>
          
          <div class="form-group">
            <label for="deadline">Application Deadline <span class="required">*</span></label>
            <input type="date" id="deadline" name="deadline" required>
          </div>
        </div>

        <div class="form-group">
          <label for="required_skills">Required Skills</label>
          <textarea id="required_skills" name="required_skills" rows="3" 
                    placeholder="e.g., Java, Spring Boot, MySQL, React"></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary">
            <span>💾</span> Create Job Posting
          </button>
          <button type="button" onclick="toggleAddForm()" class="btn-secondary">
            <span>✕</span> Cancel
          </button>
        </div>
      </form>
    </div>
    <% } %>

    <!-- Job Postings Grid -->
    <% if (jobPostings != null && !jobPostings.isEmpty()) { %>
      <div class="jobs-grid">
        <% for (JobPosting job : jobPostings) { 
             boolean isExpired = job.getDeadline() != null && 
                                 job.getDeadline().before(new java.util.Date());
        %>
          <div class="job-card <%= isExpired ? "job-expired" : "" %>">
            <div class="job-card-header">
              <div class="job-company">
                <span class="job-icon">💼</span>
                <div>
                  <h3 class="job-title"><%=job.getRole()%></h3>
                  <p class="job-company-name"><%=job.getCompanyName()%></p>
                </div>
              </div>
              <% if (isExpired) { %>
                <span class="badge badge-danger">Expired</span>
              <% } else { %>
                <span class="badge badge-success">Active</span>
              <% } %>
            </div>

            <div class="job-details">
              <div class="job-detail-item">
                <span class="job-detail-label">💰 Package:</span>
                <span class="job-detail-value">
                  <%= job.getPackageAmount() != null ? job.getPackageAmount() + " LPA" : "Not disclosed" %>
                </span>
              </div>
              
              <div class="job-detail-item">
                <span class="job-detail-label">📊 Min CGPA:</span>
                <span class="job-detail-value">
                  <%= job.getMinCgpa() != null ? job.getMinCgpa() : "N/A" %>
                </span>
              </div>
              
              <% if (job.getAllowedBranches() != null && !job.getAllowedBranches().isEmpty()) { %>
              <div class="job-detail-item">
                <span class="job-detail-label">🎓 Branches:</span>
                <span class="job-detail-value"><%=job.getAllowedBranches()%></span>
              </div>
              <% } %>
              
              <div class="job-detail-item">
                <span class="job-detail-label">📅 Deadline:</span>
                <span class="job-detail-value <%= isExpired ? "text-danger" : "" %>">
                  <%= job.getDeadline() != null ? dateFormat.format(job.getDeadline()) : "N/A" %>
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
                  <span class="skill-tag"><%=skill.trim()%></span>
                <% } } %>
              </div>
            </div>
            <% } %>

            <div class="job-card-footer">
              <span class="job-id">Job ID: <%=job.getJobId()%></span>
              <div class="job-actions">
                <% if ("COORDINATOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) { %>
                  <a href="${pageContext.request.contextPath}/eligible-students?job_id=<%=job.getJobId()%>" 
                     class="btn-link">
                    View Eligible Students →
                  </a>
                <% } %>
                <% if (!isExpired) { %>
                  <button class="btn-action">Apply Now</button>
                <% } %>
              </div>
            </div>
          </div>
        <% } %>
      </div>
    <% } else { %>
      <!-- Empty State -->
      <div class="empty-state">
        <div class="empty-state-icon">💼</div>
        <h4 class="empty-state-title">No Job Postings Yet</h4>
        <p class="empty-state-text">
          <% if (canAdd) { %>
            Click "Add Job Posting" to create your first placement opportunity.
          <% } else { %>
            Job postings will appear here once they are added by coordinators.
          <% } %>
        </p>
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

    // Toggle Add Job Form
    function toggleAddForm() {
      var form = document.getElementById('addJobForm');
      if (form.style.display === 'none') {
        form.style.display = 'block';
        form.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else {
        form.style.display = 'none';
      }
    }

    // Set minimum date for deadline (today)
    document.addEventListener('DOMContentLoaded', function() {
      var deadlineInput = document.getElementById('deadline');
      if (deadlineInput) {
        var today = new Date().toISOString().split('T')[0];
        deadlineInput.setAttribute('min', today);
      }

      // Auto-hide success message
      var successAlert = document.querySelector('.alert-success');
      if (successAlert) {
        successAlert.setAttribute('role', 'status');
        successAlert.setAttribute('aria-live', 'polite');
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
