<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.JobPosting" %>
<%@ page import="com.rit.placement.model.Company" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }
  
  @SuppressWarnings("unchecked")
  List<JobPosting> jobs = (List<JobPosting>) request.getAttribute("jobs");
  @SuppressWarnings("unchecked")
  List<Company> companies = (List<Company>) request.getAttribute("companies");
  Integer totalJobs = (Integer) request.getAttribute("totalJobs");
  
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  if (successMessage != null) session.removeAttribute("successMessage");
  if (errorMessage != null) session.removeAttribute("errorMessage");
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Manage Jobs — Admin Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Manage Job Postings 💼</h1>
        <p class="page-subtitle">Total: <%=totalJobs != null ? totalJobs : 0%> jobs</p>
      </div>
      <button class="btn-primary" onclick="toggleAddForm()">➕ Add Job</button>
    </div>

    <% if (successMessage != null) { %>
      <div class="alert alert-success">✓ <%=successMessage%></div>
    <% } %>
    <% if (errorMessage != null) { %>
      <div class="alert alert-error">✕ <%=errorMessage%></div>
    <% } %>

    <div id="addForm" class="form-card" style="display: none; margin-bottom: 20px;">
      <h3 id="formTitle">Add New Job Posting</h3>
      <form method="post" id="jobForm">
        <input type="hidden" name="action" id="formAction" value="add">
        <input type="hidden" name="job_id" id="jobId">
        <div class="form-row">
          <div class="form-group">
            <label>Company *</label>
            <select name="company_id" id="companyId" required>
              <option value="">Select Company</option>
              <% if (companies != null) for (Company c : companies) { %>
                <option value="<%=c.getCompanyId()%>"><%=c.getCompanyName()%></option>
              <% } %>
            </select>
          </div>
          <div class="form-group">
            <label>Role *</label>
            <input type="text" name="role" id="jobRole" required>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Package (LPA)</label>
            <input type="number" name="package" id="jobPackage" step="0.01" min="0">
          </div>
          <div class="form-group">
            <label>Min CGPA *</label>
            <input type="number" name="min_cgpa" id="minCgpa" step="0.01" min="0" max="10" required>
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Allowed Branches</label>
            <input type="text" name="allowed_branches" id="allowedBranches" placeholder="ISE, CSE, ECE">
          </div>
          <div class="form-group">
            <label>Deadline *</label>
            <input type="date" name="deadline" id="jobDeadline" required>
          </div>
        </div>
        <div class="form-group">
          <label>Required Skills</label>
          <textarea name="required_skills" id="requiredSkills" rows="2"></textarea>
        </div>
        <button type="submit" class="btn-primary">Save Job</button>
        <button type="button" onclick="cancelJobForm()" class="btn-secondary">Cancel</button>
      </form>
    </div>

    <% if (jobs != null && !jobs.isEmpty()) { %>
      <div class="jobs-grid">
        <% for (JobPosting job : jobs) { 
           boolean isExpired = job.getDeadline() != null && job.getDeadline().before(new java.util.Date());
        %>
          <div class="job-card <%= isExpired ? "job-expired" : "" %>">
            <div class="job-card-header">
              <div>
                <h3><%=job.getRole()%></h3>
                <p><%=job.getCompanyName()%></p>
              </div>
              <span class="badge <%= isExpired ? "badge-danger" : "badge-success" %>">
                <%= isExpired ? "Expired" : "Active" %>
              </span>
            </div>
            <div class="job-details">
              <div><strong>Package:</strong> <%= job.getPackageAmount() != null ? job.getPackageAmount() + " LPA" : "Not disclosed" %></div>
              <div><strong>Min CGPA:</strong> <%= job.getMinCgpa() %></div>
              <div><strong>Deadline:</strong> <%= job.getDeadline() != null ? dateFormat.format(job.getDeadline()) : "N/A" %></div>
            </div>
            <div class="job-card-footer">
              <span>ID: <%=job.getJobId()%></span>
              <div>
                <button onclick="editJob(<%=job.getJobId()%>, <%=job.getCompanyId()%>, '<%=job.getRole().replace("'", "\\'")%>', '<%=job.getPackageAmount() != null ? job.getPackageAmount() : ""%>', '<%=job.getMinCgpa()%>', '<%=job.getAllowedBranches() != null ? job.getAllowedBranches().replace("'", "\\'") : ""%>', '<%=job.getRequiredSkills() != null ? job.getRequiredSkills().replace("'", "\\'").replace("\n", " ") : ""%>', '<%=job.getDeadline()%>')" class="btn-edit-sm">Edit</button>
                <form method="post" style="display: inline; margin-left: 5px;" onsubmit="return confirm('Delete this job?');">
                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="job_id" value="<%=job.getJobId()%>">
                  <button type="submit" class="btn-danger-sm">Delete</button>
                </form>
              </div>
            </div>
          </div>
        <% } %>
      </div>
    <% } else { %>
      <div class="empty-state">
        <div class="empty-state-icon">💼</div>
        <h4 class="empty-state-title">No Job Postings Found</h4>
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

    function toggleAddForm() {
      const form = document.getElementById('addForm');
      if (form.style.display === 'none') {
        resetJobForm();
        document.getElementById('formTitle').textContent = 'Add New Job Posting';
        document.getElementById('formAction').value = 'add';
        form.style.display = 'block';
        form.scrollIntoView({ behavior: 'smooth' });
      } else {
        form.style.display = 'none';
      }
    }

    function editJob(jobId, companyId, role, packageAmt, minCgpa, branches, skills, deadline) {
      document.getElementById('formTitle').textContent = 'Edit Job Posting';
      document.getElementById('formAction').value = 'update';
      document.getElementById('jobId').value = jobId;
      document.getElementById('companyId').value = companyId;
      document.getElementById('jobRole').value = role;
      document.getElementById('jobPackage').value = packageAmt;
      document.getElementById('minCgpa').value = minCgpa;
      document.getElementById('allowedBranches').value = branches;
      document.getElementById('requiredSkills').value = skills;
      document.getElementById('jobDeadline').value = deadline;
      document.getElementById('addForm').style.display = 'block';
      document.getElementById('addForm').scrollIntoView({ behavior: 'smooth' });
    }

    function cancelJobForm() {
      document.getElementById('addForm').style.display = 'none';
      resetJobForm();
    }

    function resetJobForm() {
      document.getElementById('jobForm').reset();
      document.getElementById('jobId').value = '';
    }
  </script>
  <style>
    .btn-edit-sm {
      padding: 5px 10px;
      font-size: 13px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      background: #3b82f6;
      color: white;
    }
    .btn-edit-sm:hover {
      background: #2563eb;
    }
  </style>
</body>
</html>
