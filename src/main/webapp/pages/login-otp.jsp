<%@ page contentType="text/html;charset=UTF-8" %>
<%
  if (session.getAttribute("user_id") != null) {
    String role = (String) session.getAttribute("role");
    if ("STUDENT".equalsIgnoreCase(role)) {
      response.sendRedirect(request.getContextPath() + "/student/dashboard");
    } else if ("COORDINATOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
      response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    } else if ("PROCTOR".equalsIgnoreCase(role)) {
      response.sendRedirect(request.getContextPath() + "/proctor/dashboard");
    } else if ("COMPANY".equalsIgnoreCase(role)) {
      response.sendRedirect(request.getContextPath() + "/company/dashboard");
    }
    return;
  }
  String errorMessage   = (String) session.getAttribute("errorMessage");
  String successMessage = (String) session.getAttribute("successMessage");
  if (errorMessage   != null) session.removeAttribute("errorMessage");
  if (successMessage != null) session.removeAttribute("successMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - RIT Placement Portal</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        *, *::before, *::after { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .card {
            background: #fff;
            border-radius: 20px;
            box-shadow: 0 24px 64px rgba(0,0,0,.25);
            width: 100%;
            max-width: 460px;
            overflow: hidden;
            animation: slideUp .4s ease-out;
        }
        @keyframes slideUp {
            from { opacity:0; transform:translateY(28px); }
            to   { opacity:1; transform:translateY(0); }
        }

        .card-header {
            background: linear-gradient(135deg, #667eea, #764ba2);
            padding: 36px 32px 28px;
            text-align: center;
            color: #fff;
        }
        .card-header .icon { font-size: 44px; margin-bottom: 10px; }
        .card-header h1   { font-size: 24px; font-weight: 700; }
        .card-header p    { font-size: 13px; opacity: .85; margin-top: 4px; }

        .tabs {
            display: flex;
            border-bottom: 2px solid #e9ecef;
            background: #f8f9fa;
        }
        .tab-btn {
            flex: 1;
            padding: 14px 8px;
            font-size: 14px;
            font-weight: 600;
            color: #6c757d;
            background: none;
            border: none;
            cursor: pointer;
            transition: all .25s;
            border-bottom: 3px solid transparent;
            margin-bottom: -2px;
            font-family: inherit;
        }
        .tab-btn.active {
            color: #667eea;
            border-bottom-color: #667eea;
            background: #fff;
        }
        .tab-btn:hover:not(.active) { color: #495057; background: #edf0f7; }

        .card-body { padding: 32px; }
        .tab-pane  { display: none; }
        .tab-pane.active { display: block; }

        .alert {
            display: flex; align-items: center; gap: 10px;
            padding: 12px 16px; border-radius: 8px;
            font-size: 13px; margin-bottom: 20px;
        }
        .alert-error   { background:#fff0f0; color:#c0392b; border:1px solid #fcc; }
        .alert-success { background:#f0fff4; color:#27ae60; border:1px solid #cfc; }

        .info-box {
            background: #e8f4fd;
            border-left: 4px solid #2196f3;
            padding: 12px 14px;
            border-radius: 6px;
            margin-bottom: 22px;
            font-size: 13px;
            color: #1565c0;
        }

        .form-group { margin-bottom: 20px; }
        .form-group label {
            display: block; margin-bottom: 7px;
            font-size: 13px; font-weight: 600; color: #374151;
        }
        .form-group input {
            width: 100%; padding: 13px 15px;
            border: 2px solid #e5e7eb; border-radius: 10px;
            font-size: 15px; font-family: inherit;
            transition: border-color .2s, box-shadow .2s;
        }
        .form-group input:focus {
            outline: none; border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102,126,234,.12);
        }
        .form-hint { font-size: 12px; color: #9ca3af; margin-top: 5px; display: block; }

        .role-badges {
            display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 22px;
        }
        .role-badge {
            padding: 4px 12px; border-radius: 20px;
            font-size: 11px; font-weight: 600; letter-spacing: .4px;
        }
        .badge-admin   { background:#fef3c7; color:#92400e; }
        .badge-coord   { background:#d1fae5; color:#065f46; }
        .badge-proctor { background:#ede9fe; color:#5b21b6; }
        .badge-company { background:#dbeafe; color:#1e40af; }

        .btn {
            width: 100%; padding: 14px;
            border: none; border-radius: 10px;
            font-size: 15px; font-weight: 700; font-family: inherit;
            cursor: pointer; transition: all .25s;
        }
        .btn-primary {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: #fff;
        }
        .btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(102,126,234,.4); }
        .btn-primary:active { transform: translateY(0); }
        .btn-primary:disabled { opacity:.65; cursor:not-allowed; transform:none; }

        .card-footer {
            text-align: center; padding: 18px 32px;
            background: #f8f9fa; border-top: 1px solid #e9ecef;
            font-size: 12px; color: #9ca3af;
        }
        .card-footer a { color: #667eea; text-decoration: none; font-weight: 500; }
        .card-footer a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<div class="card">

    <div class="card-header">
        <div class="icon">🎓</div>
        <h1>RIT Placement Portal</h1>
        <p>MS Ramaiah Institute of Technology · ISE Department</p>
    </div>

    <div class="tabs">
        <button class="tab-btn active" id="tab-staff"   onclick="switchTab('staff')">🔐 Staff Login</button>
        <button class="tab-btn"        id="tab-student" onclick="switchTab('student')">🎓 Student Login</button>
    </div>

    <div class="card-body">

        <% if (errorMessage != null) { %>
        <div class="alert alert-error"><span>⚠️</span><span><%= com.rit.placement.util.XSSUtil.escape(errorMessage) %></span></div>
        <% } %>
        <% if (successMessage != null) { %>
        <div class="alert alert-success"><span>✓</span><span><%= com.rit.placement.util.XSSUtil.escape(successMessage) %></span></div>
        <% } %>

        <!-- STAFF TAB: Admin / Coordinator / Proctor / Company -->
        <div class="tab-pane active" id="pane-staff">
            <div class="role-badges">
                <span class="role-badge badge-admin">👤 Admin</span>
                <span class="role-badge badge-coord">🛡️ Coordinator</span>
                <span class="role-badge badge-proctor">📋 Proctor</span>
                <span class="role-badge badge-company">🏢 Company</span>
            </div>
            <form action="${pageContext.request.contextPath}/login" method="post" id="staffForm">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                <input type="hidden" name="loginType" value="password">
                <div class="form-group">
                    <label for="staff-usn">Username / USN</label>
                    <input type="text" id="staff-usn" name="usn"
                           placeholder="e.g. ADMIN001, COORD001"
                           required autofocus maxlength="30"
                           autocomplete="username">
                    <span class="form-hint">Use your assigned staff username</span>
                </div>
                <div class="form-group">
                    <label for="staff-pwd">Password</label>
                    <input type="password" id="staff-pwd" name="password"
                           placeholder="Enter your password"
                           required maxlength="100"
                           autocomplete="current-password">
                </div>
                <button type="submit" class="btn btn-primary" id="staffBtn">🔐 Login</button>
            </form>
        </div>

        <!-- STUDENT TAB: OTP flow -->
        <div class="tab-pane" id="pane-student">
            <div class="info-box">
                <strong>📧 How it works:</strong> Enter your USN and we'll send a 6-digit OTP to your registered email address.
            </div>
            <form action="${pageContext.request.contextPath}/otp/send" method="post" id="otpForm">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                <div class="form-group">
                    <label for="usn">University Seat Number (USN)</label>
                    <input type="text" id="usn" name="usn"
                           placeholder="e.g. 1MS24IS013"
                           required maxlength="20"
                           pattern="[A-Za-z0-9]+"
                           autocomplete="username">
                    <span class="form-hint">Enter your USN to receive OTP via email</span>
                </div>
                <button type="submit" class="btn btn-primary" id="otpBtn">📨 Send OTP</button>
            </form>
        </div>

    </div>

    <div class="card-footer">
        <p>Need help? <a href="mailto:support@msrit.edu">support@msrit.edu</a></p>
        <p style="margin-top:6px;">© 2026 RIT ISE Placement Portal</p>
    </div>
</div>

<script>
    function switchTab(tab) {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
        document.getElementById('tab-' + tab).classList.add('active');
        document.getElementById('pane-' + tab).classList.add('active');
    }

    document.getElementById('usn').addEventListener('input', function() {
        this.value = this.value.toUpperCase();
    });
    document.getElementById('staff-usn').addEventListener('input', function() {
        this.value = this.value.toUpperCase();
    });

    document.getElementById('staffForm').addEventListener('submit', function() {
        const btn = document.getElementById('staffBtn');
        btn.disabled = true; btn.textContent = '⏳ Logging in...';
    });
    document.getElementById('otpForm').addEventListener('submit', function() {
        const btn = document.getElementById('otpBtn');
        btn.disabled = true; btn.textContent = '⏳ Sending OTP...';
    });

    window.addEventListener('DOMContentLoaded', function() {
        // If redirected here after student OTP flow error, stay on student tab
        const urlParams = new URLSearchParams(window.location.search);
        if (urlParams.get('tab') === 'student') switchTab('student');

        const successAlert = document.querySelector('.alert-success');
        if (successAlert) {
            setTimeout(() => {
                successAlert.style.opacity = '0';
                setTimeout(() => successAlert.remove(), 300);
            }, 5000);
        }
    });
</script>
</body>
</html>
