package org.lebastudios.theroundtable.database;

import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.sql.Connection;

/// # Database Updater
/// The Database Updater interface is a piece of software that allows the maintainance of a multi dbms sytem with ease.
/// It provides a way to update the database schema and data using a set of strategies and macros that we will explain
/// as we go along.
///
/// ## Supported ways to update the database
///
/// ## SQL Files Macros
/// When writting the SQL files, you can use the following macros to apply transformations to the SQL that make it
/// compatible with all the supported DBMS.
///
/// ### Autoincrement
/// When creating a table, you can use the `AUTOINCREMENT|autoincrement` keyword to define a column as autoincrement.
/// The keyword will be replaced by the correct keyword for the DBMS.
///
/// Note that for sqlite, the column must be the primary key and the constraint should be defined inline. For example:
/// ```sql
/// create table my_table (
///    id integer primary key autoincrement,
///    name varchar(255)
///);
///```
///
/// ### Migrator
/// As many people know, SQLite does not support the `ALTER TABLE` statement as
/// other DBMS do. So, in order to alter a table that SQLITE does not support, we
/// need to create a new table and copy the data from the old table to the new
/// table, then drop the old and rename the new one.
///
/// This is too much boilerplate code, so we created a macro that will do this for you.
/// ```sql
/// -- MIGRATE
/// create table my_table (
///     id integer primary key autoincrement,
///     name varchar(255),
///     age integer,
///     -- NEW
///     new_column varchar(255),
///     constraint unique_name unique(name)
///);
///```
///
/// The macro creates a table as the defined one in the example above with the
/// name ended with `_tmp`. Then copies the columns that are above the `-- NEW`,
/// drops the old table and renames the new one to `my_table`.
///
/// Note: This macro doesn't rename existing columns and cannot convert
/// beetwen types. It only copies the columns that are above the `-- NEW`
/// flag. Note that you can delete one column just by ignoring it.
///
/// ### Conditional DBMS
/// Sometimes we need to execute a SQL statement only for a specific DBMS but
/// the rest of the SQL is compatible with all the DBMS. For this, we can use the
/// `-- IF <DBMS>` macro.
///
/// ```sql
/// -- IF SQLITE
/// create trigger core_app_installation_ensure_single_master_before_update
///     before update
///     on core_app_installation
///     for each row
///     when new.is_master = 1 and old.is_master <> 1
/// begin
///     select raise(abort, 'only one row can have is_active = 1')
///     where exists (select 1 from core_app_installation where is_master = 1 and uuid != old.uuid);
/// end;
/// -- ENDIF
///
/// -- IF MARIADB
/// create trigger core_app_installation_ensure_single_master_before_update
///     before update
///     on core_app_installation
///     for each row
/// begin
///     if new.is_master = 1 and old.is_master <> 1 then
///         if (select count(*) from core_app_installation where is_master = 1 and uuid != old.uuid) > 0 then
///             signal sqlstate '45000'
///                 set message_text = 'only one row can have is_master = 1';
///         end if;
///     end if;
/// end;
/// -- ENDIF
///```
public interface IDatabaseUpdater
{
    default int getDatabaseVersion() { return 0; }

    default void upgradeDatabaseTo(Connection conn, int version, Dbms dbms) throws Exception
    {
        Class<? extends IPlugin> clazz =
                PluginsManager.getInstance().getPluginOf(this.getClass()).orElseThrow().getClass();
        IPlugin plugin = PluginsManager.getInstance().getPluginOf(clazz).orElseThrow();

        for (IUpdateStrategy strategy : IUpdateStrategy.STRATEGIES)
        {
            if (strategy.upgradeTo(conn, version, plugin, dbms))
            {
                return;
            }
        }

        throw new Exception("No update strategy found when upgrading db to version " + version);
    }

    default void downgradeDatabaseTo(Connection conn, int version, Dbms dbms) throws Exception
    {
        Class<? extends IPlugin> clazz =
                PluginsManager.getInstance().getPluginOf(this.getClass()).orElseThrow().getClass();
        IPlugin plugin = PluginsManager.getInstance().getPluginOf(clazz).orElseThrow();

        for (IUpdateStrategy strategy : IUpdateStrategy.STRATEGIES)
        {
            if (strategy.downgradeTo(conn, version, plugin, dbms))
            {
                return;
            }
        }

        throw new Exception("No update strategy found when downgrading db to version " + version);
    }
}
