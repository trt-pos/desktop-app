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
    public void initialize() {}

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
}
