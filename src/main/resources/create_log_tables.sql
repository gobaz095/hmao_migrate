create table if not exists migrate_dzp_citizen
(
    source_id numeric primary key,
    client_types_id integer not null,
    idcitizen numeric not null,
    create_date timestamp with time zone not null default current_timestamp
);