-- 给公告表添加新字段
ALTER TABLE announcements ADD COLUMN IF NOT EXISTS community_id BIGINT COMMENT '所属社区ID' AFTER author_id;
ALTER TABLE announcements ADD COLUMN IF NOT EXISTS view_count INT NOT NULL DEFAULT 0 COMMENT '查看次数' AFTER status;

-- 重命名字段：is_pinned -> is_sticky (确保向后兼容)
-- 首先，我们添加一个新字段
ALTER TABLE announcements ADD COLUMN IF NOT EXISTS is_sticky BOOLEAN COMMENT '是否置顶' AFTER community_id;

-- 复制数据
UPDATE announcements SET is_sticky = is_pinned WHERE is_sticky IS NULL;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_sticky_created ON announcements (is_sticky, created_at);
CREATE INDEX IF NOT EXISTS idx_community_id ON announcements (community_id);

-- 注意：我们保留is_pinned字段以确保向后兼容，可以在应用稳定后通过另一个迁移脚本移除 