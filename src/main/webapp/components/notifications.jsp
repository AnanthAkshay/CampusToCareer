<%-- Notification Bell Component --%>
<div class="notification-bell" id="notificationBell">
  <button class="bell-button" onclick="toggleNotifications()" aria-label="Notifications">
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
      <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
    </svg>
    <span class="notification-badge" id="notificationBadge" style="display: none;">0</span>
  </button>
  
  <div class="notification-dropdown" id="notificationDropdown" style="display: none;">
    <div class="notification-header">
      <h3>Notifications</h3>
      <button class="mark-all-read" onclick="markAllAsRead()">Mark all as read</button>
    </div>
    
    <div class="notification-list" id="notificationList">
      <div class="notification-loading">Loading...</div>
    </div>
    
    <div class="notification-footer">
      <a href="${pageContext.request.contextPath}/notifications">View All</a>
    </div>
  </div>
</div>

<style>
  .notification-bell {
    position: relative;
    display: inline-block;
  }
  
  .bell-button {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 50%;
    transition: background 0.3s;
    position: relative;
    color: white;
  }
  
  .bell-button:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  
  .notification-badge {
    position: absolute;
    top: 0;
    right: 0;
    background: #ef4444;
    color: white;
    border-radius: 10px;
    padding: 0.125rem 0.375rem;
    font-size: 0.7rem;
    font-weight: 700;
    min-width: 18px;
    text-align: center;
  }
  
  .notification-dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 0.5rem;
    width: 380px;
    max-width: 90vw;
    background: white;
    border-radius: 12px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
    z-index: 1000;
    max-height: 500px;
    display: flex;
    flex-direction: column;
  }
  
  .notification-header {
    padding: 1rem;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .notification-header h3 {
    margin: 0;
    font-size: 1.1rem;
    color: #2c3e50;
  }
  
  .mark-all-read {
    background: none;
    border: none;
    color: #3b82f6;
    font-size: 0.85rem;
    cursor: pointer;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    transition: background 0.2s;
  }
  
  .mark-all-read:hover {
    background: #eff6ff;
  }
  
  .notification-list {
    overflow-y: auto;
    max-height: 400px;
  }
  
  .notification-item {
    padding: 1rem;
    border-bottom: 1px solid #f3f4f6;
    cursor: pointer;
    transition: background 0.2s;
  }
  
  .notification-item:hover {
    background: #f9fafb;
  }
  
  .notification-item.unread {
    background: #eff6ff;
  }
  
  .notification-item.unread:hover {
    background: #dbeafe;
  }
  
  .notification-title {
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 0.25rem;
    font-size: 0.9rem;
  }
  
  .notification-message {
    color: #6b7280;
    font-size: 0.85rem;
    line-height: 1.4;
    margin-bottom: 0.5rem;
  }
  
  .notification-time {
    color: #9ca3af;
    font-size: 0.75rem;
  }
  
  .notification-loading,
  .notification-empty {
    padding: 2rem;
    text-align: center;
    color: #9ca3af;
  }
  
  .notification-footer {
    padding: 0.75rem;
    border-top: 1px solid #e5e7eb;
    text-align: center;
  }
  
  .notification-footer a {
    color: #3b82f6;
    text-decoration: none;
    font-size: 0.9rem;
    font-weight: 600;
  }
  
  .notification-footer a:hover {
    text-decoration: underline;
  }
</style>

<script>
  let notificationDropdownOpen = false;
  
  // Load notifications on page load
  document.addEventListener('DOMContentLoaded', function() {
    loadNotificationCount();
    // Refresh count every 30 seconds
    setInterval(loadNotificationCount, 30000);
  });
  
  function loadNotificationCount() {
    fetch('${pageContext.request.contextPath}/api/notifications?action=count')
      .then(response => response.json())
      .then(data => {
        const badge = document.getElementById('notificationBadge');
        if (data.unreadCount > 0) {
          badge.textContent = data.unreadCount > 99 ? '99+' : data.unreadCount;
          badge.style.display = 'block';
        } else {
          badge.style.display = 'none';
        }
      })
      .catch(error => console.error('Error loading notification count:', error));
  }
  
  function toggleNotifications() {
    const dropdown = document.getElementById('notificationDropdown');
    notificationDropdownOpen = !notificationDropdownOpen;
    
    if (notificationDropdownOpen) {
      dropdown.style.display = 'block';
      loadNotifications();
    } else {
      dropdown.style.display = 'none';
    }
  }
  
  function loadNotifications() {
    const list = document.getElementById('notificationList');
    list.innerHTML = '<div class="notification-loading">Loading...</div>';
    
    fetch('${pageContext.request.contextPath}/api/notifications')
      .then(response => response.json())
      .then(data => {
        if (data.notifications && data.notifications.length > 0) {
          list.innerHTML = '';
          data.notifications.forEach(notif => {
            const item = createNotificationItem(notif);
            list.appendChild(item);
          });
        } else {
          list.innerHTML = '<div class="notification-empty">No notifications yet</div>';
        }
      })
      .catch(error => {
        console.error('Error loading notifications:', error);
        list.innerHTML = '<div class="notification-empty">Failed to load notifications</div>';
      });
  }
  
  function createNotificationItem(notif) {
    const div = document.createElement('div');
    div.className = 'notification-item' + (notif.isRead ? '' : ' unread');
    div.onclick = () => markAsRead(notif.id);
    
    div.innerHTML = `
      <div class="notification-title">\${notif.title}</div>
      <div class="notification-message">\${notif.message}</div>
      <div class="notification-time">\${formatTime(notif.createdAt)}</div>
    `;
    
    return div;
  }
  
  function markAsRead(notificationId) {
    fetch('${pageContext.request.contextPath}/api/notifications?action=markRead&id=' + notificationId, {
      method: 'POST'
    })
    .then(() => {
      loadNotificationCount();
      loadNotifications();
    })
    .catch(error => console.error('Error marking as read:', error));
  }
  
  function markAllAsRead() {
    fetch('${pageContext.request.contextPath}/api/notifications?action=markRead', {
      method: 'POST'
    })
    .then(() => {
      loadNotificationCount();
      loadNotifications();
    })
    .catch(error => console.error('Error marking all as read:', error));
  }
  
  function formatTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return minutes + ' min ago';
    if (hours < 24) return hours + ' hour' + (hours > 1 ? 's' : '') + ' ago';
    if (days < 7) return days + ' day' + (days > 1 ? 's' : '') + ' ago';
    
    return date.toLocaleDateString();
  }
  
  // Close dropdown when clicking outside
  document.addEventListener('click', function(event) {
    const bell = document.getElementById('notificationBell');
    if (bell && !bell.contains(event.target) && notificationDropdownOpen) {
      toggleNotifications();
    }
  });
</script>
