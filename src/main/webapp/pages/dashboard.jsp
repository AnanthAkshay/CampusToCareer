<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.rit.placement.service.JobRecommendationService.RecommendedJob" %>
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
  
  // Readiness data
  Integer readinessScore = (Integer) request.getAttribute("readinessScore");
  String readinessLevel = (String) request.getAttribute("readinessLevel");
  String readinessColor = (String) request.getAttribute("readinessColor");
  List<String> recommendations = (List<String>) request.getAttribute("recommendations");
  Integer applicationsCount = (Integer) request.getAttribute("applicationsCount");
  
  // Job recommendations
  List<RecommendedJob> jobRecommendations = (List<RecommendedJob>) request.getAttribute("jobRecommendations");

  // Safe display with null handling
  String displayName = (name != null) ? name : "Student";
  String displayUsn = (usn != null) ? usn : "N/A";
  String displaySem2 = (sem2Sgpa != null) ? String.format("%.2f", sem2Sgpa) : "N/A";
  String displaySem3 = (sem3Sgpa != null) ? String.format("%.2f", sem3Sgpa) : "N/A";
  String displayCgpa = (cgpa != null) ? String.format("%.2f", cgpa) : "N/A";
  
  // Readiness defaults
  int scoreValue = (readinessScore != null) ? readinessScore : 0;
  String levelText = (readinessLevel != null) ? readinessLevel : "Unknown";
  String colorCode = (readinessColor != null) ? readinessColor : "gray";
  int appsCount = (applicationsCount != null) ? applicationsCount : 0;
  
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
  <style>
    /* Readiness Score Styles */
    .stat-card-readiness {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 2rem;
    }
    
    .readiness-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
    }
    
    .readiness-icon {
      font-size: 4rem;
      opacity: 0.3;
    }
    
    .readiness-score-green { color: #10b981; }
    .readiness-score-blue { color: #3b82f6; }
    .readiness-score-orange { color: #f59e0b; }
    .readiness-score-red { color: #ef4444; }
    
    .readiness-badge {
      display: inline-block;
      padding: 0.4rem 1rem;
      border-radius: 20px;
      font-size: 0.9rem;
      font-weight: 600;
      margin-top: 0.5rem;
    }
    
    .readiness-badge-green { background: rgba(16, 185, 129, 0.2); color: #10b981; }
    .readiness-badge-blue { background: rgba(59, 130, 246, 0.2); color: #3b82f6; }
    .readiness-badge-orange { background: rgba(245, 158, 11, 0.2); color: #f59e0b; }
    .readiness-badge-red { background: rgba(239, 68, 68, 0.2); color: #ef4444; }
    
    .readiness-progress-container {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 10px;
      height: 30px;
      overflow: hidden;
      margin-bottom: 1.5rem;
    }
    
    .readiness-progress-bar {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding-right: 10px;
      transition: width 1s ease-in-out;
      border-radius: 10px;
    }
    
    .readiness-bar-green { background: linear-gradient(90deg, #10b981, #059669); }
    .readiness-bar-blue { background: linear-gradient(90deg, #3b82f6, #2563eb); }
    .readiness-bar-orange { background: linear-gradient(90deg, #f59e0b, #d97706); }
    .readiness-bar-red { background: linear-gradient(90deg, #ef4444, #dc2626); }
    
    .readiness-progress-label {
      color: white;
      font-weight: 600;
      font-size: 0.9rem;
    }
    
    .score-breakdown {
      display: flex;
      gap: 1.5rem;
      flex-wrap: wrap;
    }
    
    .breakdown-item {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
    }
    
    .breakdown-label {
      font-size: 0.85rem;
      opacity: 0.9;
    }
    
    .breakdown-value {
      font-weight: 600;
      font-size: 0.9rem;
    }
    
    /* Recommendations Section */
    .recommendations-section {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      margin-bottom: 2rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    
    .recommendations-header {
      margin-bottom: 1.5rem;
    }
    
    .recommendations-title {
      font-size: 1.5rem;
      color: #1f2937;
      margin-bottom: 0.5rem;
    }
    
    .recommendations-subtitle {
      color: #6b7280;
      font-size: 0.95rem;
    }
    
    .recommendations-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    
    .recommendation-item {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      background: #f9fafb;
      border-left: 4px solid #3b82f6;
      border-radius: 8px;
      transition: all 0.2s;
    }
    
    .recommendation-item:hover {
      background: #f3f4f6;
      transform: translateX(5px);
    }
    
    .recommendation-icon {
      flex-shrink: 0;
      width: 24px;
      height: 24px;
      background: #3b82f6;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.8rem;
      font-weight: bold;
    }
    
    .recommendation-text {
      flex: 1;
      color: #374151;
      line-height: 1.6;
    }
    
    @media (max-width: 768px) {
      .stat-card-readiness {
        grid-column: span 1 !important;
      }
      
      .score-breakdown {
        flex-direction: column;
        gap: 1rem;
      }
    }
  </style>
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
        <h1 class="page-title">Welcome back, <%= com.rit.placement.util.XSSUtil.escape(displayName) %>! 👋</h1>
        <p class="page-subtitle">
          <span class="badge badge-info">STUDENT</span>
          <span class="usn-badge">USN: <%= com.rit.placement.util.XSSUtil.escape(displayUsn) %></span>
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
      <!-- Placement Readiness Score Card (Featured) -->
      <div class="stat-card stat-card-readiness stat-card-featured" style="grid-column: span 2;">
        <div class="readiness-header">
          <div>
            <div class="stat-label">🎯 Placement Readiness Score</div>
            <div class="stat-value-large readiness-score-<%= com.rit.placement.util.XSSUtil.escape(colorCode) %>"><%= com.rit.placement.util.XSSUtil.escape(scoreValue) %>/100</div>
            <div class="stat-subtitle">
              <span class="readiness-badge readiness-badge-<%= com.rit.placement.util.XSSUtil.escape(colorCode) %>"><%= com.rit.placement.util.XSSUtil.escape(levelText) %></span>
            </div>
          </div>
          <div class="readiness-icon">
            <% if (scoreValue >= 80) { %>
              🌟
            <% } else if (scoreValue >= 60) { %>
              ⭐
            <% } else if (scoreValue >= 40) { %>
              ✨
            <% } else { %>
              💫
            <% } %>
          </div>
        </div>
        <!-- Progress Bar -->
        <div class="readiness-progress-container">
          <div class="readiness-progress-bar readiness-bar-<%= com.rit.placement.util.XSSUtil.escape(colorCode) %>" style="width: <%= com.rit.placement.util.XSSUtil.escape(scoreValue) %>%">
            <span class="readiness-progress-label"><%= com.rit.placement.util.XSSUtil.escape(scoreValue) %>%</span>
          </div>
        </div>
        <!-- Score Breakdown -->
        <div class="score-breakdown">
          <div class="breakdown-item">
            <span class="breakdown-label">📚 CGPA</span>
            <span class="breakdown-value">0-50 pts</span>
          </div>
          <div class="breakdown-item">
            <span class="breakdown-label">📝 Applications</span>
            <span class="breakdown-value"><%= com.rit.placement.util.XSSUtil.escape(appsCount) %> submitted (0-30 pts)</span>
          </div>
          <div class="breakdown-item">
            <span class="breakdown-label">🛠️ Skills</span>
            <span class="breakdown-value">0-20 pts</span>
          </div>
        </div>
      </div>

      <!-- Semester 2 Card -->
      <div class="stat-card stat-card-blue">
        <div class="stat-icon">📘</div>
        <div class="stat-content">
          <div class="stat-label">Semester 2 SGPA</div>
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(displaySem2) %></div>
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
          <div class="stat-value"><%= com.rit.placement.util.XSSUtil.escape(displaySem3) %></div>
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
          <div class="stat-value-large"><%= com.rit.placement.util.XSSUtil.escape(displayCgpa) %></div>
          <div class="stat-subtitle">Out of 10.0</div>
          <!-- CGPA Progress Bar -->
          <div class="progress-bar-container">
            <div class="progress-bar" style="width: <%= com.rit.placement.util.XSSUtil.escape(cgpaPercentage) %>%">
              <span class="progress-label"><%= com.rit.placement.util.XSSUtil.escape(String.format("%.0f", cgpaPercentage)) %>%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Recommendations Section -->
    <% if (recommendations != null && !recommendations.isEmpty()) { %>
    <div class="recommendations-section">
      <div class="recommendations-header">
        <h3 class="recommendations-title">💡 Personalized Recommendations</h3>
        <p class="recommendations-subtitle">Action items to improve your placement readiness</p>
      </div>
      <div class="recommendations-list">
        <% for (String recommendation : recommendations) { %>
        <div class="recommendation-item">
          <div class="recommendation-icon">✓</div>
          <div class="recommendation-text"><%= com.rit.placement.util.XSSUtil.escape(recommendation) %></div>
        </div>
        <% } %>
      </div>
    </div>
    <% } %>

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
                <div class="summary-value"><%= com.rit.placement.util.XSSUtil.escape(displaySem2) %></div>
              </div>
              <div class="summary-item">
                <div class="summary-label">
                  <span class="summary-dot dot-purple"></span>
                  Semester 3 SGPA
                </div>
                <div class="summary-value"><%= com.rit.placement.util.XSSUtil.escape(displaySem3) %></div>
              </div>
              <div class="summary-item summary-item-highlight">
                <div class="summary-label">
                  <span class="summary-dot dot-gradient"></span>
                  Overall CGPA
                </div>
                <div class="summary-value"><%= com.rit.placement.util.XSSUtil.escape(displayCgpa) %></div>
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
        data: [<%= com.rit.placement.util.XSSUtil.escape(sem2Value) %>, <%= com.rit.placement.util.XSSUtil.escape(sem3Value) %>],
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
