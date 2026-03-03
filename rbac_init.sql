-- 创建系统角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` tinyint(2) NOT NULL DEFAULT '1' COMMENT '状态：1正常 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 创建角色-权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_code` varchar(100) NOT NULL COMMENT '权限编码(前端菜单name)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- admin 表增加角色关联
ALTER TABLE `admins` ADD COLUMN `role_id` bigint(20) DEFAULT NULL COMMENT '关联角色ID' AFTER `password`;

-- 初始化超级管理员角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`) 
VALUES (1, '超级管理员', 'super_admin', '系统超级管理员，所有权限', 1) 
ON DUPLICATE KEY UPDATE `role_name`='超级管理员';

-- 绑定初始admin账号到超级管理员
UPDATE `admins` SET `role_id` = 1 WHERE `username` = 'admin';
