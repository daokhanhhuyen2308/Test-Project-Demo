alter table dbo.post alter column author_keycloak_id varchar(100) not null
go
create index idx_post_author_keycloak on dbo.post(author_keycloak_id);