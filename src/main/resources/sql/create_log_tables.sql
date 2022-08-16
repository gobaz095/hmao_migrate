create table if not exists migrate_dzp_citizen
(
    source_id numeric primary key,
    client_types_id integer not null,
    idcitizen numeric not null,
    new_item boolean not null default true,
    create_date timestamp with time zone not null default current_timestamp
);

create table if not exists migrate_dzp_applicant
(
    movesetid numeric primary key,
    idapplicant numeric not null,
    create_date timestamp with time zone not null default current_timestamp
);