create table core_app_installation
(
    uuid            char(45)     not null,
    name            varchar(255) not null default 'Unknown',
    version         varchar(255) not null,
    created_at      timestamp    not null,
    updated_at      timestamp    not null,
    status          varchar(11)  not null,
    last_account_id integer      null,
    constraint PK_CORE_APP_INSTALLATION primary key (uuid),
    constraint CK_CORE_APP_INSTALLATION_STATUS check (status in ('ACTIVE', 'INACTIVE', 'DISABLED', 'STANDBY')),
    constraint FK_CORE_APP_INSTALLATION_CORE_ACCOUNT foreign key (last_account_id) references core_account (id)
        on delete set null
)