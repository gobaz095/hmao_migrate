create table if not exists migrate_dzp_citizen
(
    client_id numeric not null,
    idcitizen numeric not null,
    client_types_id integer not null,
    new_item boolean not null default true,
    create_date timestamp with time zone not null default current_timestamp,
    CONSTRAINT  pk_migrate_dzp_citizen PRIMARY KEY (client_id, idcitizen)
);

create table if not exists migrate_dzp_applicant
(
    object_id numeric not null,
    movesets_id numeric not null,
    client_id numeric not null,
    idapplicant numeric not null,
    idcitizen numeric not null,
    create_date timestamp with time zone not null default current_timestamp,
    CONSTRAINT  pk_migrate_dzp_applicant PRIMARY KEY (object_id, movesets_id)
);

create table if not exists migrate_dzp_applicant_participant
(
    movesets_id numeric not null,
    client_id numeric not null,
    idapplicant numeric not null,
    idcitizen numeric not null,
    create_date timestamp with time zone not null default current_timestamp,
    CONSTRAINT  pk_migrate_dzp_applicant_participant PRIMARY KEY (movesets_id)
    );

create table if not exists migrate_dzp_family
(
    source_id varchar(60) primary key,
    idapplicant numeric not null,
    idcitizen numeric not null,
    idfamily numeric not null,
    create_date timestamp with time zone not null default current_timestamp
);

create table if not exists migrate_dzp_placement
(
    object_id numeric primary key,
    placementuuid UUID not null,
    new_item boolean not null default true,
    create_date timestamp with time zone not null default current_timestamp
);

create table if not exists migrate_dzp_contract
(
    contractnumber text primary key,
    create_date timestamp with time zone not null default current_timestamp
);

ALTER TABLE dzp_citizen ADD COLUMN IF NOT EXISTS citizentype integer default 1;

ALTER TABLE dzp_citizen ADD COLUMN IF NOT EXISTS fullname_normalized text;

UPDATE dzp_citizen set fullname_normalized=replace(replace(lower(REGEXP_REPLACE((COALESCE(snamecitizen, '') || COALESCE(fnamecitizen, '') || COALESCE(mnamecitizen, '')), '[^\u0410-\u044f () ]', '','g')), 'ё', 'е'), 'й', 'и');