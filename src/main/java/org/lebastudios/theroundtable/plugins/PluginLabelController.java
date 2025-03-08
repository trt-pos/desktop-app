package org.lebastudios.theroundtable.plugins;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.communications.ApiRequests;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.PluginsConfigData;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.events.Event;
import org.lebastudios.theroundtable.events.IEventMethod;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.pluginData.PluginData;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.IconTextButton;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.LoadingPaneController;

import java.io.File;

public class PluginLabelController extends PaneController<PluginLabelController>
{
    private static final Event onReloadLabelsRequest = new Event();

    @FXML private IconView pluginIcon;
    @FXML private Label pluginName;
    @FXML private Label pluginDescription;
    @FXML private IconButton unistallButton;
    @FXML private IconTextButton notInstallableButton;
    @FXML private Button installButton;
    @FXML private Button restartAppButton;
    @FXML private Button updatePlugin;
    @FXML private HBox root;
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

        if (PluginLoader.getPluginsRestartPending().containsKey(pluginData.pluginId))
        {
            root.getChildren().add(restartAppButton);
            return;
        }

        if (PluginLoader.isPluginInstalled(pluginData))
        {
            root.getChildren().add(unistallButton);
            root.getChildren().add(loadingNode);

            new Thread(() ->
            {
                if (ApiRequests.pluginNeedUpdate(pluginData))
                {
                    PluginData newVersionData = ApiRequests.getServerPluginData(pluginData.pluginId);

                    Platform.runLater(() ->
                    {
                        root.getChildren().add(PluginUpdater.areDependenciesInstalled(newVersionData)
                                ? updatePlugin
                                : notInstallableButton
                        );
                    });
                }

                Platform.runLater(() -> root.getChildren().remove(loadingNode));
            }).start();
            return;
        }

        root.getChildren().add(
                PluginUpdater.areDependenciesInstalled(this.pluginData)
                        ? installButton
                        : notInstallableButton
        );
    }

    @FXML
    private void installPlugin()
    {
        root.getChildren().remove(installButton);
        root.getChildren().add(loadingNode);

        updatePluginAsync();
    }

    @FXML
    private void updatePlugin()
    {
        root.getChildren().remove(updatePlugin);
        root.getChildren().add(loadingNode);

        updatePluginAsync();
    }

    private void updatePluginAsync()
    {
        new Thread(() -> ApiRequests.updatePlugin(pluginData, () ->
        {
            PluginLoader.getPluginsRestartPending().put(pluginData.pluginId, pluginData);
            onReloadLabelsRequest.invoke();
        })).start();
    }


    @FXML
    private void tryUninstallPlugin()
    {
        if (!PluginLoader.isDependencyOfOther(pluginData))
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
        var pluginFile = new File(
                new JSONFile<>(PluginsConfigData.class).get().pluginsFolder + pluginData.pluginId + ".jar"
        );

        if (!pluginFile.exists() || !pluginFile.isFile())
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "Plugin does not exist: " + pluginFile);
            return;
        }

        if (pluginFile.delete())
        {
            root.getChildren().remove(unistallButton);
            root.getChildren().add(loadingNode);
            
            PluginLoader.uninstallPlugin(pluginData);

            Platform.runLater(() -> MainStageController.getInstance().requestRestart());
            onReloadLabelsRequest.invoke();
        }
        else
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "Plugin could not be deleted: " + pluginFile);
        }
    }

    @FXML
    private void restartApp()
    {
        Launcher.restartAplication();
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }
}
