package org.lebastudios.theroundtable.plugins;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.events.Event;
import org.lebastudios.theroundtable.events.IEventMethod;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.server.requests.Plugins;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.IconTextButton;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.LoadingPaneController;

import java.io.File;

public class PluginLabelController extends PaneController<PluginLabelController>
{
    private static final Event onReloadLabelsRequest = new Event();

    @FXML public IconView pluginIcon;
    @FXML public Label pluginName;
    @FXML public Label pluginDescription;
    @FXML public IconButton unistallButton;
    @FXML public IconTextButton notInstallableButton;
    @FXML public Button installButton;
    @FXML public Button restartAppButton;
    @FXML public Button updatePlugin;
    @FXML public HBox root;
    private final PluginData pluginData;

    private final Node loadingNode = new LoadingPaneController().getRoot();
    private final IEventMethod onReloadLabelsListener = () -> Platform.runLater(this::updateView);

    public PluginLabelController(PluginData pluginData)
    {
        this.pluginData = pluginData;

        onReloadLabelsRequest.addWeakListener(onReloadLabelsListener);
    }

    @FXML
    @Override
    protected void initialize()
    {
        pluginIcon.setIconName(pluginData.pluginIcon + ".png");
        pluginName.setText(pluginData.pluginName);
        pluginDescription.setText(pluginData.pluginDescription);

        Tooltip tooltip = new Tooltip(LangFileLoader.getTranslation("phrase.dependenciesnotsatisfied"));
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(notInstallableButton, tooltip);

        updateView();
    }

    private void updateView()
    {
        root.getChildren().remove(installButton);
        root.getChildren().remove(unistallButton);
        root.getChildren().remove(restartAppButton);
        root.getChildren().remove(updatePlugin);
        root.getChildren().remove(notInstallableButton);
        root.getChildren().remove(loadingNode);

        if (PluginsManager.getInstance().getPluginsRestartPending().containsKey(pluginData.pluginId))
        {
            root.getChildren().add(restartAppButton);
            return;
        }

        if (PluginsManager.getInstance().isPluginInstalled(pluginData))
        {
            root.getChildren().add(unistallButton);
            root.getChildren().add(loadingNode);

            new Thread(() ->
            {
                if (Plugins.needsUpdate(pluginData))
                {
                    PluginData newVersionData = Plugins.getAvailablePluginData(pluginData.pluginId);

                    if (newVersionData == null)
                    {
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                "Could not get pluginData for plugin " + pluginData.pluginId
                        );
                        // TODO: Show error image
                        return;
                    }
                    else
                    {
                        Platform.runLater(() -> root.getChildren().add(newVersionData.areDependenciesInstalled()
                                ? updatePlugin
                                : notInstallableButton
                        ));
                    }
                }

                Platform.runLater(() -> root.getChildren().remove(loadingNode));
            }).start();
            return;
        }

        root.getChildren().add(
                this.pluginData.areDependenciesInstalled()
                        ? installButton
                        : notInstallableButton
        );
    }

    @FXML
    public void installPlugin(ActionEvent actionEvent)
    {
        root.getChildren().remove(installButton);
        root.getChildren().add(loadingNode);

        installPluginAsync();
    }

    @FXML
    public void updatePlugin(ActionEvent actionEvent)
    {
        root.getChildren().remove(updatePlugin);
        root.getChildren().add(loadingNode);

        installPluginAsync();
    }

    private void installPluginAsync()
    {
        new Thread(() -> Plugins.install(pluginData, () ->
        {
            PluginsManager.getInstance().getPluginsRestartPending().put(pluginData.pluginId, pluginData);
            onReloadLabelsRequest.invoke();
        })).start();
    }


    @FXML
    public void tryUninstallPlugin(ActionEvent actionEvent)
    {
        if (!pluginData.isDependencyOfOther())
        {
            new ConfirmationTextDialogController(LangFileLoader.getTranslation("phrase.pluginsuninstall"), result ->
            {
                if (!result) return;

                unistallPlugin();
            }).instantiate();
            return;
        }

        new ConfirmationTextDialogController(LangFileLoader.getTranslation("phrase.otherplugindepends"), result ->
        {
            if (!result) return;

            unistallPlugin();
        }).instantiate();
    }

    private void unistallPlugin()
    {
        var pluginFile = new File(new PluginsConfigData().load().pluginsFolder + pluginData.pluginId + ".jar");

        if (!pluginFile.exists() || !pluginFile.isFile())
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "Plugin does not exist: " + pluginFile);
            return;
        }

        if (pluginFile.delete())
        {
            root.getChildren().remove(unistallButton);
            root.getChildren().add(loadingNode);

            PluginsManager.getInstance().uninstallPlugin(pluginData);

            Platform.runLater(() -> MainStageController.getInstance().requestRestart());
            onReloadLabelsRequest.invoke();
        }
        else
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "Plugin could not be deleted: " + pluginFile);
        }
    }

    @FXML
    public void restartApp(ActionEvent actionEvent)
    {
        Launcher.restartAplication();
    }
}
