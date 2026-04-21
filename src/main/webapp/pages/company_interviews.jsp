<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  if (session.getAttribute("user_id") == null || !"COMPANY".equals(session.getAttribute("role"))) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  Company company = (Company) request.getAttribute("company");
  List<Interview> interviews = (List<Interview>) request.getAttribute("interviews");
  List<Application> shortlistedApps = (List<Application>) request.getAttribute("shortlistedApps");
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
  
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
  <title>Interviews — <%= company != null ? company.getCompanyName() : "Company" %></title>
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
    .badge-scheduled { background: #dbeafe; color: #1e40af; }
    .badge-completed { background: #d1fae5; color: #065f46; }
    .badge-cancelled { background: #fee2e2; color: #991b1b; }
    .badge-online { background: #dbeafe; color: #1e40af; }
    .badge-offline { background: #fef3c7; color: #92400e; }
  </style>
</head>
<body>
  <div class="navbar">
    <h1>🏢 <%= company != null ? company.getCompanyName() : "Company Portal" %></h1>
    <div>
      <a href="${pageContext.request.contextPath}/company/dashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/company/jobs">Manage Jobs</a>
      <a href="${pageContext.request.contextPath}/company/applications">Applications</a>
      <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
  </div>

  <div class="container">
    <% if (successMessage != null) { %>
      <div class="alert alert-success"><%= successMessage %></div>
    <% } %>
    <% if (errorMessage != null) { %>
      <div class="alert alert-error"><%= errorMessage %></div>
    <% } %>

    <!-- Schedule Interview Form -->
    <div class="section">
      <h3>📅 Schedule Interview</h3>
      <% if (shortlistedApps != null && !shortlistedApps.isEmpty()) { %>
        <form method="post" action="${pageContext.request.contextPath}/company/interviews">
          <div class="form-grid">
            <div class="form-group">
              <label>Select Candidate *</label>
              <select name="application_id" required>
                <option value="">Choose a candidate...</option>
                <% for (Application app : shortlistedApps) { %>
                  <option value="<%= app.getId() %>">
                    <%= app.getStudentName() %> (<%= app.getStudentUsn() %>) - <%= app.getJobTitle() %>
                  </option>
                <% } %>
              </select>
            </div>
            <div class="form-group">
              <label>Interview Date & Time *</label>
              <input type="datetime-local" name="interview_datetime" required>
            </div>
            <div class="form-group">
              <label>Interview Mode *</label>
              <select name="interview_mode" required>
                <option value="ONLINE">Online</option>
                <option value="OFFLINE">Offline</option>
                <option value="PHONE">Phone</option>
              </select>
            </div>
          </div>
          <div class="form-grid">
            <div class="form-group">
              <label>Location (for offline)</label>
              <input type="text" name="interview_location" placeholder="e.g., Office Address">
            </div>
            <div class="form-group">
              <label>Meeting Link (for online)</label>
              <input type="url" name="interview_link" placeholder="e.g., https://meet.google.com/...">
            </div>
            <div class="form-group">
              <label>Notes</label>
              <textarea name="notes" placeholder="Additional instructions for candidate"></textarea>
            </div>
          </div>
          <button type="submit" class="btn btn-primary">Schedule Interview</button>
        </form>
      <% } else { %>
        <p style="color: #6b7280;">No shortlisted candidates available. Shortlist candidates from the Applications page first.</p>
      <% } %>
    </div>

    <!-- Scheduled Interviews -->
    <div class="section">
      <h3>📋 Scheduled Interviews</h3>
      <% if (interviews != null && !interviews.isEmpty()) { %>
        <table>
          <thead>
            <tr>
              <th>Student</th>
              <th>Job Role</th>
              <th>Date & Time</th>
              <th>Mode</th>
              <th>Status</th>
              <th>Details</th>
            </tr>
          </thead>
          <tbody>
            <% for (Interview interview : interviews) { %>
            <tr>
              <td>
                <strong><%= interview.getStudentName() %></strong><br>
                <small style="color: #6b7280;"><%= interview.getStudentUsn() %></small>
              </td>
              <td><%= interview.getJobTitle() %></td>
              <td><%= interview.getInterviewDate() != null ? dateFormat.format(interview.getInterviewDate()) : "N/A" %></td>
              <td>
                <span class="badge badge-<%= interview.getInterviewMode() != null ? interview.getInterviewMode().toLowerCase() : "online" %>">
                  <%= interview.getInterviewMode() %>
                </span>
              </td>
              <td>
                <span class="badge badge-<%= interview.getStatus() != null ? interview.getStatus().toLowerCase() : "scheduled" %>">
                  <%= interview.getStatus() %>
                </span>
              </td>
              <td>
                <% if ("ONLINE".equals(interview.getInterviewMode()) && interview.getInterviewLink() != null) { %>
                  <a href="<%= interview.getInterviewLink() %>" target="_blank" style="color: #3b82f6;">Join Link</a>
                <% } else if ("OFFLINE".equals(interview.getInterviewMode()) && interview.getInterviewLocation() != null) { %>
                  <%= interview.getInterviewLocation() %>
                <% } else { %>
                  -
                <% } %>
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      <% } else { %>
        <p style="text-align: center; color: #6b7280; padding: 2rem;">No interviews scheduled yet.</p>
      <% } %>
    </div>
  </div>
</body>
</html>
