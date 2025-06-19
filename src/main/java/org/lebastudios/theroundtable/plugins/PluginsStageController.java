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
import org.lebastudios.theroundtable.components.SearchBox;
import org.lebastudios.theroundtable.config.PluginsConfigData;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.components.IconTextButton;
import org.lebastudios.theroundtable.components.LazyTab;
import org.lebastudios.theroundtable.components.StageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PluginsStageController extends StageController<PluginsStageController>
{
    @FXML public TabPane tabPane;
    @FXML public SearchBox searchBox;
    @FXML public ChoiceBox<String> categoryChoiceBox;
    @FXML public HBox centerBox;

    @FXML
    @Override
    protected void initialize()
    {
        this.instantiateInstalledPlugins();
        this.instantiateAvailablePlugins();

        categoryChoiceBox.getItems().add("");
        
        showPluginViewer(CorePlugin.getInstance().intoPluginObject());
    }

    private void instantiateInstalledPlugins()
    {
        Tab pluginTab = new PluginTabGenerator().generatePluginLazyTab(
                "Installed",
                () -> PluginsManager.getInstance().getInstalledPlugins().stream()
                        .map(IPlugin::intoPluginObject)
                        .toList()
        );

        tabPane.getTabs().addFirst(pluginTab);
    }

    private void instantiateAvailablePlugins()
    {
        Tab pluginTab = new PluginTabGenerator().generatePluginLazyTab(
                "Search",
                () ->
                {
                    List<Plugin> pluginDataList = new ArrayList<>();

                    for (String repo : new PluginsConfigData().getAllRepos())
                    {
                        PluginRepoIntrospector introspector = new PluginRepoIntrospector(repo);
                        PluginRepoData metadata = introspector.intoMetadata();

                        List<PluginData> pluginsData = introspector.getAllPluginData(new String[]{}, "", "").pluginsData;

                        List<Plugin> plugins = pluginsData.stream()
                                .map(data -> new Plugin(data, metadata, null))
                                .toList();

                        pluginDataList.addAll(plugins);
                    }

                    return pluginDataList;
                }
        );

        tabPane.getTabs().add(pluginTab);
    }

    private void showPluginViewer(Plugin plugin)
    {
        if (centerBox.getChildren().size() > 1) centerBox.getChildren().removeLast();

        centerBox.getChildren().add(new PluginViewerPaneController(plugin).getRoot());
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
        public Tab generatePluginLazyTab(String title, Supplier<List<Plugin>> pluginsSupplier)
        {
            LazyTab lazyTab = new LazyTab(title, () ->
            {
                List<Plugin> plugins = pluginsSupplier.get();

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
                        List<Plugin> pluginsReloaded = pluginsSupplier.get();

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

        private Node generatePluginTabContent(List<Plugin> plugins)
        {
            ScrollPane content = new ScrollPane();
            content.setPannable(true);
            content.setFitToHeight(true);
            content.setFitToWidth(true);

            VBox list = new VBox();
            list.setSpacing(5);
            list.setPadding(new Insets(15, 0, 0, 0));

            for (var plugin : plugins)
            {
                Node pluginLabel = new PluginLabelController(plugin).getRoot();
                pluginLabel.setOnMouseClicked(_ -> PluginsStageController.this.showPluginViewer(plugin));
                list.getChildren().add(pluginLabel);
            }

            content.setContent(list);

            return content;
        }
    }
}
