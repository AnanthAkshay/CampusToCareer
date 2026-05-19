<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Internal Server Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            text-align: center;
            padding: 20px;
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
        }
        .error-code {
            font-size: 120px;
            font-weight: bold;
            margin: 0;
            line-height: 1;
        }
        .error-title {
            font-size: 32px;
            margin: 20px 0 10px;
        }
        .error-message {
            font-size: 18px;
            margin: 10px 0 30px;
            opacity: 0.9;
        }
        .error-actions {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
            justify-content: center;
        }
        .btn {
            padding: 12px 24px;
            border-radius: 6px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s;
        }
        .btn-primary {
            background: white;
            color: #f5576c;
        }
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        }
        .btn-secondary {
            background: rgba(255,255,255,0.2);
            color: white;
            border: 2px solid white;
        }
        .btn-secondary:hover {
            background: rgba(255,255,255,0.3);
        }
        .error-details {
            margin-top: 20px;
            padding: 15px;
            background: rgba(0,0,0,0.2);
            border-radius: 8px;
            max-width: 600px;
            text-align: left;
            font-family: monospace;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">500</div>
        <h1 class="error-title">Internal Server Error</h1>
        <p class="error-message">
            Oops! Something went wrong on our end. We're working to fix it.
        </p>
        <div class="error-actions">
            <a href="javascript:history.back()" class="btn btn-secondary">← Go Back</a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Go to Dashboard</a>
        </div>
        <% if (exception != null && request.getParameter("debug") != null) { %>
            <div class="error-details">
                <strong>Error:</strong> <%= com.rit.placement.util.XSSUtil.escape(exception.getMessage()) %>
            </div>
        <% } %>
    </div>
</body>
</html>
