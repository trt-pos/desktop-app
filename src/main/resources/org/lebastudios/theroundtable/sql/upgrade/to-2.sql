create table core_app_installation
(
    uuid            varchar(45)  not null,
    name            varchar(255) not null default 'Unknown',
    version         varchar(255) not null,
    created_at      timestamp    not null,
    updated_at      timestamp    not null,
    status          varchar(11)  not null,
    last_account_id integer null,
    ip              varchar(15) null,
    is_master       boolean      not null default false,
    constraint PK_CORE_APP_INSTALLATION primary key (uuid),
    constraint CK_CORE_APP_INSTALLATION_STATUS check (status in ('ACTIVE', 'INACTIVE', 'DISABLED', 'STANDBY')),
    constraint CK_CORE_APP_INSTALLATION_IP check (ip like '%.%.%.%'),
    constraint FK_CORE_APP_INSTALLATION_CORE_ACCOUNT foreign key (last_account_id) references core_account (id)
        on delete set null
);

-- DELIMITER

create table core_plugin
(
    id      varchar(255) not null,
    repo    varchar(255) not null,
    version varchar(255) not null,
    constraint PK_CORE_PLUGIN_ID PRIMARY KEY (id),
    constraint CK_CORE_PLUGIN_URL check (repo like '%:%')
);