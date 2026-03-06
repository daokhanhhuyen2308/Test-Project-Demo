create table outbox_event (
    id char(36) not null ,
    aggregate_type varchar(100) not null,
    aggregate_id varchar(100) not null,
    topic varchar(100) not null,
    payload longtext not null,
    outbox_status varchar(20) not null default 'PENDING',
    created_at timestamp(6) not null default current_timestamp(6),
    processed_at timestamp(6) null,
    sent_at timestamp(6) null,
    retry_count int not null default 0,
    last_error varchar(2000) null,
    primary key (id),
    index idx_outbox_status_created (outbox_status, created_at),
    index idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB default CHARSET=utf8mb4;