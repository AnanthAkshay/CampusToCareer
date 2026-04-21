<%@ page contentType="text/html;charset=UTF-8" %>
<%
  // If user is already logged in, redirect to dashboard
  if (session.getAttribute("user_id") != null) {
    String role = (String) session.getAttribute("role");
    if ("STUDENT".equalsIgnoreCase(role)) {
      response.sendRedirect(request.getContextPath() + "/student/dashboard");
    } else if ("COORDINATOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
      response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }
    return;
  }

  String errorMessage = (String) session.getAttribute("errorMessage");
  String successMessage = (String) session.getAttribute("successMessage");
  
  if (errorMessage != null) session.removeAttribute("errorMessage");
  if (successMessage != null) session.removeAttribute("successMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login with OTP - RIT Placement Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .login-container {
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
            max-width: 450px;
            width: 100%;
            animation: slideUp 0.5s ease-out;
        }

        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .login-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 40px 30px;
            text-align: center;
            color: white;
        }

        .login-header h1 {
            font-size: 28px;
            margin-bottom: 10px;
        }

        .login-header p {
            font-size: 14px;
            opacity: 0.9;
        }

        .login-body {
            padding: 40px 30px;
        }

        .alert {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .alert-error {
            background-color: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }

        .alert-success {
            background-color: #efe;
            color: #3c3;
            border: 1px solid #cfc;
        }

        .form-group {
            margin-bottom: 25px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
            font-size: 14px;
        }

        .form-group input {
            width: 100%;
            padding: 14px 16px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .form-hint {
            font-size: 12px;
            color: #666;
            margin-top: 6px;
            display: block;
        }

        .btn-primary {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }

        .btn-primary:active {
            transform: translateY(0);
        }

        .login-footer {
            text-align: center;
            padding: 20px 30px;
            background-color: #f8f9fa;
            border-top: 1px solid #e9ecef;
        }

        .login-footer p {
            font-size: 13px;
            color: #666;
        }

        .login-footer a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }

        .login-footer a:hover {
            text-decoration: underline;
        }

        .info-box {
            background-color: #e3f2fd;
            border-left: 4px solid #2196f3;
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 4px;
        }

        .info-box p {
            font-size: 13px;
            color: #1565c0;
            margin: 0;
        }

        .icon {
            font-size: 48px;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <div class="icon">🎓</div>
            <h1>RIT Placement Portal</h1>
            <p>Login with OTP</p>
        </div>

        <div class="login-body">
            <% if (errorMessage != null) { %>
                <div class="alert alert-error">
                    <span>⚠️</span>
                    <span><%=errorMessage%></span>
                </div>
            <% } %>

            <% if (successMessage != null) { %>
                <div class="alert alert-success">
                    <span>✓</span>
                    <span><%=successMessage%></span>
                </div>
            <% } %>

            <div class="info-box">
                <p><strong>📧 How it works:</strong> Enter your USN and we'll send a 6-digit OTP to your registered email address.</p>
            </div>

            <form action="${pageContext.request.contextPath}/otp/send" method="post" id="otpForm">
                <div class="form-group">
                    <label for="usn">University Seat Number (USN)</label>
                    <input 
                        type="text" 
                        id="usn" 
                        name="usn" 
                        placeholder="Enter your USN (e.g., 1RV21IS001)" 
                        required 
                        autofocus
                        pattern="[A-Za-z0-9]+"
                        maxlength="20"
                    >
                    <span class="form-hint">Enter your USN to receive OTP via email</span>
                </div>

                <button type="submit" class="btn-primary" id="submitBtn">
                    📨 Send OTP
                </button>
            </form>
        </div>

        <div class="login-footer">
            <p>Need help? Contact <a href="mailto:support@rit.edu">support@rit.edu</a></p>
            <p style="margin-top: 10px; font-size: 12px; color: #999;">
                © 2026 RIT ISE Placement Portal
            </p>
        </div>
    </div>

    <script>
        // Form validation and UX enhancements
        document.getElementById('otpForm').addEventListener('submit', function(e) {
            const usn = document.getElementById('usn').value.trim();
            
            if (!usn) {
                e.preventDefault();
                alert('Please enter your USN');
                return;
            }

            // Disable button to prevent double submission
            const submitBtn = document.getElementById('submitBtn');
            submitBtn.disabled = true;
            submitBtn.textContent = '⏳ Sending OTP...';
        });

        // Auto-uppercase USN input
        document.getElementById('usn').addEventListener('input', function(e) {
            this.value = this.value.toUpperCase();
        });

        // Auto-hide success message after 5 seconds
        window.addEventListener('DOMContentLoaded', function() {
            const successAlert = document.querySelector('.alert-success');
            if (successAlert) {
                setTimeout(function() {
                    successAlert.style.opacity = '0';
                    successAlert.style.transform = 'translateY(-10px)';
                    setTimeout(function() {
                        successAlert.style.display = 'none';
                    }, 300);
                }, 5000);
            }
        });
    </script>
</body>
</html>
