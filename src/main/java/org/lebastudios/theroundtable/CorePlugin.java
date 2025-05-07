package org.lebastudios.theroundtable;

import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.*;
import org.lebastudios.theroundtable.database.entities.Account;
import org.lebastudios.theroundtable.database.entities.DatabaseVersion;
import org.lebastudios.theroundtable.fxml2java.CompileFxml;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsStageController;
import org.lebastudios.theroundtable.ui.IconButton;

import java.util.ArrayList;
import java.util.List;

@CompileFxml(
        directories = {
                "org/lebastudios/theroundtable/accounts",
                "org/lebastudios/theroundtable/config",
                "org/lebastudios/theroundtable/dialogs",
                "org/lebastudios/theroundtable/plugins",
                "org/lebastudios/theroundtable/setup",
                "org/lebastudios/theroundtable/tasks",
                "org/lebastudios/theroundtable/ui",
                "org/lebastudios/theroundtable",
        }
)
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
                    new TreeItem<>(new SettingsItem(new LicenseConfigPaneController()))
            );

            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new UsersConfigPaneController()))
            );
            
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new GlobalPreferencesConfigPaneController()))
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
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new PluginsConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new UpdatesConfigPaneController()))
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
}
