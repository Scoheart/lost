-- Add phone and real_name columns to the users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS real_name VARCHAR(100);

-- Update existing users with empty values
UPDATE users SET phone = '', real_name = '' WHERE phone IS NULL; 