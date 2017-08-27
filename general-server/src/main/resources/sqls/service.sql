USE general;

DROP TABLE general_service;

CREATE TABLE general_service (
  id                   INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name                 VARCHAR(128) NOT NULL DEFAULT 'unknown'
  COMMENT '服务的名字',
  description          VARCHAR(256) COMMENT '服务相关的描述信息',
  ip                   VARCHAR(128) NOT NULL DEFAULT ''
  COMMENT '服务部署的服务器IP',
  port                 VARCHAR(64)  NOT NULL DEFAULT ''
  COMMENT '服务部署的服务器端口',
  `status`             INT(11)               DEFAULT 2
  COMMENT '服务所处的状态',
  health_check_url     VARCHAR(256) COMMENT '健康检查URL',
  heach_check_interval INT(20) COMMENT '健康检查间隔'
);

ALTER TABLE general_service ADD UNIQUE('name','ip','port')