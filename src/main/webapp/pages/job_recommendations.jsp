<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.service.JobRecommendationService.RecommendedJob" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null || !"STUDENT".equals(session.getAttribute("role"))) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  List<RecommendedJob> recommendations = (List<RecommendedJob>) request.getAttribute("recommendations");
  Integer totalRecommendations = (Integer) request.getAttribute("totalRecommendations");
  
  if (totalRecommendations == null) totalRecommendations = 0;
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Job Recommendations — RIT ISE Placement Portal</title>
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
    
    .header {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      margin-bottom: 2rem;
      box-shadow: 0 8px 16px rgba(0,0,0,0.1);
    }
    .header h2 { color: #2c3e50; margin-bottom: 0.5rem; }
    .header p { color: #7f8c8d; }
    
    .recommendations-grid {
      display: grid;
      gap: 1.5rem;
    }
    
    .recommendation-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      transition: transform 0.3s, box-shadow 0.3s;
      border-left: 4px solid #3b82f6;
    }
    .recommendation-card:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba(0,0,0,0.15);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
    }
    
    .job-title {
      font-size: 1.3rem;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 0.25rem;
    }
    
    .company-name {
      font-size: 1rem;
      color: #3b82f6;
      font-weight: 600;
    }
    
    .match-score {
      text-align: right;
    }
    
    .score-value {
      font-size: 2rem;
      font-weight: 700;
      line-height: 1;
    }
    
    .score-label {
      font-size: 0.75rem;
      text-transform: uppercase;
      font-weight: 600;
      margin-top: 0.25rem;
    }
    
    .job-details {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 1rem;
      margin: 1rem 0;
      padding: 1rem;
      background: #f9fafb;
      border-radius: 8px;
    }
    
    .detail-item {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    
    .detail-label {
      font-size: 0.75rem;
      color: #6b7280;
      text-transform: uppercase;
      font-weight: 600;
    }
    
    .detail-value {
      font-size: 1rem;
      color: #2c3e50;
      font-weight: 600;
    }
    
    .reasons-section {
      margin-top: 1rem;
    }
    
    .reasons-title {
      font-size: 0.85rem;
      color: #6b7280;
      font-weight: 600;
      margin-bottom: 0.5rem;
    }
    
    .reasons-list {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    
    .reason-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.9rem;
      color: #374151;
    }
    
    .reason-icon {
      width: 20px;
      height: 20px;
      background: #10b981;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.7rem;
      flex-shrink: 0;
    }
    
    .apply-button {
      margin-top: 1rem;
      padding: 0.75rem 1.5rem;
      background: #3b82f6;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.3s;
      width: 100%;
    }
    
    .apply-button:hover {
      background: #2563eb;
    }
    
    .empty-state {
      background: white;
      padding: 4rem 2rem;
      border-radius: 12px;
      text-align: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    
    .empty-icon {
      font-size: 4rem;
      margin-bottom: 1rem;
    }
    
    .empty-title {
      font-size: 1.5rem;
      color: #2c3e50;
      margin-bottom: 0.5rem;
    }
    
    .empty-text {
      color: #6b7280;
      margin-bottom: 1.5rem;
    }
    
    .info-banner {
      background: #dbeafe;
      border-left: 4px solid #3b82f6;
      padding: 1rem 1.5rem;
      border-radius: 8px;
      margin-bottom: 2rem;
    }
    
    .info-banner-title {
      font-weight: 600;
      color: #1e40af;
      margin-bottom: 0.5rem;
    }
    
    .info-banner-text {
      color: #1e3a8a;
      font-size: 0.9rem;
    }
  </style>
</head>
<body>
  <div class="navbar">
    <h1>🎯 Job Recommendations</h1>
    <div>
      <a href="${pageContext.request.contextPath}/student/dashboard">Dashboard</a>
      <a href="${pageContext.request.contextPath}/apply">Browse All Jobs</a>
      <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
  </div>

  <div class="container">
    <div class="header">
      <h2>Personalized Job Recommendations</h2>
      <p>Based on your CGPA, skills, and profile — <%= com.rit.placement.util.XSSUtil.escape(totalRecommendations) %> jobs matched</p>
    </div>

    <div class="info-banner">
      <div class="info-banner-title">💡 How Recommendations Work</div>
      <div class="info-banner-text">
        Jobs are scored 0-100 based on: CGPA match (40%), Skills match (40%), Branch eligibility (10%), and Application diversity (10%).
        Higher scores mean better matches for your profile.
      </div>
    </div>

    <% if (recommendations != null && !recommendations.isEmpty()) { %>
      <div class="recommendations-grid">
        <% for (RecommendedJob rec : recommendations) { %>
          <div class="recommendation-card">
            <div class="card-header">
              <div>
                <div class="job-title"><%= com.rit.placement.util.XSSUtil.escape(rec.getJob().getRole()) %></div>
                <div class="company-name"><%= com.rit.placement.util.XSSUtil.escape(rec.getJob().getCompanyName()) %></div>
              </div>
              <%
                int score = rec.getScore();
                String scoreColor = score >= 80 ? "#10b981" : score >= 60 ? "#3b82f6" : score >= 40 ? "#f59e0b" : "#6b7280";
                String scoreLabel = score >= 80 ? "Excellent Match" : score >= 60 ? "Good Match" : score >= 40 ? "Fair Match" : "Consider";
              %>
              <div class="match-score">
                <div class="score-value" style="color: <%= scoreColor %>">
                  <%= com.rit.placement.util.XSSUtil.escape(rec.getScore()) %>
                </div>
                <div class="score-label" style="color: <%= scoreColor %>">
                  <%= scoreLabel %>
                </div>
              </div>
            </div>

            <div class="job-details">
              <% if (rec.getJob().getPackageAmount() != null) { %>
              <div class="detail-item">
                <div class="detail-label">Package</div>
                <div class="detail-value">₹<%= com.rit.placement.util.XSSUtil.escape(rec.getJob().getPackageAmount()) %> LPA</div>
              </div>
              <% } %>
              
              <% if (rec.getJob().getMinCgpa() != null) { %>
              <div class="detail-item">
                <div class="detail-label">Min CGPA</div>
                <div class="detail-value"><%= com.rit.placement.util.XSSUtil.escape(rec.getJob().getMinCgpa()) %></div>
              </div>
              <% } %>
              
              <% if (rec.getJob().getDeadline() != null) { %>
              <div class="detail-item">
                <div class="detail-label">Deadline</div>
                <div class="detail-value"><%= com.rit.placement.util.XSSUtil.escape(dateFormat.format(rec.getJob().getDeadline())) %></div>
              </div>
              <% } %>
            </div>

            <% if (rec.getReasons() != null && !rec.getReasons().isEmpty()) { %>
            <div class="reasons-section">
              <div class="reasons-title">Why this job matches you:</div>
              <div class="reasons-list">
                <% for (String reason : rec.getReasons()) { %>
                <div class="reason-item">
                  <div class="reason-icon">✓</div>
                  <div><%= com.rit.placement.util.XSSUtil.escape(reason) %></div>
                </div>
                <% } %>
              </div>
            </div>
            <% } %>

            <form method="post" action="${pageContext.request.contextPath}/apply">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

              <input type="hidden" name="job_id" value="<%= com.rit.placement.util.XSSUtil.escape(rec.getJob().getJobId()) %>">
              <button type="submit" class="apply-button">Apply Now →</button>
            </form>
          </div>
        <% } %>
      </div>
    <% } else { %>
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3 class="empty-title">No Recommendations Available</h3>
        <p class="empty-text">
          We couldn't find any job recommendations for you at the moment.<br>
          This could be because you've already applied to all eligible jobs or there are no active postings matching your profile.
        </p>
        <a href="${pageContext.request.contextPath}/apply" class="apply-button" style="max-width: 300px; margin: 0 auto; display: block;">
          Browse All Jobs
        </a>
      </div>
    <% } %>
  </div>
</body>
</html>
