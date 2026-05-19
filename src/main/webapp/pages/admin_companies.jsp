<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Company" %>
<%@ page import="com.rit.placement.model.CompanyRequest" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }
  
  @SuppressWarnings("unchecked")
  List<Company> companies = (List<Company>) request.getAttribute("companies");
  @SuppressWarnings("unchecked")
  List<CompanyRequest> pendingRequests = (List<CompanyRequest>) request.getAttribute("pendingRequests");
  Integer totalCompanies = (Integer) request.getAttribute("totalCompanies");
  Integer pendingCount = (Integer) request.getAttribute("pendingCount");
  
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  if (successMessage != null) session.removeAttribute("successMessage");
  if (errorMessage != null) session.removeAttribute("errorMessage");
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Manage Companies — Admin Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Manage Companies 🏢</h1>
        <p class="page-subtitle">Total: <%= com.rit.placement.util.XSSUtil.escape(totalCompanies != null ? totalCompanies : 0) %> companies | 
          <span style="color: #f59e0b;"><%= com.rit.placement.util.XSSUtil.escape(pendingCount != null ? pendingCount : 0) %> pending requests</span>
        </p>
      </div>
      <button class="btn-primary" onclick="toggleAddForm()">➕ Add Company</button>
    </div>

    <% if (successMessage != null) { %>
      <div class="alert alert-success">✓ <%= com.rit.placement.util.XSSUtil.escape(successMessage) %></div>
    <% } %>
    <% if (errorMessage != null) { %>
      <div class="alert alert-error">✕ <%= com.rit.placement.util.XSSUtil.escape(errorMessage) %></div>
    <% } %>

    <!-- Pending Requests Section -->
    <% if (pendingRequests != null && !pendingRequests.isEmpty()) { %>
      <div style="background: #fff3cd; border: 1px solid #ffc107; border-radius: 8px; padding: 20px; margin-bottom: 20px;">
        <h3 style="margin-bottom: 15px; color: #856404;">⏳ Pending Company Requests (<%= com.rit.placement.util.XSSUtil.escape(pendingRequests.size()) %>)</h3>
        <div class="table-wrap">
          <table style="width: 100%;">
            <thead>
              <tr>
                <th>Company Name</th>
                <th>Type</th>
                <th>Email</th>
                <th>Requested</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <% for (CompanyRequest req : pendingRequests) { %>
                <tr>
                  <td><strong><%= com.rit.placement.util.XSSUtil.escape(req.getCompanyName()) %></strong></td>
                  <td><span class="badge"><%= com.rit.placement.util.XSSUtil.escape(req.getCompanyType()) %></span></td>
                  <td><%= com.rit.placement.util.XSSUtil.escape(req.getEmail() != null ? req.getEmail() : "N/A") %></td>
                  <td><%= com.rit.placement.util.XSSUtil.escape(req.getRequestedAt() != null ? dateFormat.format(req.getRequestedAt()) : "N/A") %></td>
                  <td>
                    <form method="post" style="display: inline;" onsubmit="return confirm('Approve this company request?');">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                      <input type="hidden" name="action" value="approve_request">
                      <input type="hidden" name="request_id" value="<%= com.rit.placement.util.XSSUtil.escape(req.getRequestId()) %>">
                      <button type="submit" class="btn-success-sm">✓ Approve</button>
                    </form>
                    <form method="post" style="display: inline; margin-left: 5px;" onsubmit="return confirm('Reject this request?');">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                      <input type="hidden" name="action" value="reject_request">
                      <input type="hidden" name="request_id" value="<%= com.rit.placement.util.XSSUtil.escape(req.getRequestId()) %>">
                      <button type="submit" class="btn-danger-sm">✕ Reject</button>
                    </form>
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      </div>
    <% } %>

    <!-- Add/Edit Form -->
    <div id="addForm" class="form-card" style="display: none; margin-bottom: 20px;">
      <h3 id="formTitle">Add New Company</h3>
      <form method="post" id="companyForm">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

        <input type="hidden" name="action" id="formAction" value="add">
        <input type="hidden" name="company_id" id="companyId">
        <div class="form-group">
          <label>Company Name *</label>
          <input type="text" name="company_name" id="companyName" required>
        </div>
        <div class="form-group">
          <label>Type</label>
          <select name="company_type" id="companyType">
            <option value="PRODUCT">Product</option>
            <option value="SERVICE">Service</option>
            <option value="STARTUP">Startup</option>
            <option value="MNC">MNC</option>
          </select>
        </div>
        <div class="form-group">
          <label>Description</label>
          <textarea name="description" id="companyDescription" rows="3"></textarea>
        </div>
        <button type="submit" class="btn-primary">Save</button>
        <button type="button" onclick="cancelForm()" class="btn-secondary">Cancel</button>
      </form>
    </div>

    <!-- Companies List -->
    <% if (companies != null && !companies.isEmpty()) { %>
      <div class="companies-grid">
        <% for (Company company : companies) { %>
          <div class="company-card">
            <div class="company-card-header">
              <h3><%= com.rit.placement.util.XSSUtil.escape(company.getCompanyName()) %></h3>
              <span class="badge"><%= com.rit.placement.util.XSSUtil.escape(company.getCompanyType()) %></span>
            </div>
            <p><%= com.rit.placement.util.XSSUtil.escape(company.getDescription() != null ? company.getDescription() : "No description") %></p>
            <div class="company-card-footer">
              <span>ID: <%= com.rit.placement.util.XSSUtil.escape(company.getCompanyId()) %></span>
              <div>
                <button onclick="editCompany(<%= com.rit.placement.util.XSSUtil.escape(company.getCompanyId()) %>, '<%= com.rit.placement.util.XSSUtil.escape(company.getCompanyName().replace("'", "\\'")) %>', '<%= com.rit.placement.util.XSSUtil.escape(company.getCompanyType()) %>', '<%= com.rit.placement.util.XSSUtil.escape(company.getDescription() != null ? company.getDescription().replace("'", "\\'").replace("\n", " ") : "") %>')" class="btn-edit-sm">Edit</button>
                <form method="post" style="display: inline; margin-left: 5px;" onsubmit="return confirm('Delete this company?');">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                  <input type="hidden" name="action" value="delete">
                  <input type="hidden" name="company_id" value="<%= com.rit.placement.util.XSSUtil.escape(company.getCompanyId()) %>">
                  <button type="submit" class="btn-danger-sm">Delete</button>
                </form>
              </div>
            </div>
          </div>
        <% } %>
      </div>
    <% } else { %>
      <div class="empty-state">
        <div class="empty-state-icon">🏢</div>
        <h4 class="empty-state-title">No Companies Found</h4>
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
        resetForm();
        document.getElementById('formTitle').textContent = 'Add New Company';
        document.getElementById('formAction').value = 'add';
        form.style.display = 'block';
        form.scrollIntoView({ behavior: 'smooth' });
      } else {
        form.style.display = 'none';
      }
    }

    function editCompany(id, name, type, description) {
      document.getElementById('formTitle').textContent = 'Edit Company';
      document.getElementById('formAction').value = 'update';
      document.getElementById('companyId').value = id;
      document.getElementById('companyName').value = name;
      document.getElementById('companyType').value = type;
      document.getElementById('companyDescription').value = description;
      document.getElementById('addForm').style.display = 'block';
      document.getElementById('addForm').scrollIntoView({ behavior: 'smooth' });
    }

    function cancelForm() {
      document.getElementById('addForm').style.display = 'none';
      resetForm();
    }

    function resetForm() {
      document.getElementById('companyForm').reset();
      document.getElementById('companyId').value = '';
    }
  </script>
  <style>
    .btn-success-sm, .btn-edit-sm {
      padding: 5px 10px;
      font-size: 13px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
    }
    .btn-success-sm {
      background: #16a34a;
      color: white;
    }
    .btn-success-sm:hover {
      background: #15803d;
    }
    .btn-edit-sm {
      background: #3b82f6;
      color: white;
    }
    .btn-edit-sm:hover {
      background: #2563eb;
    }
  </style>
</body>
</html>
