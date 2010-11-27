create table EasyTable (
 identifier bigint primary key,
 name varchar(100) not null
);

create table OneToOneJpa (
 key bigint primary key,
 easy_id bigint 
);