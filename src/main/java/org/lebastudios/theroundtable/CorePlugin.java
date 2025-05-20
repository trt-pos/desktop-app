package org.lebastudios.theroundtable;

import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.config.*;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.entities.AppInstallation;
import org.lebastudios.theroundtable.entities.Plugin;
import org.lebastudios.theroundtable.env.Variables;
import org.lebastudios.theroundtable.fxml2java.CompileFxml;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsStageController;
import org.lebastudios.theroundtable.remotecontrol.RemoteControlPaneController;
import org.lebastudios.theroundtable.components.IconButton;
import org.lebastudios.theroundtable.components.IconView;
import org.lebastudios.theroundtable.components.LabeledIconButton;

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
                "org/lebastudios/theroundtable/remotecontrol",
                "org/lebastudios/theroundtable/components",
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

    private static final int DB_VERSION = 2;

    private CorePlugin() {}

    @Override
    public void initialize()
    {
    }

    @Override
    public TreeItem<SettingsItem> getSettingsRootTreeItem()
    {
        var generalConfigSection = new TreeItem<>(new SettingsItem(Translator.getInstance().t("word.general"),
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
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new EstablishmentConfigPaneController()))
            );
            generalConfigSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new PrintersConfigPaneController()))
            );
        }

        generalConfigSection.getChildren().add(
                new TreeItem<>(new SettingsItem(new PreferencesConfigPaneController()))
        );

        if (AccountManager.getInstance().isAccountAdmin())
        {
            var administrationSection = new TreeItem<>(
                    new SettingsItem(
                            Translator.getInstance().t("core.settings.section.administration"),
                            "admin-user.png")
            );

            administrationSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new DatabaseConfigPaneController()))
            );
            administrationSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new CamelotServerConfigPaneController()))
            );
            administrationSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new PluginsConfigPaneController()))
            );
            administrationSection.getChildren().add(
                    new TreeItem<>(new SettingsItem(new UpdatesConfigPaneController()))
            );

            generalConfigSection.getChildren().add(administrationSection);
        }

        if (Variables.isDev())
        {
            var developerSection = new TreeItem<>(new SettingsItem("Developer", "settings.png"));


            generalConfigSection.getChildren().add(developerSection);
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
    public List<LabeledIconButton> getHomeButtons()
    {
        List<LabeledIconButton> buttons = new ArrayList<>();

        if (AccountManager.getInstance().isAccountAdmin())
        {
            buttons.add(new LabeledIconButton(
                    Translator.getInstance().t("core.homebuttons.remotecontrol"),
                    "control-pane.png",
                    _ -> MainStageController.getInstance().setCentralNode(new RemoteControlPaneController())
            ));
        }

        return buttons;
    }

    @Override
    public List<Class<?>> getPluginEntities()
    {
        List<Class<?>> entities = new ArrayList<>();

        entities.add(Account.class);
        entities.add(Plugin.class);
        entities.add(AppInstallation.class);

        return entities;
    }

    @Override
    public int getDatabaseVersion()
    {
        return DB_VERSION;
    }
}
