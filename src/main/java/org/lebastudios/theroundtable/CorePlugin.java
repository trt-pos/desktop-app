package org.lebastudios.theroundtable;

import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.camelot.CamelotServiceManager;
import org.lebastudios.theroundtable.config.*;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.database.Dbms;
import org.lebastudios.theroundtable.database.entities.Account;
import org.lebastudios.theroundtable.database.entities.DatabaseVersion;
import org.lebastudios.theroundtable.locale.AppLocale;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.locale.LangLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsStageController;
import org.lebastudios.theroundtable.ui.IconButton;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CorePlugin implements IPlugin
{
    private static CorePlugin instance;

    public static CorePlugin getInstance()
    {
        if (instance == null) instance = new CorePlugin();

        return instance;
    }

    private static final int DB_VERSION = 1;
    
    private CorePlugin() {}

    @Override
    public void initialize() 
    {
        LangLoader.loadLang(CorePlugin.class, AppLocale.getActualLocale());
        CamelotServiceManager.getInstance().initTask().execute(true);
    }

    @Override
    public TreeItem<SettingsItem> getSettingsRootTreeItem()
    {
        var generalConfigSection = new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.general"),
                "settings.png"));
        generalConfigSection.setExpanded(true);

        if (AccountManager.getInstance().isAccountAdmin())
        {
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new AccountConfigPaneController()))
            );

            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new UsersConfigPaneController()))
            );
        }

        generalConfigSection.getChildren().add(
                new TreeItem<>(new SettingsItem(new PreferencesConfigPaneController()))
        );

        if (AccountManager.getInstance().isAccountAdmin())
        {
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new EstablishmentConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new PrintersConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new DatabaseConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new CamelotServerConfigPaneController()))
            );
        }

        return generalConfigSection;
    }

    @Override
    public List<Button> getLeftButtons()
    {
        List<Button> buttons = new ArrayList<>();

        IconButton settingsButton = new IconButton("settings.png");
        settingsButton.setOnAction(_ -> new ConfigStageController()
                .setOwner(MainStageController.getInstance().getStage())
                .instantiate()
        );

        buttons.add(
                settingsButton
        );
        
        if (AccountManager.getInstance().isAccountAdmin())
        {
            IconButton pluginsButton = new IconButton("plugins.png");
            pluginsButton.setOnAction(_ -> new PluginsStageController()
                    .setOwner(MainStageController.getInstance().getStage())
                    .instantiate()
            );
            
            buttons.add(
                    pluginsButton
            );
        }
        
        return buttons;
    }

    @Override
    public List<Class<?>> getPluginEntities()
    {
        List<Class<?>> entities = new ArrayList<>();
        
        entities.add(Account.class);
        entities.add(DatabaseVersion.class);
        
        return entities;
    }

    @Override
    public int getDatabaseVersion()
    {
        return DB_VERSION;
    }

    public void upgradeTo1(Connection conn, Dbms dbms) throws SQLException, IOException
    {
        Statement statement = conn.createStatement();

        statement.addBatch("""
                create table core_account
                (
                    id                             integer,
                    changue_password_on_next_login boolean      not null,
                    name                           varchar(255) not null,
                    password                       varchar(255) not null,
                    type                           varchar(255) not null,
                    constraint ck_account_type check (type in ('ROOT', 'ADMIN', 'MANAGER', 'CASHIER', 'ACCOUNTANT')),
                    constraint pk_account primary key (id)
                );""");

        statement.executeBatch();
    }

    public void downgradeTo0(Connection conn, Dbms dbms) throws SQLException, IOException
    {
        Statement statement = conn.createStatement();

        statement.addBatch("drop table core_account;");

        statement.executeBatch();
    }
}
