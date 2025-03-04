package org.lebastudios.theroundtable.plugins;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.communications.ApiRequests;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.plugins.pluginData.PluginData;
import org.lebastudios.theroundtable.ui.IconView;
import org.lebastudios.theroundtable.ui.LazyTab;
import org.lebastudios.theroundtable.ui.StageBuilder;

public class PluginsStageController extends StageController<PluginsStageController>
{
    @FXML private TabPane tabPane;

    @FXML
    @Override
    protected void initialize()
    {
        this.instantiateInstalledPlugins();
        this.instantiateAvailablePlugins();

        tabPane.getSelectionModel().selectedItemProperty()
                .addListener((_, _, _) -> tabPane.getScene().getWindow().sizeToScene());
    }

    private void instantiateInstalledPlugins()
    {
        Tab pluginTab = new PluginTabGenerator().generatePluginLazyTab("Installed",
                PluginLoader.getInstalledPlugins().stream().map(IPlugin::getPluginData).toArray(PluginData[]::new));

        tabPane.getTabs().addFirst(pluginTab);
    }

    private void instantiateAvailablePlugins()
    {
        Tab pluginTab = new PluginTabGenerator().generatePluginLazyTab("Search", ApiRequests.getPluginsDataAvailable());

        tabPane.getTabs().add(pluginTab);
    }

    private void showPluginViewer(PluginData pluginData)
    {
        final var root = (HBox) getRoot();

        if (root.getChildren().size() > 1) root.getChildren().removeLast();

        root.getChildren().add(new PluginViewerPaneController(pluginData).getRoot());
        getStage().sizeToScene();
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

    @Override
    public boolean hasFXMLControllerDefined()
    {
        return true;
    }

    @Override
    public String getTitle()
    {
        return "Plugins";
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.APPLICATION_MODAL).setResizeable(true);
    }

    private class PluginTabGenerator
    {
        public Tab generatePluginLazyTab(String title, PluginData... plugins)
        {
            LazyTab lazyTab = new LazyTab(title, () ->
            {
                ScrollPane content = new ScrollPane();
                content.setPannable(true);
                content.setFitToHeight(true);
                content.setFitToWidth(true);

                VBox list = new VBox();
                list.setSpacing(5);
                list.setPadding(new Insets(15, 0, 0, 0));

                var pluginsData = ApiRequests.getPluginsDataAvailable();

                if (pluginsData == null)
                {
                    return new IconView("error.png");
                }

                for (var pluginData : plugins)
                {
                    Node pluginLabel = new PluginLabelController(pluginData).getRoot();
                    pluginLabel.setOnMouseClicked(_ -> PluginsStageController.this.showPluginViewer(pluginData));
                    list.getChildren().add(pluginLabel);
                }

                content.setContent(list);

                return content;
            });

            lazyTab.setDropNodeOnDeselect(true);

            return lazyTab;
        }
    }
}
