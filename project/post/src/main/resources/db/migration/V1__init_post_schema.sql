create table dbo.category (
        id bigint identity(1, 1) not null,
        name nvarchar(255) null,
        slug nvarchar(255) null,
        constraint PK_category primary key (id)
    );

go
create table dbo.tag (
        id bigint identity(1, 1) not null,
        name nvarchar(255) null,
        slug nvarchar(255) null,
        constraint PK_tag primary key (id)
);

create table dbo.post (
            id bigint identity (1, 1) not null,
            title nvarchar(255) not null,
            slug nvarchar(255) not null,
            summary nvarchar(500) not null,
            content nvarchar(max) not null,
            created_at datetime2(7) not null constraint df_post_created_at default sysutcdatetime(),
            updated_at datetime2(7) not null constraint df_post_updated_at default sysutcdatetime(),
            author_keycloak_id nvarchar(255) not null,
            author_username nvarchar(255) not null,
            author_avatar_url nvarchar(1024) null,
            category_id bigint null,
            thumbnail nvarchar(1024) null,
            view_count bigint not null
                constraint DF_posts_view_count default (0),
            comment_count bigint not null
                constraint DF_posts_comment_count default (0),
            reading_time int null ,
            is_deleted bit not null
                constraint DF_posts_is_deleted default (0),
            constraint PK_posts primary key (id),
            constraint FK_posts_category
                foreign key (category_id) references dbo.category(id) on delete set null

        );
go

        create table dbo.post_tags (
                                       post_id bigint not null ,
                                       tag_id bigint not null ,

                                       constraint pk_post_tags primary key (post_id, tag_id),
                                       constraint fk_post_tags_post
                                           foreign key (post_id) references dbo.post(id) on delete cascade,
                                       constraint fk_post_tags_tag
                                           foreign key (tag_id) references dbo.tag(id) on delete cascade
        );


create unique index idx_post_slug on dbo.post(slug);
create index idx_post_author_username on dbo.post(author_username);
create index idx_created_at on dbo.post(created_at);
create index idx_post_tags_tag_id on dbo.post_tags(tag_id);