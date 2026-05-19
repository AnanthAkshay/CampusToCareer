<%@ page contentType="text/html;charset=UTF-8" %>
<%
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }
  
  Integer totalStudents = (Integer) request.getAttribute("totalStudents");
  Integer totalApplications = (Integer) request.getAttribute("totalApplications");
  Integer totalCompanies = (Integer) request.getAttribute("totalCompanies");
  Integer totalJobs = (Integer) request.getAttribute("totalJobs");
  Integer selectedStudents = (Integer) request.getAttribute("selectedStudents");
  Integer shortlistedStudents = (Integer) request.getAttribute("shortlistedStudents");
  Integer pendingApplications = (Integer) request.getAttribute("pendingApplications");
  Integer activeJobs = (Integer) request.getAttribute("activeJobs");
  String branchData = (String) request.getAttribute("branchData");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Dashboard — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body>
  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Admin Dashboard 📊</h1>
        <p class="page-subtitle">Placement Management Control Panel</p>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="stats-grid">
      <div class="stat-card stat-card-blue">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-label">Total Students</div>
          <div class="stat-value"><%=totalStudents != null ? totalStudents : 0%></div>
        </div>
      </div>

      <div class="stat-card stat-card-purple">
        <div class="stat-icon">📝</div>
        <div class="stat-content">
          <div class="stat-label">Total Applications</div>
          <div class="stat-value"><%=totalApplications != null ? totalApplications : 0%></div>
          <div class="stat-trend">
            <span class="trend-indicator"><%=pendingApplications != null ? pendingApplications : 0%> pending</span>
          </div>
        </div>
      </div>

      <div class="stat-card stat-card-gradient">
        <div class="stat-icon">🏢</div>
        <div class="stat-content">
          <div class="stat-label">Companies</div>
          <div class="stat-value"><%=totalCompanies != null ? totalCompanies : 0%></div>
        </div>
      </div>

      <div class="stat-card" style="border-top: 4px solid #f59e0b;">
        <div class="stat-icon">💼</div>
        <div class="stat-content">
          <div class="stat-label">Job Postings</div>
          <div class="stat-value"><%=totalJobs != null ? totalJobs : 0%></div>
          <div class="stat-trend">
            <span class="trend-indicator"><%=activeJobs != null ? activeJobs : 0%> active</span>
          </div>
        </div>
      </div>

      <div class="stat-card" style="border-top: 4px solid #16a34a;">
        <div class="stat-icon">🎉</div>
        <div class="stat-content">
          <div class="stat-label">Selected Students</div>
          <div class="stat-value"><%=selectedStudents != null ? selectedStudents : 0%></div>
        </div>
      </div>

      <div class="stat-card" style="border-top: 4px solid #0ea5e9;">
        <div class="stat-icon">✓</div>
        <div class="stat-content">
          <div class="stat-label">Shortlisted</div>
          <div class="stat-value"><%=shortlistedStudents != null ? shortlistedStudents : 0%></div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); gap: 20px; margin-top: 30px;">
      <div class="chart-card" style="background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <h3 style="margin-bottom: 15px;">Branch-wise Placements</h3>
        <canvas id="branchChart"></canvas>
      </div>

      <div class="chart-card" style="background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <h3 style="margin-bottom: 15px;">Application Status Distribution</h3>
        <canvas id="statusChart"></canvas>
      </div>

      <div class="chart-card" style="background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <h3 style="margin-bottom: 15px;">Placement Trend Over Time</h3>
        <canvas id="trendChart"></canvas>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions" style="margin-top: 30px;">
      <h3>Quick Actions</h3>
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-top: 15px;">
        <a href="${pageContext.request.contextPath}/admin/students" class="action-btn">
          <span>👥</span> Manage Students
        </a>
        <a href="${pageContext.request.contextPath}/admin/applications" class="action-btn">
          <span>📝</span> View Applications
        </a>
        <a href="${pageContext.request.contextPath}/admin/companies" class="action-btn">
          <span>🏢</span> Manage Companies
        </a>
        <a href="${pageContext.request.contextPath}/admin/jobs" class="action-btn">
          <span>💼</span> Manage Jobs
        </a>
      </div>
    </div>

    <!-- Export Reports Section -->
    <div class="export-section" style="margin-top: 30px;">
      <h3>📊 Export Reports</h3>
      <p style="color: #6b7280; margin-bottom: 15px;">Download placement data in various formats</p>
      <div style="display: flex; gap: 15px; flex-wrap: wrap;">
        <a href="${pageContext.request.contextPath}/admin/export/csv" class="export-btn export-btn-csv">
          <span class="export-icon">📄</span>
          <div class="export-content">
            <div class="export-title">Download CSV</div>
            <div class="export-desc">Excel-compatible spreadsheet</div>
          </div>
        </a>
        <a href="${pageContext.request.contextPath}/admin/export/pdf" class="export-btn export-btn-pdf">
          <span class="export-icon">📑</span>
          <div class="export-content">
            <div class="export-title">Download PDF</div>
            <div class="export-desc">Formatted report with summary</div>
          </div>
        </a>
      </div>
    </div>
  </main>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
  <script>
    document.addEventListener('DOMContentLoaded', function() {
      const pageContent = document.querySelector('.page-content');
      if (pageContent) {
        pageContent.classList.add('fade-in');
      }
    });

    // Branch-wise placements chart
    const branchData = <%=branchData != null ? branchData : "[]"%>;
    const branchLabels = branchData.map(d => d.branch);
    const branchCounts = branchData.map(d => d.count);

    new Chart(document.getElementById('branchChart'), {
      type: 'bar',
      data: {
        labels: branchLabels,
        datasets: [{
          label: 'Placed Students',
          data: branchCounts,
          backgroundColor: 'rgba(59, 130, 246, 0.8)',
          borderColor: 'rgba(59, 130, 246, 1)',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 1 }
          }
        }
      }
    });

    // Application status chart
    new Chart(document.getElementById('statusChart'), {
      type: 'doughnut',
      data: {
        labels: ['Pending', 'Shortlisted', 'Selected', 'Rejected'],
        datasets: [{
          data: [
            <%=pendingApplications != null ? pendingApplications : 0%>,
            <%=shortlistedStudents != null ? shortlistedStudents : 0%>,
            <%=selectedStudents != null ? selectedStudents : 0%>,
            <%=(totalApplications != null && pendingApplications != null && shortlistedStudents != null && selectedStudents != null) ? 
               (totalApplications - pendingApplications - shortlistedStudents - selectedStudents) : 0%>
          ],
          backgroundColor: [
            'rgba(251, 191, 36, 0.8)',
            'rgba(14, 165, 233, 0.8)',
            'rgba(34, 197, 94, 0.8)',
            'rgba(239, 68, 68, 0.8)'
          ]
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true
      }
    });

    // Placement trend chart (Line chart)
    // Sample data - in production, this would come from servlet
    const currentYear = new Date().getFullYear();
    new Chart(document.getElementById('trendChart'), {
      type: 'line',
      data: {
        labels: [currentYear - 2, currentYear - 1, currentYear],
        datasets: [{
          label: 'Placements',
          data: [
            <%=selectedStudents != null && selectedStudents > 0 ? Math.max(1, selectedStudents - 15) : 0%>,
            <%=selectedStudents != null && selectedStudents > 0 ? Math.max(1, selectedStudents - 8) : 0%>,
            <%=selectedStudents != null ? selectedStudents : 0%>
          ],
          borderColor: 'rgba(59, 130, 246, 1)',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          tension: 0.4,
          fill: true
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 5 }
          }
        }
      }
    });
  </script>

  <style>
    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 15px;
      background: white;
      border: 2px solid #e5e7eb;
      border-radius: 8px;
      text-decoration: none;
      color: #374151;
      font-weight: 500;
      transition: all 0.2s;
    }
    .action-btn:hover {
      border-color: #3b82f6;
      color: #3b82f6;
      transform: translateY(-2px);
      box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    }
    
    /* Export Section Styles */
    .export-section {
      background: white;
      padding: 25px;
      border-radius: 12px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    
    .export-section h3 {
      margin-bottom: 5px;
      color: #1f2937;
    }
    
    .export-btn {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 20px 25px;
      background: white;
      border: 2px solid #e5e7eb;
      border-radius: 12px;
      text-decoration: none;
      color: #374151;
      transition: all 0.3s;
      min-width: 280px;
    }
    
    .export-btn:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 16px rgba(0,0,0,0.15);
    }
    
    .export-btn-csv:hover {
      border-color: #10b981;
      background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    }
    
    .export-btn-pdf:hover {
      border-color: #ef4444;
      background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    }
    
    .export-icon {
      font-size: 2.5rem;
      line-height: 1;
    }
    
    .export-content {
      flex: 1;
      text-align: left;
    }
    
    .export-title {
      font-size: 1.1rem;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 4px;
    }
    
    .export-desc {
      font-size: 0.9rem;
      color: #6b7280;
    }
    
    @media (max-width: 768px) {
      .export-btn {
        min-width: 100%;
      }
    }
  </style>
</body>
</html>
