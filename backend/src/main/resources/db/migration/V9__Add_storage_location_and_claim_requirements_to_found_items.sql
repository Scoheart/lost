-- Add storage_location and claim_requirements columns to found_items table
ALTER TABLE found_items 
ADD COLUMN storage_location VARCHAR(255) COMMENT '存放位置' AFTER found_location,
ADD COLUMN claim_requirements TEXT COMMENT '认领要求' AFTER contact_info; 