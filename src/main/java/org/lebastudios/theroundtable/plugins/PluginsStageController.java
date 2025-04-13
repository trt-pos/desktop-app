package org.lebastudios.theroundtable.plugins;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.server.requests.Plugins;
import org.lebastudios.theroundtable.ui.IconTextButton;
import org.lebastudios.theroundtable.ui.LazyTab;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.function.Supplier;

public class PluginsStageController extends StageController<PluginsStageController>
{
    @FXML public TabPane tabPane;

    @FXML
    @Override
    protected void initialize()
    {
        this.instantiateInstalledPlugins();
        this.instantiateAvailablePlugins();

        showPluginViewer(CorePlugin.getInstance().getPluginData());
    }

    private void instantiateInstalledPlugins()
    {
        Tab pluginTab = new PluginTabGenerator().generatePluginLazyTab(
                "Installed",
                () -> PluginsManager.getInstance().getInstalledPlugins()
                        .stream()
                        .map(IPlugin::getPluginData)
                        .toArray(PluginData[]::new)
        );

        tabPane.getTabs().addFirst(pluginTab);
    }

    private void instantiateAvailablePlugins()
    {
        Tab pluginTab = new PluginTabGenerator().generatePluginLazyTab(
                "Search",
                Plugins::getAllAvailablePluginsData
        );

        tabPane.getTabs().add(pluginTab);
    }

    private void showPluginViewer(PluginData pluginData)
    {
        final var root = (HBox) getRoot();

        if (root.getChildren().size() > 1) root.getChildren().removeLast();

        root.getChildren().add(new PluginViewerPaneController(pluginData).getRoot());
    }

    @Override
    public String getTitle()
    {
        return "Plugins";
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL).setResizeable(true);
    }

    private class PluginTabGenerator
    {
        public Tab generatePluginLazyTab(String title, Supplier<PluginData[]> pluginsSupplier)
        {
            LazyTab lazyTab = new LazyTab(title, () ->
            {
                PluginData[] plugins = pluginsSupplier.get();
                
                if (plugins == null) 
                {
                    VBox content = new VBox();
                    content.setSpacing(10);
                    content.setAlignment(Pos.CENTER);
                    
                    content.getChildren().add(
                            new Label("Plugins couldn't be loaded")
                    );
                    
                    Button reloadButton = new IconTextButton("reload.png");
                    reloadButton.setText("Reload");
                    reloadButton.setOnAction(event -> 
                    {
                        PluginData[] pluginsReloaded = pluginsSupplier.get();
                        
                        if (pluginsReloaded == null) return;

                        content.getChildren().clear();
                        content.getChildren().add(generatePluginTabContent(pluginsReloaded));
                    });
                    
                    content.getChildren().add(reloadButton);
                    return content;
                }
                
                return generatePluginTabContent(plugins);
            });

            lazyTab.setDropNodeOnDeselect(true);

            return lazyTab;
        }
        
        private Node generatePluginTabContent(PluginData[] plugins)
        {
            ScrollPane content = new ScrollPane();
            content.setPannable(true);
            content.setFitToHeight(true);
            content.setFitToWidth(true);

            VBox list = new VBox();
            list.setSpacing(5);
            list.setPadding(new Insets(15, 0, 0, 0));

            for (var pluginData : plugins)
            {
                Node pluginLabel = new PluginLabelController(pluginData).getRoot();
                pluginLabel.setOnMouseClicked(_ -> PluginsStageController.this.showPluginViewer(pluginData));
                list.getChildren().add(pluginLabel);
            }

            content.setContent(list);

            return content;
        }
    }
}
