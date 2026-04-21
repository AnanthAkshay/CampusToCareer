<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Notification" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/pages/login-otp.jsp");
    return;
  }

  List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
  Integer unreadCount = (Integer) request.getAttribute("unreadCount");
  
  if (unreadCount == null) unreadCount = 0;
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Notifications — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
    }
    
    .container { max-width: 900px; margin: 2rem auto; padding: 0 2rem; }
    
    .header {
      background: white;
      padding: 2rem;
      border-radius: 12px;
      margin-bottom: 2rem;
      box-shadow: 0 8px 16px rgba(0,0,0,0.1);
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .header h2 { color: #2c3e50; margin: 0; }
    
    .mark-all-btn {
      padding: 0.75rem 1.5rem;
      background: #3b82f6;
      color: white;
      border: none;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.3s;
    }
    
    .mark-all-btn:hover { background: #2563eb; }
    
    .notifications-container {
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
      overflow: hidden;
    }
    
    .notification-item {
      padding: 1.5rem;
      border-bottom: 1px solid #f3f4f6;
      transition: background 0.2s;
      cursor: pointer;
    }
    
    .notification-item:last-child { border-bottom: none; }
    
    .notification-item:hover { background: #f9fafb; }
    
    .notification-item.unread {
      background: #eff6ff;
      border-left: 4px solid #3b82f6;
    }
    
    .notification-item.unread:hover { background: #dbeafe; }
    
    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 0.5rem;
    }
    
    .notification-title {
      font-size: 1.1rem;
      font-weight: 700;
      color: #2c3e50;
    }
    
    .notification-type {
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }
    
    .type-application { background: #dbeafe; color: #1e40af; }
    .type-status_update { background: #fef3c7; color: #92400e; }
    .type-interview { background: #d1fae5; color: #065f46; }
    .type-job_posted { background: #e0e7ff; color: #4338ca; }
    
    .notification-message {
      color: #6b7280;
      line-height: 1.6;
      margin-bottom: 0.75rem;
    }
    
    .notification-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .notification-time {
      color: #9ca3af;
      font-size: 0.85rem;
    }
    
    .mark-read-btn {
      padding: 0.5rem 1rem;
      background: #f3f4f6;
      color: #6b7280;
      border: none;
      border-radius: 6px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }
    
    .mark-read-btn:hover {
      background: #e5e7eb;
      color: #374151;
    }
    
    .empty-state {
      padding: 4rem 2rem;
      text-align: center;
    }
    
    .empty-icon { font-size: 4rem; margin-bottom: 1rem; }
    .empty-title { font-size: 1.5rem; color: #2c3e50; margin-bottom: 0.5rem; }
    .empty-text { color: #6b7280; }
  </style>
</head>
<body>
  <%@ include file="/components/navbar.jsp" %>

  <div class="container">
    <div class="header">
      <div>
        <h2>🔔 Notifications</h2>
        <% if (unreadCount > 0) { %>
          <p style="color: #6b7280; margin-top: 0.5rem;"><%= unreadCount %> unread notification<%= unreadCount > 1 ? "s" : "" %></p>
        <% } %>
      </div>
      <% if (notifications != null && !notifications.isEmpty()) { %>
        <button class="mark-all-btn" onclick="markAllAsRead()">Mark All as Read</button>
      <% } %>
    </div>

    <div class="notifications-container">
      <% if (notifications != null && !notifications.isEmpty()) { %>
        <% for (Notification notif : notifications) { %>
          <div class="notification-item <%= notif.isRead() ? "" : "unread" %>" id="notif-<%= notif.getNotificationId() %>">
            <div class="notification-header">
              <div class="notification-title"><%= notif.getTitle() %></div>
              <span class="notification-type type-<%= notif.getType().toLowerCase() %>">
                <%= notif.getType().replace("_", " ") %>
              </span>
            </div>
            
            <div class="notification-message"><%= notif.getMessage() %></div>
            
            <div class="notification-footer">
              <div class="notification-time">
                <%= dateFormat.format(notif.getCreatedAt()) %>
              </div>
              <% if (!notif.isRead()) { %>
                <button class="mark-read-btn" onclick="markAsRead(<%= notif.getNotificationId() %>)">
                  Mark as Read
                </button>
              <% } %>
            </div>
          </div>
        <% } %>
      <% } else { %>
        <div class="empty-state">
          <div class="empty-icon">🔔</div>
          <h3 class="empty-title">No Notifications</h3>
          <p class="empty-text">You're all caught up! Check back later for updates.</p>
        </div>
      <% } %>
    </div>
  </div>

  <script>
    function markAsRead(notificationId) {
      fetch('${pageContext.request.contextPath}/api/notifications?action=markRead&id=' + notificationId, {
        method: 'POST'
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          // Remove unread styling
          const item = document.getElementById('notif-' + notificationId);
          if (item) {
            item.classList.remove('unread');
            const btn = item.querySelector('.mark-read-btn');
            if (btn) btn.remove();
          }
          // Reload page to update count
          setTimeout(() => location.reload(), 500);
        }
      })
      .catch(error => console.error('Error:', error));
    }
    
    function markAllAsRead() {
      fetch('${pageContext.request.contextPath}/api/notifications?action=markRead', {
        method: 'POST'
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          location.reload();
        }
      })
      .catch(error => console.error('Error:', error));
    }
  </script>
</body>
</html>
