-- 创建认领申请表
CREATE TABLE claim_applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认领申请ID',
    found_item_id BIGINT NOT NULL COMMENT '失物招领ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    description VARCHAR(500) NOT NULL COMMENT '申请说明',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '申请状态：pending(待处理), approved(已批准), rejected(已拒绝)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    processed_at TIMESTAMP NULL COMMENT '处理时间',
    
    INDEX idx_found_item_id (found_item_id) COMMENT '失物招领索引',
    INDEX idx_applicant_id (applicant_id) COMMENT '申请人索引',
    INDEX idx_status (status) COMMENT '状态索引',
    
    CONSTRAINT fk_claim_application_found_item FOREIGN KEY (found_item_id) REFERENCES found_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_application_user FOREIGN KEY (applicant_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认领申请表'; 