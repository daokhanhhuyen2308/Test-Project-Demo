set foreign_key_checks = 0;

update user_follow u
    join profile p on u.follower_id = p.id
    set u.follower_id = p.keycloak_id;

update user_follow u
    join profile p on u.following_id = p.id
    set u.following_id = p.keycloak_id;

update profile set id = keycloak_id;

alter table user_follow drop foreign key fk_follower;
alter table user_follow drop foreign key fk_following;

alter table user_follow add constraint fk_follower
    foreign key (follower_id) references profile(id);

alter table user_follow add constraint fk_following
    foreign key (following_id) references profile(id);

set foreign_key_checks = 1;