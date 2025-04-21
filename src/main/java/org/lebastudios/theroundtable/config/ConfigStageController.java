package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.LogoPaneController;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.events.AccountEvents;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginsManager;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ConfigStageController extends StageController<ConfigStageController>
{
    public static Stage configStage;
    @Getter private static ConfigStageController instance;

    static
    {
        AccountEvents.OnAccountLogOutBefore.addListener(_ -> configStage = null);
    }
    
    @FXML public TreeView<SettingsItem> configSectionsTreeView;
    @FXML public ScrollPane mainPane;
    @FXML public Label errorLabel;

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
        configSectionsTreeView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) ->
        {
            if (newValue == null || newValue.getValue() == null) return;

            ConfigPaneController<?> controller = newValue.getValue().settingPane();
            
            if (controller == null) return;
            
            final var root = controller.getRoot();

            mainPane.setContent(root);
            currentPaneController = controller.getController();
            currentPaneController.updateUI();
            
            errorLabel.setText("");
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

        List<TreeItem<SettingsItem>> rootTrees =  PluginsManager.getInstance().getSettingsTreeViews();
        Collections.reverse(rootTrees);
        
        configSectionsTreeView.getRoot().getChildren().addAll(rootTrees);
        
        mainPane.setContent(new LogoPaneController().getRoot());
    }
    
    @FXML
    public void apply(ActionEvent actionEvent)
    {
        if (currentPaneController == null)
        {
            return;
        }

        ConfigPaneController.ValidationResult validationResult = currentPaneController.apply();

        if (validationResult.success())
        {
            errorLabel.setText("");
        }
        else
        {
            errorLabel.setText(validationResult.message());
        }
    }

    @FXML
    public void cancel(ActionEvent actionEvent)
    {
        if (currentPaneController == null)
        {
            close();
            return;
        }
        
        errorLabel.setText("");
        currentPaneController.cancel();
        this.close();
    }

    @FXML
    public void accept(ActionEvent actionEvent)
    {
        if (currentPaneController == null)
        {
            cancel(actionEvent);
            return;
        }

        ConfigPaneController.ValidationResult validationResult = currentPaneController.apply();
        
        if (validationResult.success()) 
        {
            errorLabel.setText("");
            this.close();
        }
        else
        {
            errorLabel.setText(validationResult.message());
        }
    }

    @Override
    public String getTitle()
    {
        return LangFileLoader.getTranslation("title.settingsstage");
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(true);
    }
}
