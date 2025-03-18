create table if not exists sys_info
(
    id           bigint unsigned not null,
    platform     varchar(50)     not null,
    os_arch      varchar(50)     not null,
    gpu_platform varchar(50)     null,
    create_time  datetime,
    update_time  datetime,
    primary key (id)
);

create table if not exists user
(
    id          bigint unsigned not null,
    username    varchar(50)     not null,
    email       varchar(50)     not null,
    password    char(32)        not null,
    role        varchar(5)      not null,
    create_time datetime,
    update_time datetime,
    primary key (id)
);

create unique index if not exists sys_info_username_idx on `user`(username);


create table if not exists model
(
    id                bigint unsigned not null,
    name              varchar(50)     not null,
    repo              varchar(50)     not null,
    download_platform varchar(50)     null,
    files             longtext        null,
    save_dir          varchar(255)    null,
    create_time       datetime,
    update_time       datetime,
    primary key (id)
);

create unique index if not exists model_name_idx on `model`(name);

create table if not exists file_download
(
    id          bigint unsigned not null,
    model_id    bigint unsigned not null,
    model_name  varchar(50)     not null,
    file_path   varchar(255)    not null,
    file_name   varchar(50)     not null,
    file_size   bigint unsigned not null,
    create_time datetime,
    update_time datetime,
    primary key (id)
);

create table if not exists llama_exec_his
(
    id                bigint unsigned not null,
    model_id          bigint unsigned not null,
    model_name        varchar(50)     not null,
    file_id           bigint unsigned not null,
    file_path         varchar(255)    not null,
    file_name         varchar(50)     not null,
    llama_cpp_dir     varchar(255)    not null,
    llama_cpp_command varchar(50)     not null,
    llama_cpp_args    varchar(1000)   null,
    status            int unsigned    not null default 0 comment '0 for not start, 1 for start',
    pid               varchar(50)     null comment '执行llama.cpp进程的pid',
    log_file_path     varchar(255)    null comment '执行llama.cpp进程日志文件目录',
    create_time       datetime,
    update_time       datetime,
    primary key (id)
)



