<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rit.placement.model.User" %>
<%@ page import="com.rit.placement.model.StudentPerformance" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"PROCTOR".equals(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    List<StudentPerformance> students = (List<StudentPerformance>) request.getAttribute("students");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Proctor Dashboard - RIT Placement</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }
        .navbar { background: rgba(44, 62, 80, 0.95); backdrop-filter: blur(10px); color: white; padding: 1rem 2rem; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .navbar h1 { font-size: 1.5rem; display: flex; align-items: center; gap: 0.5rem; }
        .navbar a { color: white; text-decoration: none; margin-left: 1rem; padding: 0.5rem 1rem; border-radius: 4px; transition: background 0.3s; }
        .navbar a:hover { background: rgba(255,255,255,0.1); }
        .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
        .header { background: white; padding: 2rem; border-radius: 12px; margin-bottom: 2rem; box-shadow: 0 8px 16px rgba(0,0,0,0.1); }
        .header h2 { color: #2c3e50; margin-bottom: 0.5rem; }
        .header p { color: #7f8c8d; }
        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
        .stat-card { background: white; padding: 1.5rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); transition: transform 0.3s, box-shadow 0.3s; position: relative; overflow: hidden; }
        .stat-card:hover { transform: translateY(-5px); box-shadow: 0 8px 20px rgba(0,0,0,0.15); }
        .stat-card::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 4px; }
        .stat-card:nth-child(1)::before { background: linear-gradient(90deg, #667eea, #764ba2); }
        .stat-card:nth-child(2)::before { background: linear-gradient(90deg, #f093fb, #f5576c); }
        .stat-card:nth-child(3)::before { background: linear-gradient(90deg, #4facfe, #00f2fe); }
        .stat-card:nth-child(4)::before { background: linear-gradient(90deg, #43e97b, #38f9d7); }
        .stat-card h3 { color: #7f8c8d; font-size: 0.85rem; margin-bottom: 0.5rem; text-transform: uppercase; letter-spacing: 0.5px; }
        .stat-card .number { font-size: 2.5rem; font-weight: bold; color: #2c3e50; }
        .stat-card .icon { position: absolute; right: 1rem; top: 1rem; font-size: 2rem; opacity: 0.2; }
        .filters { background: white; padding: 1.5rem; border-radius: 12px; margin-bottom: 2rem; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .filters h3 { color: #2c3e50; margin-bottom: 1rem; }
        .filter-group { display: flex; gap: 1rem; flex-wrap: wrap; align-items: center; }
        .filter-group input { flex: 1; min-width: 250px; padding: 0.75rem; border: 2px solid #ecf0f1; border-radius: 8px; font-size: 1rem; transition: border 0.3s; }
        .filter-group input:focus { outline: none; border-color: #3498db; }
        .filter-group select { padding: 0.75rem 1rem; border: 2px solid #ecf0f1; border-radius: 8px; font-size: 1rem; cursor: pointer; transition: border 0.3s; }
        .filter-group select:focus { outline: none; border-color: #3498db; }
        .filter-group button { padding: 0.75rem 1.5rem; background: #3498db; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 1rem; transition: background 0.3s; }
        .filter-group button:hover { background: #2980b9; }
        .charts-section { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 2rem; margin-bottom: 2rem; }
        .chart-card { background: white; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .chart-card h3 { color: #2c3e50; margin-bottom: 1.5rem; text-align: center; }
        .table-container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 1rem; text-align: left; border-bottom: 1px solid #ecf0f1; }
        th { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-weight: 600; text-transform: uppercase; font-size: 0.85rem; letter-spacing: 0.5px; }
        tbody tr { transition: background 0.2s; }
        tbody tr:hover { background: #f8f9fa; }
        .risk-badge { padding: 0.4rem 0.9rem; border-radius: 20px; font-size: 0.8rem; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
        .risk-at-risk { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; }
        .risk-safe { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: white; }
        .status-placed { color: #27ae60; font-weight: 600; }
        .status-not-placed { color: #e67e22; font-weight: 600; }
        .btn { padding: 0.6rem 1.2rem; border: none; border-radius: 8px; cursor: pointer; text-decoration: none; display: inline-block; font-weight: 600; transition: all 0.3s; }
        .btn-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4); }
        .no-data { text-align: center; padding: 3rem; color: #7f8c8d; }
        @media (max-width: 768px) {
            .stats { grid-template-columns: 1fr; }
            .charts-section { grid-template-columns: 1fr; }
            .filter-group { flex-direction: column; }
            .filter-group input { min-width: 100%; }
        }
    </style>
</head>
<body>
    <div class="navbar">
        <h1><span>🎓</span> RIT Placement - Proctor Dashboard</h1>
        <div>
            <span>Welcome, <strong><%= user.getName() %></strong></span>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>
    
    <div class="container">
        <div class="header">
            <h2>📊 Student Performance Monitoring</h2>
            <p>Track your students' placement progress and identify at-risk students</p>
        </div>
        
        <% 
        int totalStudents = students != null ? students.size() : 0;
        int atRiskCount = 0;
        int placedCount = 0;
        double totalCGPA = 0;
        if (students != null) {
            for (StudentPerformance sp : students) {
                if ("AT RISK".equals(sp.getRiskStatus())) atRiskCount++;
                if (sp.isPlaced()) placedCount++;
                totalCGPA += sp.getCgpa();
            }
        }
        double avgCGPA = totalStudents > 0 ? totalCGPA / totalStudents : 0;
        int safeCount = totalStudents - atRiskCount;
        %>
        
        <div class="stats">
            <div class="stat-card">
                <div class="icon">👥</div>
                <h3>Total Students</h3>
                <div class="number"><%= totalStudents %></div>
            </div>
            <div class="stat-card">
                <div class="icon">⚠️</div>
                <h3>At Risk</h3>
                <div class="number" style="color: #f5576c;"><%= atRiskCount %></div>
            </div>
            <div class="stat-card">
                <div class="icon">✓</div>
                <h3>Placed</h3>
                <div class="number" style="color: #43e97b;"><%= placedCount %></div>
            </div>
            <div class="stat-card">
                <div class="icon">📈</div>
                <h3>Average CGPA</h3>
                <div class="number" style="color: #667eea;"><%= String.format("%.2f", avgCGPA) %></div>
            </div>
        </div>
        
        <div class="filters">
            <h3>🔍 Search & Filter</h3>
            <div class="filter-group">
                <input type="text" id="searchInput" placeholder="Search by USN, name or email..." onkeyup="filterTable()">
                <select id="riskFilter" onchange="filterTable()">
                    <option value="all">All Students</option>
                    <option value="AT RISK">At Risk Only</option>
                    <option value="SAFE">Safe Only</option>
                </select>
                <select id="placementFilter" onchange="filterTable()">
                    <option value="all">All Placement Status</option>
                    <option value="placed">Placed Only</option>
                    <option value="not-placed">Not Placed Only</option>
                </select>
                <button onclick="resetFilters()">Reset</button>
            </div>
        </div>
        
        <div class="charts-section">
            <div class="chart-card">
                <h3>📊 Risk Status Distribution</h3>
                <canvas id="riskChart"></canvas>
            </div>
            <div class="chart-card">
                <h3>📈 Applications per Student</h3>
                <canvas id="applicationsChart"></canvas>
            </div>
        </div>
        
        <div class="table-container">
        <table id="studentsTable">
            <thead>
                <tr>
                    <th>USN</th>
                    <th>Student Name</th>
                    <th>Email</th>
                    <th>Branch</th>
                    <th>CGPA</th>
                    <th>Applications</th>
                    <th>Placement Status</th>
                    <th>Risk Status</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <% if (students != null && !students.isEmpty()) {
                    for (StudentPerformance sp : students) { %>
                <tr>
                    <td><%= sp.getUsn() %></td>
                    <td><%= sp.getName() %></td>
                    <td><%= sp.getEmail() %></td>
                    <td><%= sp.getBranch() %></td>
                    <td><%= String.format("%.2f", sp.getCgpa()) %></td>
                    <td><%= sp.getApplicationsCount() %></td>
                    <td class="<%= sp.isPlaced() ? "status-placed" : "status-not-placed" %>">
                        <%= sp.isPlaced() ? "✓ PLACED" : "NOT PLACED" %>
                    </td>
                    <td>
                        <span class="risk-badge <%= "AT RISK".equals(sp.getRiskStatus()) ? "risk-at-risk" : "risk-safe" %>">
                            <%= sp.getRiskStatus() %>
                        </span>
                    </td>
                    <td>
                        <a href="<%= request.getContextPath() %>/proctor/student-detail?id=<%= sp.getStudentId() %>" 
                           class="btn btn-primary">View Details</a>
                    </td>
                </tr>
                <% }
                } else { %>
                <tr>
                    <td colspan="9" style="text-align: center; padding: 2rem; color: #7f8c8d;">
                        No students assigned to you yet.
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>
        </div>
    </div>
    
    <script>
        // Chart.js - Risk Status Pie Chart
        const riskCtx = document.getElementById('riskChart').getContext('2d');
        new Chart(riskCtx, {
            type: 'doughnut',
            data: {
                labels: ['Safe', 'At Risk'],
                datasets: [{
                    data: [<%= safeCount %>, <%= atRiskCount %>],
                    backgroundColor: [
                        'rgba(67, 233, 123, 0.8)',
                        'rgba(245, 87, 108, 0.8)'
                    ],
                    borderColor: [
                        'rgba(67, 233, 123, 1)',
                        'rgba(245, 87, 108, 1)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
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
        
        // Chart.js - Applications Bar Chart
        const appCtx = document.getElementById('applicationsChart').getContext('2d');
        const studentNames = [
            <% if (students != null) {
                for (int i = 0; i < Math.min(students.size(), 10); i++) {
                    StudentPerformance sp = students.get(i);
                    out.print("'" + sp.getName().split(" ")[0] + "'");
                    if (i < Math.min(students.size(), 10) - 1) out.print(",");
                }
            } %>
        ];
        const applicationCounts = [
            <% if (students != null) {
                for (int i = 0; i < Math.min(students.size(), 10); i++) {
                    StudentPerformance sp = students.get(i);
                    out.print(sp.getApplicationsCount());
                    if (i < Math.min(students.size(), 10) - 1) out.print(",");
                }
            } %>
        ];
        
        new Chart(appCtx, {
            type: 'bar',
            data: {
                labels: studentNames,
                datasets: [{
                    label: 'Applications',
                    data: applicationCounts,
                    backgroundColor: 'rgba(102, 126, 234, 0.8)',
                    borderColor: 'rgba(102, 126, 234, 1)',
                    borderWidth: 2,
                    borderRadius: 8
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
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
        
        // Filter functionality
        function filterTable() {
            const searchValue = document.getElementById('searchInput').value.toLowerCase();
            const riskFilter = document.getElementById('riskFilter').value;
            const placementFilter = document.getElementById('placementFilter').value;
            const table = document.getElementById('studentsTable');
            const rows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
            
            for (let i = 0; i < rows.length; i++) {
                const row = rows[i];
                if (row.cells.length < 9) continue; // Skip empty rows
                
                const usn = row.cells[0].textContent.toLowerCase();
                const name = row.cells[1].textContent.toLowerCase();
                const email = row.cells[2].textContent.toLowerCase();
                const riskStatus = row.cells[7].textContent.trim();
                const placementStatus = row.cells[6].textContent.trim();
                
                let showRow = true;
                
                // Search filter
                if (searchValue && !usn.includes(searchValue) && !name.includes(searchValue) && !email.includes(searchValue)) {
                    showRow = false;
                }
                
                // Risk filter
                if (riskFilter !== 'all' && riskStatus !== riskFilter) {
                    showRow = false;
                }
                
                // Placement filter
                if (placementFilter === 'placed' && !placementStatus.includes('PLACED')) {
                    showRow = false;
                } else if (placementFilter === 'not-placed' && placementStatus.includes('PLACED')) {
                    showRow = false;
                }
                
                row.style.display = showRow ? '' : 'none';
            }
        }
        
        function resetFilters() {
            document.getElementById('searchInput').value = '';
            document.getElementById('riskFilter').value = 'all';
            document.getElementById('placementFilter').value = 'all';
            filterTable();
        }
    </script>
</body>
</html>
