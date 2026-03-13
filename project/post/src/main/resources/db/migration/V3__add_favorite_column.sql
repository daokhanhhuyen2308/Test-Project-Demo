alter table post add column favorite_count bigint default 0;
alter table post add column is_favorited bit default false;