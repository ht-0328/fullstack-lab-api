create table tasks (
    id          bigint generated always as identity primary key,
    title       varchar(100)  not null,
    description varchar(1000),
    due_date    date,
    created_by  varchar(100)  not null,
    created_at  timestamptz   not null default now()
);
