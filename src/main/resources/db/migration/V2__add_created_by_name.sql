-- 認証導入に伴い、created_by には IdP のユーザー ID（sub）を入れ、表示用の名前を別に持つ
alter table tasks add column created_by_name varchar(100);
update tasks set created_by_name = created_by;
alter table tasks alter column created_by_name set not null;

create index tasks_created_by_idx on tasks (created_by);
