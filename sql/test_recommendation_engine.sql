-- ============================================================
-- Job Recommendation Engine - Test Data
-- Creates sample data to demonstrate recommendation system
-- ============================================================

-- STEP 1: Ensure we have companies
INSERT INTO companies (company_name, description, company_type) VALUES
('TechCorp Solutions', 'Leading software development company', 'PRODUCT'),
('DataMinds Analytics', 'Data science and AI company', 'PRODUCT'),
('WebWorks Inc', 'Full-stack web development', 'SERVICE'),
('CloudNine Systems', 'Cloud infrastructure provider', 'MNC'),
('StartupHub', 'Innovative startup accelerator', 'STARTUP')
ON DUPLICATE KEY UPDATE company_name = company_name;

-- STEP 2: Create diverse job postings
INSERT INTO job_postings (company_id, role, package, min_cgpa, allowed_branches, required_skills, deadline) VALUES
-- High CGPA, Java/Python jobs
((SELECT company_id FROM companies WHERE company_name = 'TechCorp Solutions'), 
 'Software Engineer', 12.00, 8.00, 'CSE,ISE,ECE', 'Java,Spring Boot,MySQL,REST API', '2026-06-30'),

((SELECT company_id FROM companies WHERE company_name = 'TechCorp Solutions'), 
 'Backend Developer', 10.00, 7.50, 'CSE,ISE', 'Python,Django,PostgreSQL,Redis', '2026-06-15'),

-- Data Science jobs
((SELECT company_id FROM companies WHERE company_name = 'DataMinds Analytics'), 
 'Data Analyst', 8.00, 7.00, 'CSE,ISE,ECE,MECH', 'Python,SQL,Excel,Tableau', '2026-07-15'),

((SELECT company_id FROM companies WHERE company_name = 'DataMinds Analytics'), 
 'Machine Learning Engineer', 15.00, 8.50, 'CSE,ISE', 'Python,TensorFlow,PyTorch,ML,AI', '2026-07-01'),

-- Web Development jobs
((SELECT company_id FROM companies WHERE company_name = 'WebWorks Inc'), 
 'Full Stack Developer', 9.00, 7.00, 'CSE,ISE', 'JavaScript,React,Node.js,MongoDB', '2026-06-20'),

((SELECT company_id FROM companies WHERE company_name = 'WebWorks Inc'), 
 'Frontend Developer', 7.50, 6.50, 'CSE,ISE,ECE', 'HTML,CSS,JavaScript,React,Vue.js', '2026-06-25'),

-- Cloud/DevOps jobs
((SELECT company_id FROM companies WHERE company_name = 'CloudNine Systems'), 
 'DevOps Engineer', 11.00, 7.50, 'CSE,ISE', 'AWS,Docker,Kubernetes,Jenkins,Linux', '2026-07-10'),

((SELECT company_id FROM companies WHERE company_name = 'CloudNine Systems'), 
 'Cloud Architect', 16.00, 8.00, 'CSE,ISE', 'AWS,Azure,GCP,Terraform,Microservices', '2026-07-05'),

-- Startup opportunities
((SELECT company_id FROM companies WHERE company_name = 'StartupHub'), 
 'Software Developer', 6.00, 6.00, 'CSE,ISE,ECE,MECH,CIVIL', 'Any programming language,Problem solving', '2026-08-01'),

((SELECT company_id FROM companies WHERE company_name = 'StartupHub'), 
 'Product Engineer', 8.00, 7.00, 'CSE,ISE', 'Java,Python,JavaScript,Agile,Git', '2026-07-20')
ON DUPLICATE KEY UPDATE role = role;

-- STEP 3: Create sample students with different profiles
-- Note: Assumes users table already has some students

-- Update student 1: High CGPA, Java/Python skills
UPDATE students SET 
    skills = 'Java, Python, Spring Boot, MySQL, REST API, Git',
    projects = 'E-commerce website, Library management system',
    experience = 'Internship at Tech Company (3 months)'
WHERE student_id = (SELECT user_id FROM users WHERE role = 'STUDENT' LIMIT 1);

-- Update student 2: Moderate CGPA, Web development skills
UPDATE students SET 
    skills = 'JavaScript, React, Node.js, HTML, CSS, MongoDB',
    projects = 'Portfolio website, Chat application',
    experience = 'Freelance web developer'
WHERE student_id = (SELECT user_id FROM users WHERE role = 'STUDENT' LIMIT 1 OFFSET 1);

-- Update student 3: Data science focus
UPDATE students SET 
    skills = 'Python, SQL, Pandas, NumPy, Matplotlib, Excel',
    projects = 'Sales prediction model, Customer segmentation',
    experience = 'Data analysis internship'
WHERE student_id = (SELECT user_id FROM users WHERE role = 'STUDENT' LIMIT 1 OFFSET 2);

-- STEP 4: Verify setup
SELECT '✅ Test data created successfully!' as status;

-- Show companies
SELECT 'Companies:' as info;
SELECT company_id, company_name, company_type FROM companies;

-- Show job postings
SELECT 'Job Postings:' as info;
SELECT 
    jp.job_id,
    c.company_name,
    jp.role,
    jp.package as package_lpa,
    jp.min_cgpa,
    jp.required_skills
FROM job_postings jp
JOIN companies c ON jp.company_id = c.company_id
ORDER BY jp.created_at DESC
LIMIT 10;

-- Show students with skills
SELECT 'Students with Skills:' as info;
SELECT 
    u.user_id,
    u.name,
    u.usn,
    s.branch,
    s.skills
FROM users u
JOIN students s ON u.user_id = s.student_id
WHERE u.role = 'STUDENT'
LIMIT 5;

SELECT '🎯 Recommendation engine test data ready!' as result;
SELECT 'Students can now visit /student/recommendations to see personalized job suggestions' as next_step;

COMMIT;
