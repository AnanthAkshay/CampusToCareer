-- Update all student emails from @rit.edu to @msrit.edu
UPDATE users 
SET email = REPLACE(email, '@rit.edu', '@msrit.edu')
WHERE role = 'STUDENT' AND email LIKE '%@rit.edu';

-- Verify the update
SELECT usn, email FROM users WHERE role = 'STUDENT' LIMIT 10;
