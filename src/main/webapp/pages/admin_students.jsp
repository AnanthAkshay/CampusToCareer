<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.controller.AdminStudentsServlet.StudentDetails" %>
<%@ page import="java.util.List" %>
<%
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }
  
  @SuppressWarnings("unchecked")
  List<StudentDetails> students = (List<StudentDetails>) request.getAttribute("students");
  Integer totalStudents = (Integer) request.getAttribute("totalStudents");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Manage Students — Admin Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Manage Students 👥</h1>
        <p class="page-subtitle">Total: <%=totalStudents != null ? totalStudents : 0%> students</p>
      </div>
    </div>

    <% if (students != null && !students.isEmpty()) { %>
      <div class="table-wrap">
        <table class="students-table">
          <thead>
            <tr>
              <th>USN</th>
              <th>Name</th>
              <th>Branch</th>
              <th>Semester</th>
              <th>CGPA</th>
              <th>Skills</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <% for (StudentDetails student : students) { %>
              <tr>
                <td><%=student.getUsn()%></td>
                <td><%=student.getName()%></td>
                <td><%=student.getBranch()%></td>
                <td><%=student.getCurrentSem()%></td>
                <td><span class="cgpa-badge"><%=String.format("%.2f", student.getCgpa())%></span></td>
                <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis;"><%=student.getSkills()%></td>
                <td>
                  <% if (student.isActive()) { %>
                    <span class="badge badge-success">Active</span>
                  <% } else { %>
                    <span class="badge badge-danger">Inactive</span>
                  <% } %>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    <% } else { %>
      <div class="empty-state">
        <div class="empty-state-icon">👥</div>
        <h4 class="empty-state-title">No Students Found</h4>
      </div>
    <% } %>
  </main>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
