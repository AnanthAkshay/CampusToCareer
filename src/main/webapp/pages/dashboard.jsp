<%@ page contentType="text/html;charset=UTF-8" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }

  // Read data from request attributes (set by StudentDashboardServlet)
  String name = (String) request.getAttribute("name");
  String usn = (String) request.getAttribute("usn");
  Double sem2Sgpa = (Double) request.getAttribute("sem2_sgpa");
  Double sem3Sgpa = (Double) request.getAttribute("sem3_sgpa");
  Double cgpa = (Double) request.getAttribute("cgpa");

  // Safe display with null handling
  String displayName = (name != null) ? name : "Student";
  String displayUsn = (usn != null) ? usn : "N/A";
  String displaySem2 = (sem2Sgpa != null) ? String.format("%.2f", sem2Sgpa) : "N/A";
  String displaySem3 = (sem3Sgpa != null) ? String.format("%.2f", sem3Sgpa) : "N/A";
  String displayCgpa = (cgpa != null) ? String.format("%.2f", cgpa) : "N/A";
  
  // For Chart.js - use actual values or 0 for null
  double sem2Value = (sem2Sgpa != null) ? sem2Sgpa : 0.0;
  double sem3Value = (sem3Sgpa != null) ? sem3Sgpa : 0.0;
  double cgpaValue = (cgpa != null) ? cgpa : 0.0;
  double cgpaPercentage = (cgpaValue / 10.0) * 100;
  
  // Check if we have academic data
  boolean hasAcademicData = (sem2Sgpa != null || sem3Sgpa != null);
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Student Dashboard — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Loading dashboard...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Welcome back, <%=displayName%>! 👋</h1>
        <p class="page-subtitle">
          <span class="badge badge-info">STUDENT</span>
          <span class="usn-badge">USN: <%=displayUsn%></span>
        </p>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" onclick="window.print()">
          <span>📄</span> Download Report
        </button>
      </div>
    </div>

    <!-- Stats Cards Grid -->
    <div class="stats-grid">
      <!-- Semester 2 Card -->
      <div class="stat-card stat-card-blue">
        <div class="stat-icon">📘</div>
        <div class="stat-content">
          <div class="stat-label">Semester 2 SGPA</div>
          <div class="stat-value"><%=displaySem2%></div>
          <div class="stat-trend">
            <span class="trend-indicator">Previous Semester</span>
          </div>
        </div>
      </div>

      <!-- Semester 3 Card -->
      <div class="stat-card stat-card-purple">
        <div class="stat-icon">📗</div>
        <div class="stat-content">
          <div class="stat-label">Semester 3 SGPA</div>
          <div class="stat-value"><%=displaySem3%></div>
          <div class="stat-trend">
            <span class="trend-indicator">Current Semester</span>
          </div>
        </div>
      </div>

      <!-- CGPA Card (Highlighted) -->
      <div class="stat-card stat-card-gradient stat-card-featured">
        <div class="stat-icon-large">🎓</div>
        <div class="stat-content">
          <div class="stat-label">Current CGPA</div>
          <div class="stat-value-large"><%=displayCgpa%></div>
          <div class="stat-subtitle">Out of 10.0</div>
          <!-- CGPA Progress Bar -->
          <div class="progress-bar-container">
            <div class="progress-bar" style="width: <%=cgpaPercentage%>%">
              <span class="progress-label"><%=String.format("%.0f", cgpaPercentage)%>%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-container">
      <!-- SGPA Comparison Chart -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">📊 Semester Performance</h3>
          <span class="chart-subtitle">SGPA Comparison</span>
        </div>
        <div class="chart-body">
          <% if (hasAcademicData) { %>
            <canvas id="sgpaChart"></canvas>
          <% } else { %>
            <!-- Empty State -->
            <div class="empty-state">
              <div class="empty-state-icon">📚</div>
              <h4 class="empty-state-title">No Academic Data Available</h4>
              <p class="empty-state-text">Your academic records will appear here once they are added to the system.</p>
            </div>
          <% } %>
        </div>
      </div>

      <!-- Academic Summary -->
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">📈 Academic Summary</h3>
          <span class="chart-subtitle">Performance Overview</span>
        </div>
        <div class="chart-body">
          <% if (hasAcademicData) { %>
            <div class="summary-list">
              <div class="summary-item">
                <div class="summary-label">
                  <span class="summary-dot dot-blue"></span>
                  Semester 2 SGPA
                </div>
                <div class="summary-value"><%=displaySem2%></div>
              </div>
              <div class="summary-item">
                <div class="summary-label">
                  <span class="summary-dot dot-purple"></span>
                  Semester 3 SGPA
                </div>
                <div class="summary-value"><%=displaySem3%></div>
              </div>
              <div class="summary-item summary-item-highlight">
                <div class="summary-label">
                  <span class="summary-dot dot-gradient"></span>
                  Overall CGPA
                </div>
                <div class="summary-value"><%=displayCgpa%></div>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <div class="summary-label">
                  <span class="summary-dot dot-gray"></span>
                  Total Semesters
                </div>
                <div class="summary-value">2</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">
                  <span class="summary-dot dot-green"></span>
                  Status
                </div>
                <div class="summary-value">
                  <span class="badge badge-success">Active</span>
                </div>
              </div>
            </div>
          <% } else { %>
            <!-- Empty State -->
            <div class="empty-state">
              <div class="empty-state-icon">📊</div>
              <h4 class="empty-state-title">No Summary Available</h4>
              <p class="empty-state-text">Academic summary will be displayed once your records are available.</p>
            </div>
          <% } %>
        </div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <div class="action-card">
        <div class="action-icon">💼</div>
        <div class="action-content">
          <h4>Job Applications</h4>
          <p>View and apply to placement opportunities</p>
        </div>
        <button class="btn-action">Coming Soon</button>
      </div>
      <div class="action-card">
        <div class="action-icon">📄</div>
        <div class="action-content">
          <h4>Documents</h4>
          <p>Upload resume and certificates</p>
        </div>
        <button class="btn-action">Coming Soon</button>
      </div>
      <div class="action-card">
        <div class="action-icon">🔔</div>
        <div class="action-content">
          <h4>Notifications</h4>
          <p>Stay updated with latest announcements</p>
        </div>
        <button class="btn-action">Coming Soon</button>
      </div>
    </div>
  </main>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
  <script>
    // Page Load Handler with Fade-in Effect
    window.addEventListener('load', function() {
      setTimeout(function() {
        document.getElementById('loadingOverlay').classList.add('fade-out');
        document.querySelector('.page-content').classList.add('fade-in');
        
        setTimeout(function() {
          document.getElementById('loadingOverlay').style.display = 'none';
        }, 300);
      }, 400);
    });

    // Chart.js Configuration (only if we have data)
    <% if (hasAcademicData) { %>
    const ctx = document.getElementById('sgpaChart');
    
    const chartData = {
      labels: ['Semester 2', 'Semester 3'],
      datasets: [{
        label: 'SGPA',
        data: [<%=sem2Value%>, <%=sem3Value%>],
        backgroundColor: [
          'rgba(59, 130, 246, 0.8)',
          'rgba(147, 51, 234, 0.8)'
        ],
        borderColor: [
          'rgba(59, 130, 246, 1)',
          'rgba(147, 51, 234, 1)'
        ],
        borderWidth: 2,
        borderRadius: 8,
        barThickness: 60
      }]
    };

    // Responsive chart configuration
    const isSmallScreen = window.innerWidth < 375;
    
    const config = {
      type: 'bar',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: !isSmallScreen,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            padding: 12,
            titleFont: {
              size: 14,
              weight: 'bold'
            },
            bodyFont: {
              size: 13
            },
            borderColor: 'rgba(255, 255, 255, 0.1)',
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                return 'SGPA: ' + context.parsed.y.toFixed(2);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            max: 10,
            ticks: {
              stepSize: 2,
              font: {
                size: 12
              }
            },
            grid: {
              color: 'rgba(0, 0, 0, 0.05)'
            }
          },
          x: {
            ticks: {
              font: {
                size: 13,
                weight: '500'
              }
            },
            grid: {
              display: false
            }
          }
        }
      }
    };

    new Chart(ctx, config);
    
    // Handle window resize for chart responsiveness
    window.addEventListener('resize', function() {
      const chart = Chart.getChart('sgpaChart');
      if (chart) {
        chart.options.maintainAspectRatio = window.innerWidth >= 375;
        chart.update();
      }
    });
    <% } %>
  </script>
</body>
</html>
