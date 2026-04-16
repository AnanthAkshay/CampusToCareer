<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login — RIT ISE Placement Portal</title>
  <meta name="description" content="Login to the RIT ISE Placement and Academic Tracking System">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <div class="login-wrapper">
    <div class="login-card">
      <h1>RIT ISE Placement Portal</h1>
      <p>Sign in with your USN and password</p>

      <%-- Show error message if redirected with ?error=1 --%>
      <% if (request.getParameter("error") != null) { %>
        <div class="alert-error">Invalid USN or password. Please try again.</div>
      <% } %>

      <form action="${pageContext.request.contextPath}/login" method="post"
            onsubmit="return validateLogin(this)">
        <div class="form-group">
          <label for="usn">USN</label>
          <input type="text" id="usn" name="usn" placeholder="e.g. 1RV21IS001" required>
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" name="password" placeholder="Enter password" required>
        </div>
        <button type="submit" class="btn-primary">Sign In</button>
      </form>
    </div>
  </div>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
