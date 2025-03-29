package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.events.AccountEvents;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.function.Consumer;

public class ConfigStageController extends StageController<ConfigStageController>
{
    public static Stage configStage;
    @Getter private static ConfigStageController instance;

    static
    {
        AccountEvents.OnAccountLogOutBefore.addListener(_ -> configStage = null);
    }
    
    @FXML public Label versionLabel;
    @FXML public TreeView<SettingsItem> configSectionsTreeView;
    @FXML public ScrollPane mainPane;
    
    private ConfigPaneController<?> currentPaneController;

    @Override
    public void instantiate(Consumer<ConfigStageController> acceptController, boolean shouldWait)
    {
        if (configStage != null)
        {
            if (configStage.isShowing())
            {
                Logs.getInstance().log(
                        Logs.LogType.WARNING,
                        "Config stage is already showing"
                );
                return;
            }
            
            configStage.show();
            return;
        }
        
        super.instantiate(acceptController, shouldWait);
        
        configStage = getStage();
        instance = this;
    }

    @SneakyThrows @FXML @Override
    protected void initialize()
    {
        versionLabel.setText("Version: " + TheRoundTableApplication.getAppVersion());
        
        configSectionsTreeView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) ->
        {
            if (newValue == null || newValue.getValue() == null) return;

            ConfigPaneController<?> controller = newValue.getValue().settingPane();
            
            if (controller == null) return;
            
            final var root = controller.getRoot();

            mainPane.setContent(root);
            currentPaneController = controller.getController();
            currentPaneController.updateUI();
        });
        
        configSectionsTreeView.setCellFactory(_ -> new TreeCell<>()
        {
            @Override
            protected void updateItem(SettingsItem item, boolean empty)
            {
                super.updateItem(item, empty);

                if (empty || item == null)
                {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item.value());

                var imageView = new ImageView(ImageLoader.getIcon(item.iconName()));
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                this.setGraphic(imageView);
            }
        });
        
        configSectionsTreeView.getRoot().getChildren().addAll(PluginsManager.getInstance().getSettingsTreeViews());
        
        mainPane.setContent(new FXMLLoader(CorePlugin.class.getResource("defaultCenterPane.fxml")).load());
    }
    
    @FXML
    public void apply(ActionEvent actionEvent)
    {
        if (currentPaneController == null)
        {
            return;
        }

        currentPaneController.apply();
    }

    @FXML
    public void cancel(ActionEvent actionEvent)
    {
        if (currentPaneController == null)
        {
            close();
            return;
        }

        currentPaneController.cancel();
    }

    @FXML
    public void accept(ActionEvent actionEvent)
    {
        if (currentPaneController == null)
        {
            cancel(actionEvent);
            return;
        }

        currentPaneController.accept();
    }

    @Override
    public String getTitle()
    {
        return LangFileLoader.getTranslation("title.settingsstage");
    }

    @Override
    public Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(true)
                .setOwner(MainStageController.getInstance().getStage());
    }
}
