-- ============================================================
-- Company Portal Module - Production Readiness Validation
-- Run this script to verify all fixes are in place
-- ============================================================

SELECT '🔍 VALIDATING COMPANY PORTAL MODULE...' as status;
SELECT '' as blank_line;

-- ============================================================
-- TEST 1: Schema Validation
-- ============================================================
SELECT '✅ TEST 1: Schema Validation' as test_name;

-- Check users.company_id column exists
SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN '✅ PASS: users.company_id column exists'
        ELSE '❌ FAIL: users.company_id column missing'
    END as result
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'company_id';

-- Check users.role includes COMPANY
SELECT 
    CASE 
        WHEN COLUMN_TYPE LIKE '%COMPANY%' THEN '✅ PASS: users.role includes COMPANY'
        ELSE '❌ FAIL: users.role missing COMPANY'
    END as result
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'role';

-- Check applications.status includes INTERVIEW
SELECT 
    CASE 
        WHEN COLUMN_TYPE LIKE '%INTERVIEW%' THEN '✅ PASS: applications.status includes INTERVIEW'
        ELSE '❌ FAIL: applications.status missing INTERVIEW'
    END as result
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'applications' 
AND COLUMN_NAME = 'status';

-- Check interviews table exists
SELECT 
    CASE 
        WHEN COUNT(*) = 1 THEN '✅ PASS: interviews table exists'
        ELSE '❌ FAIL: interviews table missing'
    END as result
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'interviews';

-- Check notifications table structure
SELECT 
    CASE 
        WHEN COUNT(*) >= 7 THEN '✅ PASS: notifications table has correct structure'
        ELSE '❌ FAIL: notifications table incomplete'
    END as result
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'notifications';

-- Check students table has profile fields
SELECT 
    CASE 
        WHEN COUNT(*) >= 3 THEN '✅ PASS: students table has profile fields (skills, projects, experience)'
        ELSE '❌ FAIL: students table missing profile fields'
    END as result
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'students'
AND COLUMN_NAME IN ('skills', 'projects', 'experience');

SELECT '' as blank_line;

-- ============================================================
-- TEST 2: Foreign Key Constraints
-- ============================================================
SELECT '✅ TEST 2: Foreign Key Constraints' as test_name;

-- Check users.company_id foreign key
SELECT 
    CASE 
        WHEN COUNT(*) >= 1 THEN '✅ PASS: users.company_id foreign key exists'
        ELSE '⚠️  WARNING: users.company_id foreign key missing (optional)'
    END as result
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'company_id'
AND REFERENCED_TABLE_NAME = 'companies';

SELECT '' as blank_line;

-- ============================================================
-- TEST 3: Indexes
-- ============================================================
SELECT '✅ TEST 3: Performance Indexes' as test_name;

-- Check users.role index
SELECT 
    CASE 
        WHEN COUNT(*) >= 1 THEN '✅ PASS: users.role index exists'
        ELSE '⚠️  WARNING: users.role index missing (performance impact)'
    END as result
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'role';

-- Check users.company_id index
SELECT 
    CASE 
        WHEN COUNT(*) >= 1 THEN '✅ PASS: users.company_id index exists'
        ELSE '⚠️  WARNING: users.company_id index missing (performance impact)'
    END as result
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'users' 
AND COLUMN_NAME = 'company_id';

SELECT '' as blank_line;

-- ============================================================
-- TEST 4: Data Integrity
-- ============================================================
SELECT '✅ TEST 4: Data Integrity Checks' as test_name;

-- Check for orphaned applications (jobs deleted but applications remain)
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ PASS: No orphaned applications'
        ELSE CONCAT('⚠️  WARNING: ', COUNT(*), ' orphaned applications found')
    END as result
FROM applications a
LEFT JOIN job_postings jp ON a.job_id = jp.job_id
WHERE jp.job_id IS NULL;

-- Check for company users without company_id
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ PASS: All COMPANY users have company_id'
        ELSE CONCAT('❌ FAIL: ', COUNT(*), ' COMPANY users missing company_id')
    END as result
FROM users
WHERE role = 'COMPANY' AND company_id IS NULL;

-- Check for invalid application statuses
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✅ PASS: All application statuses are valid'
        ELSE CONCAT('❌ FAIL: ', COUNT(*), ' applications with invalid status')
    END as result
FROM applications
WHERE status NOT IN ('PENDING', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED');

SELECT '' as blank_line;

-- ============================================================
-- TEST 5: Sample Data Check
-- ============================================================
SELECT '✅ TEST 5: Sample Data Availability' as test_name;

-- Check if companies exist
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ INFO: ', COUNT(*), ' companies in database')
        ELSE '⚠️  INFO: No companies yet (add via admin panel)'
    END as result
FROM companies;

-- Check if company users exist
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ INFO: ', COUNT(*), ' company users in database')
        ELSE '⚠️  INFO: No company users yet (create via OTP registration)'
    END as result
FROM users
WHERE role = 'COMPANY';

-- Check if job postings exist
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ INFO: ', COUNT(*), ' job postings in database')
        ELSE '⚠️  INFO: No job postings yet'
    END as result
FROM job_postings;

-- Check if applications exist
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ INFO: ', COUNT(*), ' applications in database')
        ELSE '⚠️  INFO: No applications yet'
    END as result
FROM applications;

-- Check if interviews exist
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN CONCAT('✅ INFO: ', COUNT(*), ' interviews scheduled')
        ELSE '⚠️  INFO: No interviews scheduled yet'
    END as result
FROM interviews;

SELECT '' as blank_line;

-- ============================================================
-- TEST 6: Status Distribution
-- ============================================================
SELECT '✅ TEST 6: Application Status Distribution' as test_name;

SELECT 
    status,
    COUNT(*) as count,
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM applications), 1), '%') as percentage
FROM applications
GROUP BY status
ORDER BY 
    CASE status
        WHEN 'PENDING' THEN 1
        WHEN 'SHORTLISTED' THEN 2
        WHEN 'INTERVIEW' THEN 3
        WHEN 'SELECTED' THEN 4
        WHEN 'REJECTED' THEN 5
    END;

SELECT '' as blank_line;

-- ============================================================
-- FINAL SUMMARY
-- ============================================================
SELECT '📊 VALIDATION SUMMARY' as summary;

SELECT 
    CONCAT(
        '✅ Schema: ', 
        CASE WHEN (
            SELECT COUNT(*) FROM information_schema.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'company_id'
        ) = 1 THEN 'READY' ELSE 'NOT READY' END
    ) as schema_status;

SELECT 
    CONCAT(
        '✅ Tables: ', 
        CASE WHEN (
            SELECT COUNT(*) FROM information_schema.TABLES 
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('users', 'companies', 'job_postings', 'applications', 'interviews', 'notifications')
        ) = 6 THEN 'READY' ELSE 'NOT READY' END
    ) as tables_status;

SELECT 
    CONCAT(
        '✅ Data Integrity: ', 
        CASE WHEN (
            SELECT COUNT(*) FROM applications a
            LEFT JOIN job_postings jp ON a.job_id = jp.job_id
            WHERE jp.job_id IS NULL
        ) = 0 THEN 'READY' ELSE 'ISSUES FOUND' END
    ) as integrity_status;

SELECT '' as blank_line;
SELECT '🎉 VALIDATION COMPLETE!' as status;
SELECT 'Review results above. All ✅ PASS items indicate production readiness.' as note;
SELECT 'Any ❌ FAIL items must be fixed before deployment.' as warning;
