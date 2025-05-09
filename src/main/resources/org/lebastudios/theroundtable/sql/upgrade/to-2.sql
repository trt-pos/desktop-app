create table core_app_installation
(
    uuid            char(45)     not null,
    name            varchar(255) not null default 'Unknown',
    version         varchar(255) not null,
    created_at      timestamp    not null,
    updated_at      timestamp    not null,
    status          varchar(11)  not null,
    last_account_id integer      null,
    ip              varchar(15)  null,
    is_master       boolean      not null default false,
    constraint PK_CORE_APP_INSTALLATION primary key (uuid),
    constraint CK_CORE_APP_INSTALLATION_STATUS check (status in ('ACTIVE', 'INACTIVE', 'DISABLED', 'STANDBY')),
    constraint CK_CORE_APP_INSTALLATION_IP check (ip glob '[0-9]*.[0-9]*.[0-9]*.[0-9]*'),
    constraint FK_CORE_APP_INSTALLATION_CORE_ACCOUNT foreign key (last_account_id) references core_account (id)
        on delete set null
);
-- DELIMITER

-- DELIMITER
create trigger core_app_installation_set_master_if_none_after_insert
    after insert
    on core_app_installation
    for each row
begin
    update core_app_installation
    set is_master = 1
    where uuid = new.uuid
      and (select count(*) from core_app_installation where is_master = 1) = 0;
end;

