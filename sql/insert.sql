insert into users(username, email, password, created_at) values('admin', 'admin@aulab.it', '$2a$10$oMiUOq5ToRfUI/Zprg5nE.qt8nT9KKJZoDBu1SIWuj.UGx8aRHwxS0', '20240607');

insert into roles(name) values('ROLE_ADMIN'), ('ROLE_REVISOR'),('ROLE_WRITER'), ('ROLE_USER');

insert into users_roles (user_id, role_id) values (1, 1);

insert into categories (name) values ('politica'), ('economia'), ('food&drink'), ('sport'), ('intrattenimento'), ('tech');