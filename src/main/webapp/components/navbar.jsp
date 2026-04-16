<%-- Top navigation bar — include via <%@ include file="/components/navbar.jsp" %> --%>
<%
  // Read session attributes (set by LoginServlet)
  String navName = (String) session.getAttribute("name");
  String navRole = (String) session.getAttribute("role");
  if (navName == null) navName = "Guest";
  if (navRole == null) navRole = "";
  // Build initials for avatar (first letter of first & last name)
  String[] parts = navName.split(" ");
  String initials = String.valueOf(parts[0].charAt(0));
  if (parts.length > 1) initials += parts[parts.length - 1].charAt(0);
  
  // Determine dashboard path based on role
  String navDashboardPath = "/student/dashboard";
  if (navRole != null) {
    switch (navRole.trim().toUpperCase()) {
      case "PROCTOR":
        navDashboardPath = "/proctor/dashboard";
        break;
      case "FACULTY":
      case "COORDINATOR":
      case "ADMIN":
        navDashboardPath = "/admin/dashboard";
        break;
      default:
        navDashboardPath = "/student/dashboard";
        break;
    }
  }
%>
<nav class="navbar">
  <div class="brand">
    <a href="${pageContext.request.contextPath}<%=navDashboardPath%>" class="brand-link">
      RIT ISE Placement Portal
    </a>
  </div>
  
  <div class="navbar-menu">
    <a href="${pageContext.request.contextPath}<%=navDashboardPath%>" class="nav-link">
      <span>🏠</span> Dashboard
    </a>
    <% if ("STUDENT".equalsIgnoreCase(navRole)) { %>
      <a href="${pageContext.request.contextPath}/student/profile" class="nav-link">
        <span>👤</span> Profile
      </a>
    <% } %>
    <a href="${pageContext.request.contextPath}/logout" class="nav-link">
      <span>🚪</span> Logout
    </a>
  </div>
  
  <div class="user-info">
    <div class="user-details">
      <span class="user-name"><%=navName%></span>
      <span class="user-role"><%=navRole%></span>
    </div>
    <div class="user-avatar"><%=initials.toUpperCase()%></div>
  </div>
</nav>
