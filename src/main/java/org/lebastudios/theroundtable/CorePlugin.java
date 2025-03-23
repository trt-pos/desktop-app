package org.lebastudios.theroundtable;

import javafx.scene.control.TreeItem;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.*;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;

public class CorePlugin implements IPlugin
{
    private static CorePlugin instance;
    
    public static CorePlugin getInstance()
    {
        if (instance == null) instance = new CorePlugin();
        
        return instance;
    }
    
    private CorePlugin() {}
    
    @Override
    public void initialize()
    {
        
    }

    @Override
    public TreeItem<SettingsItem> getSettingsRootTreeItem()
    {
        var generalConfigSection = new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.general"),
                "settings.png", null));
        generalConfigSection.setExpanded(true);

        if (AccountManager.getInstance().isAccountAdmin())
        {
            // generalConfigSection.getChildren().add(
            //         new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.account"),
            //                 "user.png", new AccountConfigPaneController()))
            // );

            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.users"),
                            "users.png", new UsersConfigPaneController()))
            );
        }

        generalConfigSection.getChildren().add(
                new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.preferences"),
                        "preferences.png", new PreferencesConfigPaneController()))
        );

        if (AccountManager.getInstance().isAccountAdmin())
        {
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.establishment"),
                            "establishment.png", new EstablishmentConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.printers"),
                            "printer.png", new PrintersConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(LangFileLoader.getTranslation("word.database"),
                            "database.png", new DatabaseConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem("Camelot",
                            "server.png", new CamelotServerConfigPaneController()))
            );
        }

        return generalConfigSection;
    }
}
