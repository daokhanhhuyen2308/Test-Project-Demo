create table category (
    id bigint auto_increment not null,
    name varchar(255) null,
    slug varchar(255) null,
    primary key (id)
);

create table tag (
    id bigint auto_increment not null,
    name varchar(255) null,
    slug varchar(255) null,
    primary key (id)
);

create table post (
    id bigint auto_increment not null,
    title varchar(255) not null,
    slug varchar(255) not null,
    summary varchar(500) not null,
    content longtext not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    author_keycloak_id varchar(100) not null,
    author_username varchar(255) not null,
    author_avatar_url varchar(1024) null,
    category_id bigint null,
    thumbnail varchar(1024) null,
    view_count bigint not null default 0,
    comment_count bigint not null default 0,
    reading_time int null,
    is_deleted tinyint(1) not null default 0,
    primary key (id),
    unique index idx_post_slug (slug),
    index idx_post_author_username (author_username),
    index idx_created_at (created_at),
    index idx_post_author_keycloak (author_keycloak_id),
    constraint fk_posts_category foreign key (category_id) references category(id) on delete set null
);

create table post_tags (
    post_id bigint not null,
    tag_id bigint not null,
    primary key (post_id, tag_id),
    index idx_post_tags_tag_id (tag_id),
    constraint fk_post_tags_post foreign key (post_id) references post(id) on delete cascade,
    constraint fk_post_tags_tag foreign key (tag_id) references tag(id) on delete cascade
);