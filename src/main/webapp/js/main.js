/**
 * RIT ISE Placement Portal — Main JS
 * Handles sidebar active state and login form validation.
 */

// Highlight the active sidebar link based on current page
document.addEventListener('DOMContentLoaded', function () {
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar a').forEach(function (link) {
    if (path.indexOf(link.getAttribute('href')) !== -1) {
      link.classList.add('active');
    }
  });
});

// Basic login form validation
function validateLogin(form) {
  var usn  = form.querySelector('#usn').value.trim();
  var pass = form.querySelector('#password').value.trim();

  if (!usn) {
    alert('Please enter your USN.');
    return false;
  }
  if (!pass) {
    alert('Please enter your password.');
    return false;
  }
  return true;
}

// Toggle sidebar on mobile (called from navbar hamburger if added)
function toggleSidebar() {
  var sb = document.querySelector('.sidebar');
  if (sb) {
    sb.style.display = sb.style.display === 'block' ? 'none' : 'block';
  }
}
