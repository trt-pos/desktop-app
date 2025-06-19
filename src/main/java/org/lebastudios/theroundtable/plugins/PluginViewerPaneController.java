package org.lebastudios.theroundtable.plugins;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.controllers.PaneController;

public class PluginViewerPaneController extends PaneController<PluginViewerPaneController>
{
    private final Plugin plugin;
    @FXML public Label pluginVersionLabel;
    @FXML public Label pluginVendorLabel;
    @FXML public Label pluginVendorUrlLabel;
    @FXML public Label pluginRequiredCoreVersionLabel;
    @FXML public VBox dependenciesPane;
    @FXML public HBox iconViewContainer;
    @FXML public Label pluginNameLabel;
    @FXML public Label pluginIdLabel;
    @FXML public Label pluginDescriptionLabel;
    @FXML public TitledPane dependenciesPaneContainer;

    public PluginViewerPaneController(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    protected void initialize()
    {
        PluginData pluginData  = plugin.data();
        
        ImageView iconView = new ImageView(plugin.getPluginIcon());
        iconView.setPreserveRatio(true);
        iconView.setFitHeight(100);
        iconView.setFitWidth(100);
        
        iconViewContainer.getChildren().add(iconView);
        pluginNameLabel.setText(pluginData.name);
        pluginIdLabel.setText(pluginData.id);
        pluginDescriptionLabel.setText(pluginData.description);
        pluginVendorLabel.setText(pluginData.vendor);
        pluginVendorUrlLabel.setText(pluginData.vendorUrl);
        pluginRequiredCoreVersionLabel.setText(pluginData.requiredCoreVersion());
        pluginVersionLabel.setText(pluginData.version);
        
        if (pluginData.dependencies == null || pluginData.dependencies.length < 2)
        {
            ((VBox) getRoot()).getChildren().remove(dependenciesPaneContainer);
        }
        else
        {
            for (var dependency : pluginData.dependencies)
            {
                if (dependency.id.equals(CorePlugin.getInstance().getPluginData().id)) continue;
                dependenciesPane.getChildren().add(createDependencyNode(dependency));
            }
        }
    }

    private Node createDependencyNode(PluginDependencyData dependency)
    {
        final var dependencyLabel = new Label(dependency.id + " " + dependency.version);
        dependencyLabel.getStyleClass().add("dependency-label");
        return dependencyLabel;
    }
    
    
}
