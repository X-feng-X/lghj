-- 初始化insert指令

insert into `role` (`role_name`)
values ('普通用户'),
       ('机构用户'),
       ('超级管理员'),
       ('系统管理员'),
       ('运营管理员'),
       ('内容管理员');

# insert into permissions (permissions_name)
# values ('用户管理'),
#        ('审核管理'),
#        ('模型管理');

insert into user(username, password, user_type)
values ('admin', '123456', 3);

insert into user_role (user_id, role_id)
values (1, 3);
