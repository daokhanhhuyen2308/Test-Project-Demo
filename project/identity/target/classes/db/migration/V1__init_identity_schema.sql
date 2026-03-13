create table user_entity(
    id varchar(36) not null primary key,
    keycloak_id varchar(255) not null,
    email varchar(255) not null unique,
    username varchar(255) not null unique,
    user_status varchar(50),
    avatar_url varchar(500) null,
    created_at timestamp(6) not null default current_timestamp(6),
    updated_at timestamp(6) not null default current_timestamp(6)
);


create table role_entity(
    name varchar(50) not null primary key,
    description varchar(255)
);

create table user_role(
  user_id varchar(36) not null,
  role_id varchar(50) not null,
  primary key (user_id, role_id),
  constraint FK_user_role_user foreign key (user_id) references user_entity(id),
  constraint FK_user_role_role foreign key (role_id) references role_entity(name)
);
