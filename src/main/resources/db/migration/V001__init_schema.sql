-- 组织架构与人员管理系统 - 数据库初始化脚本
-- Proposal: testdj-04-organization-management

-- ============================================
-- 部门表 (物化路径模式)
-- ============================================
CREATE TABLE IF NOT EXISTS department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父部门ID',
    path VARCHAR(500) NOT NULL DEFAULT '' COMMENT '物化路径: /1/2/3',
    level INT NOT NULL DEFAULT 1 COMMENT '层级深度',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ============================================
-- 员工表
-- ============================================
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    employee_no VARCHAR(20) NOT NULL COMMENT '工号',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    position VARCHAR(100) DEFAULT NULL COMMENT '职位',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-在职, 0-离职',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    hire_date DATE DEFAULT NULL COMMENT '入职日期',
    resign_date DATE DEFAULT NULL COMMENT '离职日期',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    updated_by VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    
    UNIQUE KEY uk_employee_no (employee_no),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_dept_id (dept_id),
    INDEX idx_status (status),
    INDEX idx_name (name),
    
    CONSTRAINT fk_employee_dept FOREIGN KEY (dept_id) REFERENCES department(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- ============================================
-- 调动记录表
-- ============================================
CREATE TABLE IF NOT EXISTS transfer_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '调动记录ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    from_dept_id BIGINT NOT NULL COMMENT '原部门ID',
    to_dept_id BIGINT NOT NULL COMMENT '目标部门ID',
    from_position VARCHAR(100) DEFAULT NULL COMMENT '原职位',
    to_position VARCHAR(100) DEFAULT NULL COMMENT '新职位',
    transfer_type TINYINT NOT NULL DEFAULT 1 COMMENT '调动类型: 1-部门调动, 2-职位变更, 3-部门+职位',
    reason VARCHAR(500) DEFAULT NULL COMMENT '调动原因',
    transfer_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调动时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    
    INDEX idx_employee_id (employee_id),
    INDEX idx_transfer_date (transfer_date),
    INDEX idx_from_dept (from_dept_id),
    INDEX idx_to_dept (to_dept_id),
    
    CONSTRAINT fk_transfer_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    CONSTRAINT fk_transfer_from_dept FOREIGN KEY (from_dept_id) REFERENCES department(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transfer_to_dept FOREIGN KEY (to_dept_id) REFERENCES department(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工调动记录表';

-- ============================================
-- 初始化根部门数据
-- ============================================
INSERT INTO department (id, name, parent_id, path, level, sort_order, status) 
VALUES (1, '总公司', NULL, '/1/', 1, 0, 1);