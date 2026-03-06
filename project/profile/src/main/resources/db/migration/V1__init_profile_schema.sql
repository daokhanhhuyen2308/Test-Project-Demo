create table if not exists profile(
  id char(36) not null,
  keycloak_id varchar(50) not null,
  username varchar(255),
  email varchar(255),
  avatar_url varchar(500),
  primary key (id)
);
