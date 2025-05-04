package org.lebastudios.theroundtable.plugins;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.communications.Version;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.dialogs.InformationTextDialogController;
import org.lebastudios.theroundtable.events.Event;
import org.lebastudios.theroundtable.events.IEventMethod;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.IconTextButton;
import org.lebastudios.theroundtable.ui.LoadingPaneController;

import java.io.File;

public class PluginLabelController extends PaneController<PluginLabelController>
{
    private static final Event onReloadLabelsRequest = new Event();

    @FXML public ImageView pluginIcon;
    @FXML public Label pluginName;
    @FXML public Label pluginDescription;
    @FXML public Label pluginRepo;
    @FXML public IconButton unistallButton;
    @FXML public IconTextButton notInstallableButton;
    @FXML public Button installButton;
    @FXML public Button restartAppButton;
    @FXML public Button updatePlugin;
    
    private final Plugin plugin;

    private HBox rootVBox;

    private final Node loadingNode = new LoadingPaneController().getRoot();
    private final IEventMethod onReloadLabelsListener = () -> Platform.runLater(this::updateView);

    public PluginLabelController(Plugin plugin)
    {
        this.plugin = plugin;

        onReloadLabelsRequest.addWeakListener(onReloadLabelsListener);
    }

    @FXML
    @Override
    protected void initialize()
    {
        PluginData pluginData = plugin.data();
        PluginRepoData data = plugin.repoData();
        
        rootVBox = (HBox) root;
        
        pluginIcon.setImage(plugin.getPluginIcon());

        pluginIcon.setPreserveRatio(true);
        pluginIcon.setFitHeight(35);
        pluginIcon.setFitWidth(35);

        pluginName.setText(pluginData.pluginName);
        pluginDescription.setText(pluginData.pluginDescription);
        pluginRepo.setText(data == null ? "Built-in" : data.url);

        Tooltip tooltip = new Tooltip(LangFileLoader.getTranslation("phrase.dependenciesnotsatisfied"));
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(notInstallableButton, tooltip);

        updateView();
    }

    private void updateView()
    {
        PluginData pluginData = plugin.data();
        
        rootVBox.getChildren().remove(installButton);
        rootVBox.getChildren().remove(unistallButton);
        rootVBox.getChildren().remove(restartAppButton);
        rootVBox.getChildren().remove(updatePlugin);
        rootVBox.getChildren().remove(notInstallableButton);
        rootVBox.getChildren().remove(loadingNode);

        if (pluginData.pluginId.equals(CorePlugin.getInstance().getPluginData().pluginId)) return;
        
        PluginRepoIntrospector repo = plugin.repoData().intoIntrospector();

        if (PluginsManager.getInstance().getPluginsRestartPending().containsKey(pluginData.pluginId))
        {
            rootVBox.getChildren().add(restartAppButton);
            return;
        }

        if (PluginsManager.getInstance().isPluginInstalled(pluginData))
        {
            rootVBox.getChildren().add(unistallButton);
            rootVBox.getChildren().add(loadingNode);

            new Thread(() ->
            {
                if (repo.needsUpdate(pluginData.pluginId, new Version(pluginData.pluginVersion)))
                {
                    PluginData newVersionData = repo.getPluginData(pluginData.pluginId);

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
                        Platform.runLater(() -> rootVBox.getChildren().add(newVersionData.areDependenciesInstalled()
                                ? updatePlugin
                                : notInstallableButton
                        ));
                    }
                }

                Platform.runLater(() -> rootVBox.getChildren().remove(loadingNode));
            }).start();
            return;
        }

        rootVBox.getChildren().add(
                pluginData.areDependenciesInstalled()
                        ? installButton
                        : notInstallableButton
        );
    }

    @FXML
    public void installPlugin(ActionEvent actionEvent)
    {
        rootVBox.getChildren().remove(installButton);
        rootVBox.getChildren().add(loadingNode);

        installPluginAsync();
    }

    @FXML
    public void updatePlugin(ActionEvent actionEvent)
    {
        rootVBox.getChildren().remove(updatePlugin);
        rootVBox.getChildren().add(loadingNode);

        installPluginAsync();
    }

    private void installPluginAsync()
    {
        PluginData pluginData = plugin.data();
        PluginRepoIntrospector repo = plugin.repoData().intoIntrospector();
        
        new Thread(() -> repo.install(pluginData.pluginId, () ->
        {
            PluginsManager.getInstance().getPluginsRestartPending()
                    .put(pluginData.pluginId, pluginData);
            onReloadLabelsRequest.invoke();
        })).start();
    }


    @FXML
    public void tryUninstallPlugin(ActionEvent actionEvent)
    {
        PluginData pluginData = plugin.data();
        
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
        PluginData pluginData = plugin.data();
        
        var pluginFile = new File(plugin.getLocalPath());

        if (!pluginFile.exists() || !pluginFile.isFile())
        {
            Logs.getInstance().log(Logs.LogType.WARNING, "Plugin does not exist: " + pluginFile);
            return;
        }

        IPlugin plugin = PluginsManager.getInstance().getPluginsInstalled().get(pluginData.pluginId);
        
        if (plugin != null && plugin.purgeTask() != null)
        {
            new ConfirmationTextDialogController(
                    """
                            This plugin can be purged, this action might remove all data related to it, depending on the \
                            plugin developer implementation. \
                             \
                            Do you want to purge the plugin before uninstalling it?""",
                    accept ->
            {
                if (!accept) return;

                plugin.purgeTask().setOnTaskComplete(result ->
                {
                    if (!result.success()) 
                    {
                        new InformationTextDialogController(
                                "Purge failed:\n\n" + result.message()
                        ).instantiate(true);
                    }
                }).execute(true);
            }).instantiate(true);
        }

        if (pluginFile.delete())
        {
            rootVBox.getChildren().remove(unistallButton);
            rootVBox.getChildren().add(loadingNode);

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
