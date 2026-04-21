<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rit.placement.model.Document" %>
<%
  // Session guard
  if (session.getAttribute("user_id") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }

  // Read data from request attributes (set by ProfileServlet)
  String name = (String) request.getAttribute("name");
  String usn = (String) request.getAttribute("usn");
  String branch = (String) request.getAttribute("branch");
  Integer currentSem = (Integer) request.getAttribute("currentSem");
  String skills = (String) request.getAttribute("skills");
  String projects = (String) request.getAttribute("projects");
  String experience = (String) request.getAttribute("experience");
  Document document = (Document) request.getAttribute("document");

  // Get success/error messages from session
  String successMessage = (String) session.getAttribute("successMessage");
  String errorMessage = (String) session.getAttribute("errorMessage");
  
  // Clear messages after displaying
  if (successMessage != null) {
    session.removeAttribute("successMessage");
  }
  if (errorMessage != null) {
    session.removeAttribute("errorMessage");
  }

  // Safe display with null handling
  String displayName = (name != null) ? name : "Student";
  String displayUsn = (usn != null) ? usn : "N/A";
  String displayBranch = (branch != null) ? branch : "N/A";
  String displaySem = (currentSem != null) ? String.valueOf(currentSem) : "N/A";
  String displaySkills = (skills != null) ? skills : "";
  String displayProjects = (projects != null) ? projects : "";
  String displayExperience = (experience != null) ? experience : "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Student Profile — RIT ISE Placement Portal</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
  <!-- Loading Overlay -->
  <div id="loadingOverlay" class="loading-overlay">
    <div class="loading-spinner"></div>
    <p class="loading-text">Loading profile...</p>
  </div>

  <%@ include file="/components/navbar.jsp" %>
  <%@ include file="/components/sidebar.jsp" %>

  <main class="content page-content">
    <!-- Header Section -->
    <div class="profile-header">
      <div class="profile-avatar-large">
        <%
          String[] nameParts = displayName.split(" ");
          String initials = String.valueOf(nameParts[0].charAt(0));
          if (nameParts.length > 1) initials += nameParts[nameParts.length - 1].charAt(0);
        %>
        <%=initials.toUpperCase()%>
      </div>
      <div class="profile-header-info">
        <h1 class="page-title"><%=displayName%></h1>
        <p class="page-subtitle">
          <span class="badge badge-info">STUDENT</span>
          <span class="usn-badge">USN: <%=displayUsn%></span>
          <span class="usn-badge">Branch: <%=displayBranch%></span>
          <span class="usn-badge">Semester: <%=displaySem%></span>
        </p>
      </div>
    </div>

    <!-- Success/Error Messages -->
    <% if (successMessage != null) { %>
      <div class="alert alert-success">
        <span class="alert-icon">✓</span>
        <%=successMessage%>
      </div>
    <% } %>
    
    <% if (errorMessage != null) { %>
      <div class="alert alert-error">
        <span class="alert-icon">✕</span>
        <%=errorMessage%>
      </div>
    <% } %>

    <!-- Profile Form -->
    <div class="profile-container">
      <div class="profile-card">
        <div class="profile-card-header">
          <h2 class="profile-card-title">📝 Edit Profile</h2>
          <p class="profile-card-subtitle">Update your skills, projects, and experience</p>
        </div>

        <form action="${pageContext.request.contextPath}/student/profile" method="post" 
              onsubmit="return validateProfile(this)" class="profile-form">
          
          <!-- Read-only Fields -->
          <div class="form-section">
            <h3 class="form-section-title">Personal Information</h3>
            
            <div class="form-row">
              <div class="form-group">
                <label for="name">Full Name</label>
                <input type="text" id="name" value="<%=displayName%>" readonly class="form-input-readonly">
              </div>
              
              <div class="form-group">
                <label for="usn">USN</label>
                <input type="text" id="usn" value="<%=displayUsn%>" readonly class="form-input-readonly">
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="branch">Branch</label>
                <input type="text" id="branch" value="<%=displayBranch%>" readonly class="form-input-readonly">
              </div>
              
              <div class="form-group">
                <label for="semester">Current Semester</label>
                <input type="text" id="semester" value="<%=displaySem%>" readonly class="form-input-readonly">
              </div>
            </div>
          </div>

          <!-- Editable Fields -->
          <div class="form-section">
            <h3 class="form-section-title">Professional Details</h3>
            
            <div class="form-group">
              <label for="skills">
                Skills <span class="label-hint">(e.g., Java, Python, React, SQL)</span>
              </label>
              <textarea id="skills" name="skills" rows="4" 
                        placeholder="Enter your technical skills, separated by commas..."
                        class="form-textarea"><%=displaySkills%></textarea>
              <span class="form-hint">💡 List programming languages, frameworks, and tools you know</span>
            </div>

            <div class="form-group">
              <label for="projects">
                Projects <span class="label-hint">(Academic or Personal)</span>
              </label>
              <textarea id="projects" name="projects" rows="6" 
                        placeholder="Describe your projects with technologies used..."
                        class="form-textarea"><%=displayProjects%></textarea>
              <span class="form-hint">💡 Include project name, description, and tech stack</span>
            </div>

            <div class="form-group">
              <label for="experience">
                Experience <span class="label-hint">(Internships, Work)</span>
              </label>
              <textarea id="experience" name="experience" rows="6" 
                        placeholder="Describe your work experience or internships..."
                        class="form-textarea"><%=displayExperience%></textarea>
              <span class="form-hint">💡 Include company name, role, duration, and responsibilities</span>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button type="submit" class="btn-primary">
              <span>💾</span> Save Profile
            </button>
            <button type="button" onclick="window.location.href='${pageContext.request.contextPath}/student/dashboard'" 
                    class="btn-secondary">
              <span>↩</span> Back to Dashboard
            </button>
          </div>
        </form>
      </div>

      <!-- Document Upload Card -->
      <div class="profile-card" style="margin-top: 20px;">
        <div class="profile-card-header">
          <h2 class="profile-card-title">📄 Documents</h2>
          <p class="profile-card-subtitle">Upload your resume and certificates</p>
        </div>

        <div class="documents-section">
          <!-- Resume Upload -->
          <div class="document-item">
            <div class="document-info">
              <span class="document-icon">📄</span>
              <div>
                <h4>Resume</h4>
                <% if (document != null && document.getResumePath() != null) { %>
                  <p class="document-status">✓ Uploaded</p>
                  <a href="${pageContext.request.contextPath}/<%=document.getResumePath()%>" 
                     target="_blank" class="document-link">View Resume</a>
                <% } else { %>
                  <p class="document-status-pending">⏳ Not uploaded</p>
                <% } %>
              </div>
            </div>
            <form action="${pageContext.request.contextPath}/student/upload" method="post" 
                  enctype="multipart/form-data" class="upload-form">
              <input type="hidden" name="doc_type" value="resume">
              <input type="file" name="file" accept=".pdf" required class="file-input" id="resumeFile">
              <button type="submit" class="btn-upload">Upload Resume (PDF)</button>
            </form>
          </div>

          <!-- Certificate Upload -->
          <div class="document-item">
            <div class="document-info">
              <span class="document-icon">🎓</span>
              <div>
                <h4>Certificates</h4>
                <% if (document != null && document.getCertificatesPath() != null) { %>
                  <p class="document-status">✓ Uploaded</p>
                  <a href="${pageContext.request.contextPath}/<%=document.getCertificatesPath()%>" 
                     target="_blank" class="document-link">View Certificates</a>
                <% } else { %>
                  <p class="document-status-pending">⏳ Not uploaded</p>
                <% } %>
              </div>
            </div>
            <form action="${pageContext.request.contextPath}/student/upload" method="post" 
                  enctype="multipart/form-data" class="upload-form">
              <input type="hidden" name="doc_type" value="certificate">
              <input type="file" name="file" accept=".pdf,.jpg,.jpeg,.png" required class="file-input" id="certFile">
              <button type="submit" class="btn-upload">Upload Certificate</button>
            </form>
          </div>
        </div>
      </div>

      <!-- Profile Tips Card -->
      <div class="tips-card">
        <div class="tips-header">
          <span class="tips-icon">💡</span>
          <h3>Profile Tips</h3>
        </div>
        <ul class="tips-list">
          <li>Keep your skills updated with latest technologies</li>
          <li>Include quantifiable achievements in projects</li>
          <li>Mention specific tools and frameworks used</li>
          <li>Highlight your role and contributions clearly</li>
          <li>Use action verbs to describe your experience</li>
          <li>Proofread for grammar and spelling errors</li>
        </ul>
      </div>
    </div>
  </main>

  <script src="${pageContext.request.contextPath}/js/main.js"></script>
  <script>
    // Page Load Handler with Fade-in Effect
    window.addEventListener('load', function() {
      setTimeout(function() {
        document.getElementById('loadingOverlay').classList.add('fade-out');
        document.querySelector('.page-content').classList.add('fade-in');
        
        setTimeout(function() {
          document.getElementById('loadingOverlay').style.display = 'none';
        }, 300);
      }, 400);
    });

    function validateProfile(form) {
      var skills = form.skills.value.trim();
      var projects = form.projects.value.trim();
      var experience = form.experience.value.trim();

      // At least one field must be filled
      if (!skills && !projects && !experience) {
        alert('Please fill at least one field (Skills, Projects, or Experience)');
        return false;
      }

      // Confirm before submitting
      return confirm('Are you sure you want to update your profile?');
    }

    // Auto-hide success message after 8 seconds (improved accessibility)
    window.addEventListener('DOMContentLoaded', function() {
      var successAlert = document.querySelector('.alert-success');
      if (successAlert) {
        // Add ARIA live region for screen readers
        successAlert.setAttribute('role', 'status');
        successAlert.setAttribute('aria-live', 'polite');
        
        setTimeout(function() {
          successAlert.style.opacity = '0';
          successAlert.style.transform = 'translateY(-10px)';
          setTimeout(function() {
            successAlert.style.display = 'none';
          }, 400);
        }, 8000); // Extended to 8 seconds
      }
    });
  </script>
  <style>
    .documents-section {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .document-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      background: #f9fafb;
      border-radius: 8px;
      border: 1px solid #e5e7eb;
    }
    .document-info {
      display: flex;
      align-items: center;
      gap: 15px;
    }
    .document-icon {
      font-size: 32px;
    }
    .document-status {
      color: #16a34a;
      font-weight: 500;
      margin: 5px 0;
    }
    .document-status-pending {
      color: #f59e0b;
      font-weight: 500;
      margin: 5px 0;
    }
    .document-link {
      color: #3b82f6;
      text-decoration: none;
      font-size: 14px;
    }
    .document-link:hover {
      text-decoration: underline;
    }
    .upload-form {
      display: flex;
      gap: 10px;
      align-items: center;
    }
    .file-input {
      padding: 8px;
      border: 1px solid #d1d5db;
      border-radius: 4px;
      font-size: 14px;
    }
    .btn-upload {
      padding: 10px 20px;
      background: #3b82f6;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;
      white-space: nowrap;
    }
    .btn-upload:hover {
      background: #2563eb;
    }
  </style>
</body>
</html>
