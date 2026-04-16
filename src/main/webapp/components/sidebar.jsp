<%-- Left sidebar navigation — include via <%@ include file="/components/sidebar.jsp" %> --%>
<%
  String sidebarRole = (String) session.getAttribute("role");
  String currentPath = request.getServletPath();
  String dashboardPath = "/student/dashboard";
  String profilePath = "/student/profile";
  
  if (sidebarRole != null) {
    switch (sidebarRole.trim().toUpperCase()) {
      case "PROCTOR":
        dashboardPath = "/proctor/dashboard";
        break;
      case "FACULTY":
      case "COORDINATOR":
      case "ADMIN":
        dashboardPath = "/admin/dashboard";
        break;
      default:
        dashboardPath = "/student/dashboard";
        break;
    }
  }
  
  // Helper method to check if link is active
  boolean isDashboardActive = currentPath != null && currentPath.contains("dashboard");
  boolean isProfileActive = currentPath != null && currentPath.contains("profile");
%>
<aside class="sidebar">
  <div class="section-label">Main</div>
  <a href="${pageContext.request.contextPath}<%=dashboardPath%>" 
     class="<%=isDashboardActive ? "active" : ""%>">
    &#x1F4CA; Dashboard
  </a>
  
  <% if ("STUDENT".equalsIgnoreCase(sidebarRole)) { %>
    <a href="${pageContext.request.contextPath}<%=profilePath%>" 
       class="<%=isProfileActive ? "active" : ""%>">
      &#x1F464; Profile
    </a>
  <% } %>
  
  <a href="#">&#x1F393; Academics</a>
  
  <% if ("STUDENT".equalsIgnoreCase(sidebarRole)) { %>
    <a href="${pageContext.request.contextPath}/my-applications">&#x1F4CB; My Applications</a>
  <% } else { %>
    <a href="#">&#x1F4C4; Applications</a>
  <% } %>

  <div class="section-label">Placements</div>
  <% if ("STUDENT".equalsIgnoreCase(sidebarRole)) { %>
    <a href="${pageContext.request.contextPath}/apply">&#x1F4BC; Apply for Jobs</a>
  <% } %>
  <a href="${pageContext.request.contextPath}/companies">&#x1F3E2; Companies</a>
  <a href="${pageContext.request.contextPath}/job-postings">&#x1F4BC; Job Postings</a>

  <div class="section-label">Account</div>
  <a href="#">&#x1F4C1; Documents</a>
  <a href="#">&#x1F514; Notifications</a>
  <a href="${pageContext.request.contextPath}/logout">&#x1F6AA; Logout</a>
</aside>
