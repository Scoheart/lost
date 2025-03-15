-- 重命名author_id为admin_id
ALTER TABLE announcements CHANGE COLUMN author_id admin_id BIGINT;

-- 删除不需要的字段
ALTER TABLE announcements 
    DROP COLUMN IF EXISTS community_id,
    DROP COLUMN IF EXISTS is_important,
    DROP COLUMN IF EXISTS view_count;

-- 确保is_sticky字段存在
ALTER TABLE announcements MODIFY COLUMN is_sticky BOOLEAN NOT NULL DEFAULT FALSE;

-- 确保is_pinned字段存在（用于兼容，与is_sticky保持一致）
ALTER TABLE announcements MODIFY COLUMN is_pinned BOOLEAN NOT NULL DEFAULT FALSE;

-- 确保status字段存在
ALTER TABLE announcements MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'published';

-- 更新任何可能的null值
UPDATE announcements SET is_sticky = FALSE WHERE is_sticky IS NULL;
UPDATE announcements SET is_pinned = FALSE WHERE is_pinned IS NULL;
UPDATE announcements SET status = 'published' WHERE status IS NULL;

-- 删除和重建索引
DROP INDEX IF EXISTS idx_announcements_admin_id;
CREATE INDEX idx_announcements_admin_id ON announcements(admin_id);

DROP INDEX IF EXISTS idx_announcements_status;
CREATE INDEX idx_announcements_status ON announcements(status);

DROP INDEX IF EXISTS idx_announcements_created_at;
CREATE INDEX idx_announcements_created_at ON announcements(created_at);

-- 更新auto_increment
ALTER TABLE announcements AUTO_INCREMENT = 1000; 