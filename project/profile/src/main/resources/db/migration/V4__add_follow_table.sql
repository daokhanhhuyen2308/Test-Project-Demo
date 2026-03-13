create table user_follow (
    id bigint auto_increment primary key,
    follower_id varchar(36) not null,
    following_id varchar(36) not null,
    created_at timestamp not null default current_timestamp,
    unique key uk_follower_following (follower_id, following_id),
    constraint chk_no_self_follow check (follower_id <> following_id),
    constraint fk_follower foreign key (follower_id) references profile(id),
    constraint fk_following foreign key (following_id) references profile(id)
);

create index idx_user_follow_follower_id on user_follow(follower_id);
create index idx_user_follow_following_id on user_follow(following_id);

alter table profile add column follower_count bigint not null default 0;
alter table profile add column following_count bigint not null default 0;
alter table profile add column bio varchar(1000) null;
alter table profile add column created_at timestamp not null default current_timestamp;
alter table profile add column last_modified_at timestamp not null default current_timestamp;