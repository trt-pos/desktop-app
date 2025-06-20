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
import org.lebastudios.theroundtable.apparience.ImageManager;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.accounts.AccountEvents;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginsManager;
import org.lebastudios.theroundtable.components.StageBuilder;
import org.lebastudios.theroundtable.rustdesk.TechnicalSupportStageController;

import java.util.List;
import java.util.function.Consumer;

public class ConfigStageController extends StageController<ConfigStageController>
{
    public static Stage configStage;
    @Getter private static ConfigStageController instance;

    static
    {
        AccountEvents.onAccountLogOutBefore.addListener(_ -> configStage = null);
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

            if (!newValue.getChildren().isEmpty()) 
            {
                newValue.setExpanded(!newValue.isExpanded());
            }
            
            ConfigPaneController<?> controller = newValue.getValue().settingPane();
            
            if (controller == null) return;
            
            final var root = controller.getRoot();

            mainPane.setContent(root);
            currentPaneController = controller;
            currentPaneController.updateUI();
            
            errorLabel.setText("");
        });
        
        configSectionsTreeView.setCellFactory(_ -> new TreeCell<>()
        {
            {
                setOnMouseClicked(_ ->
                {
                    if (!isEmpty())
                    {
                        getTreeItem().setExpanded(!getTreeItem().isExpanded());
                    }
                });
            }
            
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

                var imageView = new ImageView(ImageManager.getInstance().get(item.iconName(), ImageManager.ImageType.ICON));
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                this.setGraphic(imageView);
            }
        });

        List<TreeItem<SettingsItem>> rootTrees =  PluginsManager.getInstance().getSettingsTreeViews();
        
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

    @FXML
    public void openTechnicalSupport(ActionEvent actionEvent) 
    {
        new TechnicalSupportStageController().setOwner(this.getStage()).instantiate();
    }

    @Override
    public String getTitle()
    {
        return Translator.getInstance().t("core:title.settingsstage");
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(true);
    }
}
