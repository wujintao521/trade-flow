SET time_zone = '+00:00';

CREATE TABLE auth_user (
                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                           username VARCHAR(64) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT '登录名，应用统一转为小写',
                           password_hash VARCHAR(255) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT 'PasswordEncoder生成的密码哈希，包含算法标识',
                           display_name VARCHAR(64) NOT NULL COMMENT '用户显示名称',
                           status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
                           created_by BIGINT DEFAULT NULL COMMENT '创建账号的管理员ID，受控初始化创建时为空',
                           created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间，按UTC记录',
                           updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间，按UTC记录',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_auth_user_username (username),
                           CONSTRAINT ck_auth_user_status CHECK (status IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '认证用户账号';

CREATE TABLE auth_role (
                           id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                           role_code VARCHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '角色编码',
                           role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
                           created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间，按UTC记录',
                           PRIMARY KEY (id),
                           UNIQUE KEY uk_auth_role_code (role_code)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '认证角色';

CREATE TABLE auth_user_role (
                                user_id BIGINT NOT NULL COMMENT '用户ID',
                                role_id BIGINT NOT NULL COMMENT '角色ID',
                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '角色分配时间，按UTC记录',
                                PRIMARY KEY (user_id, role_id),
                                KEY idx_auth_user_role_role_user (role_id, user_id),
                                CONSTRAINT fk_auth_user_role_user FOREIGN KEY (user_id) REFERENCES auth_user (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT fk_auth_user_role_role FOREIGN KEY (role_id) REFERENCES auth_role (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '认证用户角色关系';

INSERT INTO auth_role (role_code, role_name)
VALUES
    ('ADMIN', '系统管理员'),
    ('USER', '普通用户');