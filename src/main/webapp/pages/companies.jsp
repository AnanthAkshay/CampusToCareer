<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Company" %>
<%@ page import="java.util.List" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }

  // Read data from request attributes
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
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Companies — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Loading companies...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Companies 🏢</h1>
        <p class="page-subtitle">Manage placement partner companies</p>
      </div>
      <% if (canAdd) { %>
      <div class="header-actions">
        <button class="btn-primary" onclick="toggleAddForm()">
          <span>➕</span> Add Company
        </button>
      </div>
      <% } %>
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

    <!-- Add Company Form (Hidden by default) -->
    <% if (canAdd) { %>
    <div id="addCompanyForm" class="form-card" style="display: none;">
      <div class="form-card-header">
        <h2 class="form-card-title">➕ Add New Company</h2>
        <button class="btn-close" onclick="toggleAddForm()">✕</button>
      </div>
      <form action="${pageContext.request.contextPath}/companies" method="post" class="company-form">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

        <div class="form-row">
          <div class="form-group">
            <label for="company_name">Company Name <span class="required">*</span></label>
            <input type="text" id="company_name" name="company_name" required 
                   placeholder="e.g., Google, Microsoft, Amazon">
          </div>
          
          <div class="form-group">
            <label for="company_type">Company Type</label>
            <select id="company_type" name="company_type" class="form-select">
              <option value="PRODUCT">Product</option>
              <option value="SERVICE">Service</option>
              <option value="STARTUP">Startup</option>
              <option value="MNC">MNC</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" name="description" rows="4" 
                    placeholder="Brief description about the company..."></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary">
            <span>💾</span> Save Company
          </button>
          <button type="button" onclick="toggleAddForm()" class="btn-secondary">
            <span>✕</span> Cancel
          </button>
        </div>
      </form>
    </div>
    <% } %>

    <!-- Companies Grid -->
    <% if (companies != null && !companies.isEmpty()) { %>
      <div class="companies-grid">
        <% for (Company company : companies) { %>
          <div class="company-card">
            <div class="company-card-header">
              <div class="company-icon">
                <% 
                  String icon = "🏢";
                  if ("PRODUCT".equals(company.getCompanyType())) icon = "💻";
                  else if ("SERVICE".equals(company.getCompanyType())) icon = "🔧";
                  else if ("STARTUP".equals(company.getCompanyType())) icon = "🚀";
                  else if ("MNC".equals(company.getCompanyType())) icon = "🌐";
                %>
                <%= com.rit.placement.util.XSSUtil.escape(icon) %>
              </div>
              <div class="company-info">
                <h3 class="company-name"><%= com.rit.placement.util.XSSUtil.escape(company.getCompanyName()) %></h3>
                <span class="company-type-badge badge-<%= com.rit.placement.util.XSSUtil.escape(company.getCompanyType().toLowerCase()) %>">
                  <%= com.rit.placement.util.XSSUtil.escape(company.getCompanyType()) %>
                </span>
              </div>
            </div>
            
            <% if (company.getDescription() != null && !company.getDescription().isEmpty()) { %>
              <p class="company-description"><%= com.rit.placement.util.XSSUtil.escape(company.getDescription()) %></p>
            <% } else { %>
              <p class="company-description text-muted">No description available</p>
            <% } %>
            
            <div class="company-card-footer">
              <span class="company-id">ID: <%= com.rit.placement.util.XSSUtil.escape(company.getCompanyId()) %></span>
              <a href="${pageContext.request.contextPath}/job-postings?company=<%= com.rit.placement.util.XSSUtil.escape(company.getCompanyId()) %>" 
                 class="btn-link">
                View Jobs →
              </a>
            </div>
          </div>
        <% } %>
      </div>
    <% } else { %>
      <!-- Empty State -->
      <div class="empty-state">
        <div class="empty-state-icon">🏢</div>
        <h4 class="empty-state-title">No Companies Yet</h4>
        <p class="empty-state-text">
          <% if (canAdd) { %>
            Click "Add Company" to register your first placement partner.
          <% } else { %>
            Companies will appear here once they are added by coordinators.
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

    // Toggle Add Company Form
    function toggleAddForm() {
      var form = document.getElementById('addCompanyForm');
      if (form.style.display === 'none') {
        form.style.display = 'block';
        form.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } else {
        form.style.display = 'none';
      }
    }

    // Auto-hide success message
    window.addEventListener('DOMContentLoaded', function() {
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
