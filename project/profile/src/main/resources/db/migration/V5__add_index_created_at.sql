# follower_id -> following_id
# ? ->  Author
create index idx_user_follow_following_created_at on user_follow (following_id, created_at desc );

#  Author -> ?
create index idx_user_follow_follower_created_at on user_follow(follower_id, created_at desc );