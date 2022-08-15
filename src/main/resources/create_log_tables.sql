create table if not exists migrate_dzp_citizen
(
    source_id numeric primary key,
    idcitizen numeric,
    create_date timestamp with time zone default CURRENT_TIMESTAMP
);