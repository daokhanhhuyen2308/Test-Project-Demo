create table if not exists processed_event (
    event_id varchar(36) not null ,
    status varchar(50) not null ,
    attempts int not null default 0,
    last_error text,
    updated_at datetime(6),
    next_retry_at datetime(6) null,
    payload longtext null,
    primary key (event_id)
) engine =InnoDB default charset =utf8mb4 collate =utf8mb4_unicode_ci;