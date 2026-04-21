<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rit.placement.model.User" %>
<%@ page import="java.util.Map" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    Integer totalStudents = (Integer) request.getAttribute("totalStudents");
    Integer eligibleStudents = (Integer) request.getAttribute("eligibleStudents");
    Integer studentsApplied = (Integer) request.getAttribute("studentsApplied");
    Integer studentsShortlisted = (Integer) request.getAttribute("studentsShortlisted");
    Integer studentsPlaced = (Integer) request.getAttribute("studentsPlaced");
    Integer totalApplications = (Integer) request.getAttribute("totalApplications");
    Integer activeJobs = (Integer) request.getAttribute("activeJobs");
    Double placementPercentage = (Double) request.getAttribute("placementPercentage");
    Double applicationRate = (Double) request.getAttribute("applicationRate");
    
    Map<String, Integer> applicationsByCompany = (Map<String, Integer>) request.getAttribute("applicationsByCompany");
    Map<String, Integer> monthlyPlacements = (Map<String, Integer>) request.getAttribute("monthlyPlacements");
    Map<String, Integer> statusBreakdown = (Map<String, Integer>) request.getAttribute("statusBreakdown");
    
    // Default values if null
    if (totalStudents == null) totalStudents = 0;
    if (eligibleStudents == null) eligibleStudents = 0;
    if (studentsApplied == null) studentsApplied = 0;
    if (studentsShortlisted == null) studentsShortlisted = 0;
    if (studentsPlaced == null) studentsPlaced = 0;
    if (totalApplications == null) totalApplications = 0;
    if (activeJobs == null) activeJobs = 0;
    if (placementPercentage == null) placementPercentage = 0.0;
    if (applicationRate == null) applicationRate = 0.0;
    
    int notPlaced = totalStudents - studentsPlaced;
%>
<!DOCTYPE html>
<html>
<head>
    <title>Placement Intelligence Dashboard - RIT</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
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
        .navbar h1 { font-size: 1.5rem; display: flex; align-items: center; gap: 0.5rem; }
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
        .header { 
            background: white; 
            padding: 2rem; 
            border-radius: 12px; 
            margin-bottom: 2rem; 
            box-shadow: 0 8px 16px rgba(0,0,0,0.1); 
        }
        .header h2 { color: #2c3e50; margin-bottom: 0.5rem; }
        .header p { color: #7f8c8d; }
        
        /* KPI Cards */
        .kpi-grid { 
            display: grid; 
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); 
            gap: 1.5rem; 
            margin-bottom: 2rem; 
        }
        .kpi-card { 
            background: white; 
            padding: 1.5rem; 
            border-radius: 12px; 
            box-shadow: 0 4px 12px rgba(0,0,0,0.1); 
            transition: transform 0.3s, box-shadow 0.3s; 
            position: relative; 
            overflow: hidden; 
        }
        .kpi-card:hover { 
            transform: translateY(-5px); 
            box-shadow: 0 8px 20px rgba(0,0,0,0.15); 
        }
        .kpi-card::before { 
            content: ''; 
            position: absolute; 
            top: 0; 
            left: 0; 
            width: 100%; 
            height: 4px; 
        }
        .kpi-card:nth-child(1)::before { background: linear-gradient(90deg, #667eea, #764ba2); }
        .kpi-card:nth-child(2)::before { background: linear-gradient(90deg, #f093fb, #f5576c); }
        .kpi-card:nth-child(3)::before { background: linear-gradient(90deg, #4facfe, #00f2fe); }
        .kpi-card:nth-child(4)::before { background: linear-gradient(90deg, #43e97b, #38f9d7); }
        .kpi-card:nth-child(5)::before { background: linear-gradient(90deg, #fa709a, #fee140); }
        .kpi-card:nth-child(6)::before { background: linear-gradient(90deg, #30cfd0, #330867); }
        .kpi-card:nth-child(7)::before { background: linear-gradient(90deg, #a8edea, #fed6e3); }
        
        .kpi-label { 
            color: #7f8c8d; 
            font-size: 0.85rem; 
            text-transform: uppercase; 
            letter-spacing: 0.5px; 
            margin-bottom: 0.5rem; 
        }
        .kpi-value { 
            font-size: 2.5rem; 
            font-weight: bold; 
            color: #2c3e50; 
            margin-bottom: 0.25rem; 
        }
        .kpi-subtitle { 
            color: #95a5a6; 
            font-size: 0.9rem; 
        }
        .kpi-icon { 
            position: absolute; 
            right: 1rem; 
            top: 1rem; 
            font-size: 2.5rem; 
            opacity: 0.15; 
        }
        
        /* Charts Section */
        .charts-grid { 
            display: grid; 
            grid-template-columns: repeat(auto-fit, minmax(500px, 1fr)); 
            gap: 2rem; 
            margin-bottom: 2rem; 
        }
        .chart-card { 
            background: white; 
            padding: 2rem; 
            border-radius: 12px; 
            box-shadow: 0 4px 12px rgba(0,0,0,0.1); 
        }
        .chart-card h3 { 
            color: #2c3e50; 
            margin-bottom: 1.5rem; 
            text-align: center; 
            font-size: 1.2rem; 
        }
        .chart-container { 
            position: relative; 
            height: 300px; 
        }
        
        .alert { 
            padding: 1rem; 
            border-radius: 8px; 
            margin-bottom: 1.5rem; 
            background: #f8d7da; 
            color: #721c24; 
            border: 1px solid #f5c6cb; 
        }
        
        @media (max-width: 768px) {
            .kpi-grid { grid-template-columns: 1fr; }
            .charts-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <div class="navbar">
        <h1><span>📊</span> Placement Intelligence Dashboard</h1>
        <div>
            <span>Welcome, <strong><%= user.getName() %></strong></span>
            <a href="<%= request.getContextPath() %>/admin/dashboard">Dashboard</a>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>
    
    <div class="container">
        <div class="header">
            <h2>📈 Real-Time Placement Analytics</h2>
            <p>Comprehensive insights into placement activities and student performance</p>
        </div>
        
        <% if (request.getAttribute("error") != null) { %>
        <div class="alert">
            <%= request.getAttribute("error") %>
        </div>
        <% } %>
        
        <!-- KPI Cards -->
        <div class="kpi-grid">
            <div class="kpi-card">
                <div class="kpi-icon">👥</div>
                <div class="kpi-label">Total Students</div>
                <div class="kpi-value"><%= totalStudents %></div>
                <div class="kpi-subtitle">Registered students</div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon">✓</div>
                <div class="kpi-label">Eligible Students</div>
                <div class="kpi-value"><%= eligibleStudents %></div>
                <div class="kpi-subtitle">CGPA ≥ 6.0</div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon">📝</div>
                <div class="kpi-label">Students Applied</div>
                <div class="kpi-value"><%= studentsApplied %></div>
                <div class="kpi-subtitle"><%= String.format("%.1f%%", applicationRate) %> application rate</div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon">⭐</div>
                <div class="kpi-label">Shortlisted</div>
                <div class="kpi-value"><%= studentsShortlisted %></div>
                <div class="kpi-subtitle">In selection process</div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon">🎉</div>
                <div class="kpi-label">Students Placed</div>
                <div class="kpi-value" style="color: #27ae60;"><%= studentsPlaced %></div>
                <div class="kpi-subtitle"><%= String.format("%.1f%%", placementPercentage) %> placement rate</div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon">📊</div>
                <div class="kpi-label">Total Applications</div>
                <div class="kpi-value"><%= totalApplications %></div>
                <div class="kpi-subtitle">All submissions</div>
            </div>
            
            <div class="kpi-card">
                <div class="kpi-icon">💼</div>
                <div class="kpi-label">Active Jobs</div>
                <div class="kpi-value"><%= activeJobs %></div>
                <div class="kpi-subtitle">Open positions</div>
            </div>
        </div>
        
        <!-- Charts Section -->
        <div class="charts-grid">
            <!-- Placement Status Doughnut Chart -->
            <div class="chart-card">
                <h3>📊 Placement Status Distribution</h3>
                <div class="chart-container">
                    <canvas id="placementStatusChart"></canvas>
                </div>
            </div>
            
            <!-- Applications by Company Bar Chart -->
            <div class="chart-card">
                <h3>🏢 Top Companies by Applications</h3>
                <div class="chart-container">
                    <canvas id="companyChart"></canvas>
                </div>
            </div>
            
            <!-- Monthly Placement Trend Line Chart -->
            <div class="chart-card" style="grid-column: 1 / -1;">
                <h3>📈 Monthly Placement Trends (Last 12 Months)</h3>
                <div class="chart-container">
                    <canvas id="trendChart"></canvas>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        // Placement Status Doughnut Chart
        const statusCtx = document.getElementById('placementStatusChart').getContext('2d');
        new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: ['Placed', 'Not Placed'],
                datasets: [{
                    data: [<%= studentsPlaced %>, <%= notPlaced %>],
                    backgroundColor: [
                        'rgba(67, 233, 123, 0.8)',
                        'rgba(149, 165, 166, 0.8)'
                    ],
                    borderColor: [
                        'rgba(67, 233, 123, 1)',
                        'rgba(149, 165, 166, 1)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 15,
                            font: { size: 14 }
                        }
                    }
                }
            }
        });
        
        // Applications by Company Bar Chart
        const companyCtx = document.getElementById('companyChart').getContext('2d');
        const companyLabels = [
            <% if (applicationsByCompany != null) {
                int count = 0;
                for (String company : applicationsByCompany.keySet()) {
                    out.print("'" + company.replace("'", "\\'") + "'");
                    if (++count < applicationsByCompany.size()) out.print(",");
                }
            } %>
        ];
        const companyData = [
            <% if (applicationsByCompany != null) {
                int count = 0;
                for (Integer appCount : applicationsByCompany.values()) {
                    out.print(appCount);
                    if (++count < applicationsByCompany.size()) out.print(",");
                }
            } %>
        ];
        
        new Chart(companyCtx, {
            type: 'bar',
            data: {
                labels: companyLabels,
                datasets: [{
                    label: 'Applications',
                    data: companyData,
                    backgroundColor: 'rgba(102, 126, 234, 0.8)',
                    borderColor: 'rgba(102, 126, 234, 1)',
                    borderWidth: 2,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
        
        // Monthly Placement Trend Line Chart
        const trendCtx = document.getElementById('trendChart').getContext('2d');
        const monthLabels = [
            <% if (monthlyPlacements != null) {
                int count = 0;
                for (String month : monthlyPlacements.keySet()) {
                    out.print("'" + month + "'");
                    if (++count < monthlyPlacements.size()) out.print(",");
                }
            } %>
        ];
        const monthData = [
            <% if (monthlyPlacements != null) {
                int count = 0;
                for (Integer placedCount : monthlyPlacements.values()) {
                    out.print(placedCount);
                    if (++count < monthlyPlacements.size()) out.print(",");
                }
            } %>
        ];
        
        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: monthLabels,
                datasets: [{
                    label: 'Students Placed',
                    data: monthData,
                    borderColor: 'rgba(67, 233, 123, 1)',
                    backgroundColor: 'rgba(67, 233, 123, 0.2)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                }
            }
        });
    </script>
</body>
</html>
