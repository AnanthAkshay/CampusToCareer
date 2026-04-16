# 🚀 DEPLOYMENT GUIDE
## RIT ISE Placement & Academic Tracking System

**Version:** 1.0.0 - Production Ready  
**Status:** ✅ Ready for Deployment

---

## 📋 PRE-REQUISITES

### Software Requirements
- ✅ Java 17 or higher
- ✅ Apache Tomcat 10.x
- ✅ MySQL 8.0 or higher
- ✅ Maven 3.8 or higher

### System Requirements
- **RAM:** Minimum 2GB (4GB recommended)
- **Disk Space:** 500MB for application + database
- **Network:** Port 8080 (Tomcat), Port 3306 (MySQL)

---

## 🗄️ STEP 1: DATABASE SETUP

### 1.1 Create Database
```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS rit_placement;
CREATE USER 'rit_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON rit_placement.* TO 'rit_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 1.2 Run Schema
```bash
mysql -u rit_user -p rit_placement < sql/schema.sql
```

### 1.3 Verify Tables
```bash
mysql -u rit_user -p rit_placement -e "SHOW TABLES;"
```

**Expected Output:**
```
+---------------------------+
| Tables_in_rit_placement   |
+---------------------------+
| academic_records          |
| applications              |
| companies                 |
| documents                 |
| job_postings              |
| notifications             |
| proctor_student_map       |
| proctors                  |
| remarks                   |
| students                  |
| users                     |
+---------------------------+
```

### 1.4 Verify Critical Columns
```bash
# Check companies table
mysql -u rit_user -p rit_placement -e "DESCRIBE companies;"

# Check job_postings table
mysql -u rit_user -p rit_placement -e "DESCRIBE job_postings;"

# Check applications table
mysql -u rit_user -p rit_placement -e "DESCRIBE applications;"
```

**Verify these columns exist:**
- `companies.company_type` ✅
- `companies.created_at` ✅
- `job_postings.allowed_branches` ✅
- `job_postings.deadline` ✅
- `job_postings.created_at` ✅
- `applications.applied_at` ✅

---

## ⚙️ STEP 2: APPLICATION CONFIGURATION

### 2.1 Configure Database Connection
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/rit_placement?useSSL=false&serverTimezone=UTC
db.username=rit_user
db.password=your_secure_password
db.driver=com.mysql.cj.jdbc.Driver
```

### 2.2 Verify Configuration
```bash
# Test database connection
mysql -u rit_user -p rit_placement -e "SELECT 1;"
```

---

## 🔨 STEP 3: BUILD APPLICATION

### 3.1 Clean Previous Builds
```bash
mvn clean
```

### 3.2 Compile and Package
```bash
mvn package
```

### 3.3 Verify WAR File
```bash
ls -lh target/rit-placement.war
```

**Expected:** WAR file should be created (approximately 10-20 MB)

---

## 🚀 STEP 4: DEPLOY TO TOMCAT

### 4.1 Stop Tomcat (if running)
```bash
$TOMCAT_HOME/bin/shutdown.sh
```

### 4.2 Deploy WAR File
```bash
# Remove old deployment (if exists)
rm -rf $TOMCAT_HOME/webapps/rit-placement*

# Copy new WAR file
cp target/rit-placement.war $TOMCAT_HOME/webapps/
```

### 4.3 Start Tomcat
```bash
$TOMCAT_HOME/bin/startup.sh
```

### 4.4 Monitor Deployment
```bash
tail -f $TOMCAT_HOME/logs/catalina.out
```

**Wait for:** "Deployment of web application archive ... has finished"

---

## 🧪 STEP 5: SMOKE TESTING

### 5.1 Test Application Access
```bash
curl http://localhost:8080/rit-placement/
```

**Expected:** Redirect to login page (HTTP 302)

### 5.2 Test Login Page
Open browser: `http://localhost:8080/rit-placement/login`

**Expected:** Login form displayed

### 5.3 Create Test Users

#### Create Admin User
```sql
USE rit_placement;

-- Insert admin user
INSERT INTO users (usn, name, password_hash, role, is_active) 
VALUES ('ADMIN001', 'System Admin', '$2a$10$...', 'ADMIN', TRUE);
```

**Note:** Use BCrypt to generate password hash. Default password: `admin123`

#### Create Test Student
```sql
-- Insert student user
INSERT INTO users (usn, name, password_hash, role, is_active) 
VALUES ('1RV21IS001', 'Test Student', '$2a$10$...', 'STUDENT', TRUE);

-- Insert student details
INSERT INTO students (student_id, branch, current_sem) 
VALUES (LAST_INSERT_ID(), 'ISE', 6);

-- Insert academic records
INSERT INTO academic_records (student_id, semester, sgpa) 
VALUES 
  (LAST_INSERT_ID(), 1, 8.5),
  (LAST_INSERT_ID(), 2, 8.7),
  (LAST_INSERT_ID(), 3, 8.9);
```

#### Create Test Coordinator
```sql
-- Insert coordinator user
INSERT INTO users (usn, name, password_hash, role, is_active) 
VALUES ('COORD001', 'Placement Coordinator', '$2a$10$...', 'COORDINATOR', TRUE);
```

### 5.4 Test Login Flow

1. **Login as Admin**
   - URL: `http://localhost:8080/rit-placement/login`
   - USN: `ADMIN001`
   - Password: `admin123`
   - Expected: Redirect to admin dashboard

2. **Login as Student**
   - USN: `1RV21IS001`
   - Password: `student123`
   - Expected: Redirect to student dashboard

3. **Login as Coordinator**
   - USN: `COORD001`
   - Password: `coord123`
   - Expected: Redirect to coordinator dashboard

---

## ✅ STEP 6: FUNCTIONAL TESTING

### 6.1 Test Company Management (Coordinator)

1. Login as Coordinator
2. Navigate to "Companies"
3. Click "Add Company"
4. Fill form:
   - Company Name: Google
   - Type: PRODUCT
   - Description: Tech giant
5. Submit
6. **Expected:** Company appears in list

### 6.2 Test Job Posting (Coordinator)

1. Navigate to "Job Postings"
2. Click "Add Job Posting"
3. Fill form:
   - Company: Google
   - Role: Software Engineer
   - Package: 25.0
   - Min CGPA: 7.5
   - Branches: ISE, CSE
   - Skills: Java, Spring Boot
   - Deadline: (future date)
4. Submit
5. **Expected:** Job appears in list

### 6.3 Test Student Access (Student)

1. Login as Student
2. Navigate to "Companies"
3. **Expected:** Can view companies (read-only)
4. Navigate to "Job Postings"
5. **Expected:** Can view jobs (read-only)
6. Try to add company
7. **Expected:** No "Add Company" button visible

### 6.4 Test Application Flow (Student)

1. Navigate to "Apply for Jobs"
2. Find eligible job
3. **Expected:** "Apply Now" button enabled
4. Click "Apply Now"
5. Confirm application
6. **Expected:** Redirect to "My Applications"
7. **Expected:** Application shows status "APPLIED"

### 6.5 Test Eligibility Engine (Coordinator)

1. Login as Coordinator
2. Navigate to "Job Postings"
3. Click "View Eligible Students" on a job
4. **Expected:** List of eligible students displayed
5. **Expected:** Statistics showing eligible/ineligible counts
6. **Expected:** Students filtered by CGPA, branch, skills

### 6.6 Test Duplicate Prevention (Student)

1. Login as Student
2. Navigate to "Apply for Jobs"
3. Find job already applied to
4. **Expected:** Button shows "Already Applied" (disabled)
5. Try to apply again via direct POST
6. **Expected:** Error message "Already applied"

---

## 🔍 STEP 7: VERIFICATION CHECKLIST

### Database Verification ✅
- [ ] All tables created successfully
- [ ] All required columns present
- [ ] Foreign keys working
- [ ] UNIQUE constraints enforced
- [ ] Default values applied

### Application Verification ✅
- [ ] WAR deployed successfully
- [ ] No errors in Tomcat logs
- [ ] Login page accessible
- [ ] Static resources loading (CSS, JS)

### Authentication Verification ✅
- [ ] Login works for all roles
- [ ] Session created on login
- [ ] Session timeout works (30 min)
- [ ] Logout clears session
- [ ] Unauthorized access redirects to login

### Authorization Verification ✅
- [ ] Students can view companies (GET)
- [ ] Students can view jobs (GET)
- [ ] Students CANNOT create companies (POST)
- [ ] Students CANNOT create jobs (POST)
- [ ] Students CAN apply for jobs (POST)
- [ ] Coordinators can create companies
- [ ] Coordinators can create jobs
- [ ] Coordinators can view eligible students

### Functional Verification ✅
- [ ] Company CRUD works
- [ ] Job posting CRUD works
- [ ] Eligibility engine calculates correctly
- [ ] Application submission works
- [ ] Application tracking displays correctly
- [ ] Duplicate applications prevented
- [ ] Status updates work

### UI/UX Verification ✅
- [ ] Pages load without errors
- [ ] Forms validate input
- [ ] Success messages display
- [ ] Error messages display
- [ ] Empty states handled
- [ ] Loading overlays work
- [ ] Navigation works

---

## 🐛 TROUBLESHOOTING

### Issue: Database Connection Failed

**Symptoms:** Application fails to start, SQLException in logs

**Solution:**
```bash
# Check MySQL is running
sudo systemctl status mysql

# Verify credentials
mysql -u rit_user -p rit_placement -e "SELECT 1;"

# Check db.properties file
cat src/main/resources/db.properties
```

### Issue: 404 Not Found

**Symptoms:** Application URL returns 404

**Solution:**
```bash
# Check WAR deployed
ls -l $TOMCAT_HOME/webapps/rit-placement.war

# Check Tomcat logs
tail -f $TOMCAT_HOME/logs/catalina.out

# Verify context path
# URL should be: http://localhost:8080/rit-placement/
```

### Issue: Login Fails

**Symptoms:** Invalid credentials error

**Solution:**
```sql
-- Check user exists
SELECT * FROM users WHERE usn = 'ADMIN001';

-- Reset password (BCrypt hash for 'admin123')
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE usn = 'ADMIN001';
```

### Issue: Students Can't View Jobs

**Symptoms:** 403 Forbidden when accessing /job-postings

**Solution:**
```bash
# Verify SessionFilter.java has been updated
grep -A 5 "STUDENT_PATHS" src/main/java/com/rit/placement/filter/SessionFilter.java

# Should include:
# "/companies",
# "/job-postings"

# Rebuild and redeploy
mvn clean package
cp target/rit-placement.war $TOMCAT_HOME/webapps/
```

### Issue: SQLException on Insert

**Symptoms:** Column not found error

**Solution:**
```sql
-- Verify schema has all columns
DESCRIBE companies;
DESCRIBE job_postings;
DESCRIBE applications;

-- If columns missing, run schema again
mysql -u rit_user -p rit_placement < sql/schema.sql
```

---

## 📊 MONITORING

### Application Logs
```bash
# Tomcat logs
tail -f $TOMCAT_HOME/logs/catalina.out

# Application logs (if configured)
tail -f $TOMCAT_HOME/logs/rit-placement.log
```

### Database Monitoring
```sql
-- Check active connections
SHOW PROCESSLIST;

-- Check table sizes
SELECT 
    table_name,
    table_rows,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS "Size (MB)"
FROM information_schema.TABLES
WHERE table_schema = 'rit_placement'
ORDER BY (data_length + index_length) DESC;

-- Check recent applications
SELECT COUNT(*) FROM applications WHERE applied_at > NOW() - INTERVAL 1 DAY;
```

### Performance Monitoring
```bash
# Check Tomcat memory usage
ps aux | grep tomcat

# Check MySQL performance
mysqladmin -u root -p status
```

---

## 🔒 SECURITY CHECKLIST

### Pre-Production Security ✅
- [ ] Change default passwords
- [ ] Use strong database password
- [ ] Enable MySQL SSL (production)
- [ ] Configure firewall rules
- [ ] Disable directory listing in Tomcat
- [ ] Remove default Tomcat apps
- [ ] Set secure session timeout
- [ ] Enable HTTPS (production)
- [ ] Configure CORS properly
- [ ] Review error messages (no sensitive data)

### Database Security ✅
- [ ] Use dedicated database user (not root)
- [ ] Grant minimum required privileges
- [ ] Enable MySQL audit log
- [ ] Regular backups configured
- [ ] Backup encryption enabled

### Application Security ✅
- [ ] All passwords hashed with BCrypt
- [ ] PreparedStatement used (SQL injection protection)
- [ ] Session validation on all routes
- [ ] Role-based access control enabled
- [ ] Input validation on all forms
- [ ] XSS protection enabled

---

## 📦 BACKUP & RECOVERY

### Database Backup
```bash
# Daily backup
mysqldump -u rit_user -p rit_placement > backup_$(date +%Y%m%d).sql

# Automated backup (cron)
0 2 * * * mysqldump -u rit_user -p'password' rit_placement > /backups/rit_$(date +\%Y\%m\%d).sql
```

### Application Backup
```bash
# Backup WAR file
cp $TOMCAT_HOME/webapps/rit-placement.war /backups/rit-placement_$(date +%Y%m%d).war

# Backup configuration
cp src/main/resources/db.properties /backups/db.properties_$(date +%Y%m%d)
```

### Recovery
```bash
# Restore database
mysql -u rit_user -p rit_placement < backup_20240101.sql

# Restore application
cp /backups/rit-placement_20240101.war $TOMCAT_HOME/webapps/rit-placement.war
$TOMCAT_HOME/bin/shutdown.sh
$TOMCAT_HOME/bin/startup.sh
```

---

## 🎯 POST-DEPLOYMENT

### User Training
1. Create user documentation
2. Conduct training sessions for:
   - Students (how to apply)
   - Coordinators (how to manage companies/jobs)
   - Admins (system administration)

### Monitoring Setup
1. Set up application monitoring
2. Configure database monitoring
3. Set up alerts for errors
4. Monitor disk space
5. Monitor memory usage

### Maintenance Plan
1. Schedule regular backups
2. Plan for database maintenance
3. Monitor application logs
4. Review security updates
5. Plan for feature enhancements

---

## 📞 SUPPORT

### Common Issues
- Login problems → Check user credentials in database
- Access denied → Verify role and SessionFilter configuration
- Database errors → Check schema and connection
- Performance issues → Check database indexes and connection pool

### Contact
- **Technical Support:** [your-email@example.com]
- **Documentation:** See README_PRODUCTION.md
- **Issue Tracker:** [GitHub/GitLab URL]

---

## ✅ DEPLOYMENT COMPLETE

Once all steps are completed and verified:

1. ✅ Database setup complete
2. ✅ Application deployed
3. ✅ Smoke tests passed
4. ✅ Functional tests passed
5. ✅ Security checklist completed
6. ✅ Monitoring configured
7. ✅ Backups configured

**System Status:** 🎉 **PRODUCTION READY**

---

**Deployment Guide Version:** 1.0.0  
**Last Updated:** Day 3 - Production Release  
**Status:** ✅ Complete

