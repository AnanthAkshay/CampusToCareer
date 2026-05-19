<%@ page contentType="text/html;charset=UTF-8" %>
<%
  // Check if OTP session exists
  if (session.getAttribute("otp") == null) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  String errorMessage = (String) session.getAttribute("errorMessage");
  String successMessage = (String) session.getAttribute("successMessage");
  String maskedEmail = (String) session.getAttribute("otp_email");
  Long otpTimestamp = (Long) session.getAttribute("otp_timestamp");
  
  if (errorMessage != null) session.removeAttribute("errorMessage");
  if (successMessage != null) session.removeAttribute("successMessage");

  // Calculate remaining time
  long currentTime = System.currentTimeMillis();
  long otpAge = (currentTime - (otpTimestamp != null ? otpTimestamp : currentTime)) / 1000;
  long remainingSeconds = (5 * 60) - otpAge;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify OTP - RIT Placement Portal</title>
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

        .verify-container {
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

        .verify-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 40px 30px;
            text-align: center;
            color: white;
        }

        .verify-header h1 {
            font-size: 28px;
            margin-bottom: 10px;
        }

        .verify-header p {
            font-size: 14px;
            opacity: 0.9;
        }

        .verify-body {
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

        .email-info {
            background-color: #f8f9fa;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 25px;
            text-align: center;
        }

        .email-info p {
            font-size: 13px;
            color: #666;
            margin-bottom: 5px;
        }

        .email-info strong {
            color: #667eea;
            font-size: 15px;
        }

        .timer-box {
            background: linear-gradient(135deg, #fff3cd 0%, #ffe69c 100%);
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 25px;
            text-align: center;
        }

        .timer-box p {
            font-size: 13px;
            color: #856404;
            margin-bottom: 8px;
        }

        .timer {
            font-size: 24px;
            font-weight: bold;
            color: #856404;
            font-family: 'Courier New', monospace;
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

        .otp-input-container {
            display: flex;
            gap: 10px;
            justify-content: center;
            margin-bottom: 10px;
        }

        .otp-input {
            width: 50px;
            height: 60px;
            text-align: center;
            font-size: 24px;
            font-weight: bold;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            transition: all 0.3s;
        }

        .otp-input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .form-hint {
            font-size: 12px;
            color: #666;
            text-align: center;
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
            margin-bottom: 15px;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
        }

        .btn-secondary {
            width: 100%;
            padding: 14px;
            background: white;
            color: #667eea;
            border: 2px solid #667eea;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s;
        }

        .btn-secondary:hover {
            background: #f8f9fa;
        }

        .verify-footer {
            text-align: center;
            padding: 20px 30px;
            background-color: #f8f9fa;
            border-top: 1px solid #e9ecef;
        }

        .verify-footer p {
            font-size: 13px;
            color: #666;
        }

        .icon {
            font-size: 48px;
            margin-bottom: 10px;
        }

        .expired {
            background: linear-gradient(135deg, #fee 0%, #fcc 100%);
        }
    </style>
</head>
<body>
    <div class="verify-container">
        <div class="verify-header">
            <div class="icon">🔐</div>
            <h1>Verify OTP</h1>
            <p>Enter the code sent to your email</p>
        </div>

        <div class="verify-body">
            <% if (errorMessage != null) { %>
                <div class="alert alert-error">
                    <span>⚠️</span>
                    <span><%= com.rit.placement.util.XSSUtil.escape(errorMessage) %></span>
                </div>
            <% } %>

            <% if (successMessage != null) { %>
                <div class="alert alert-success">
                    <span>✓</span>
                    <span><%= com.rit.placement.util.XSSUtil.escape(successMessage) %></span>
                </div>
            <% } %>

            <div class="email-info">
                <p>OTP sent to</p>
                <strong><%= com.rit.placement.util.XSSUtil.escape(maskedEmail != null ? maskedEmail : "your email") %></strong>
            </div>

            <div class="timer-box" id="timerBox">
                <p>⏰ OTP expires in</p>
                <div class="timer" id="timer"><%= com.rit.placement.util.XSSUtil.escape(String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)) %></div>
            </div>

            <form action="${pageContext.request.contextPath}/otp/verify" method="post" id="verifyForm">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                <div class="form-group">
                    <label>Enter 6-Digit OTP</label>
                    <div class="otp-input-container">
                        <input type="text" class="otp-input" maxlength="1" pattern="[0-9]" required>
                        <input type="text" class="otp-input" maxlength="1" pattern="[0-9]" required>
                        <input type="text" class="otp-input" maxlength="1" pattern="[0-9]" required>
                        <input type="text" class="otp-input" maxlength="1" pattern="[0-9]" required>
                        <input type="text" class="otp-input" maxlength="1" pattern="[0-9]" required>
                        <input type="text" class="otp-input" maxlength="1" pattern="[0-9]" required>
                    </div>
                    <input type="hidden" name="otp" id="otpValue">
                    <span class="form-hint">Check your email inbox and spam folder</span>
                </div>

                <button type="submit" class="btn-primary" id="verifyBtn">
                    ✓ Verify & Login
                </button>
            </form>

            <form action="${pageContext.request.contextPath}/otp/send" method="post">
    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">

                <input type="hidden" name="usn" value="<%= com.rit.placement.util.XSSUtil.escape(session.getAttribute("otp_usn")) %>">
                <button type="submit" class="btn-secondary">
                    🔄 Resend OTP
                </button>
            </form>
        </div>

        <div class="verify-footer">
            <p>Didn't receive the code? Check spam folder or click Resend OTP</p>
        </div>
    </div>

    <script>
        // OTP Input Auto-focus and Auto-submit
        const otpInputs = document.querySelectorAll('.otp-input');
        const otpValue = document.getElementById('otpValue');
        const verifyForm = document.getElementById('verifyForm');

        otpInputs.forEach((input, index) => {
            // Auto-focus next input
            input.addEventListener('input', function(e) {
                if (this.value.length === 1) {
                    if (index < otpInputs.length - 1) {
                        otpInputs[index + 1].focus();
                    } else {
                        // All inputs filled, combine and submit
                        combineOTP();
                    }
                }
            });

            // Handle backspace
            input.addEventListener('keydown', function(e) {
                if (e.key === 'Backspace' && this.value === '' && index > 0) {
                    otpInputs[index - 1].focus();
                }
            });

            // Only allow numbers
            input.addEventListener('keypress', function(e) {
                if (!/[0-9]/.test(e.key)) {
                    e.preventDefault();
                }
            });

            // Handle paste
            input.addEventListener('paste', function(e) {
                e.preventDefault();
                const pastedData = e.clipboardData.getData('text').trim();
                if (/^\d{6}$/.test(pastedData)) {
                    pastedData.split('').forEach((char, i) => {
                        if (otpInputs[i]) {
                            otpInputs[i].value = char;
                        }
                    });
                    otpInputs[5].focus();
                    combineOTP();
                }
            });
        });

        function combineOTP() {
            let otp = '';
            otpInputs.forEach(input => {
                otp += input.value;
            });
            otpValue.value = otp;
        }

        // Form submission
        verifyForm.addEventListener('submit', function(e) {
            combineOTP();
            
            if (otpValue.value.length !== 6) {
                e.preventDefault();
                alert('Please enter all 6 digits');
                return;
            }

            // Disable button
            const verifyBtn = document.getElementById('verifyBtn');
            verifyBtn.disabled = true;
            verifyBtn.textContent = '⏳ Verifying...';
        });

        // Countdown timer
        let remainingSeconds = <%= com.rit.placement.util.XSSUtil.escape(remainingSeconds) %>;
        const timerElement = document.getElementById('timer');
        const timerBox = document.getElementById('timerBox');

        const countdown = setInterval(function() {
            remainingSeconds--;
            
            if (remainingSeconds <= 0) {
                clearInterval(countdown);
                timerBox.classList.add('expired');
                timerElement.textContent = 'EXPIRED';
                timerElement.style.color = '#c33';
                
                // Disable form
                otpInputs.forEach(input => input.disabled = true);
                document.getElementById('verifyBtn').disabled = true;
                document.getElementById('verifyBtn').textContent = '⏰ OTP Expired';
                
                alert('OTP has expired. Please request a new OTP.');
            } else {
                const minutes = Math.floor(remainingSeconds / 60);
                const seconds = remainingSeconds % 60;
                timerElement.textContent = String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
                
                // Warning when less than 1 minute
                if (remainingSeconds <= 60) {
                    timerBox.style.background = 'linear-gradient(135deg, #fee 0%, #fcc 100%)';
                    timerElement.style.color = '#c33';
                }
            }
        }, 1000);

        // Auto-focus first input
        otpInputs[0].focus();
    </script>
</body>
</html>
