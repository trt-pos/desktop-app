package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import lombok.NonNull;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.components.StageBuilder;
import org.lebastudios.theroundtable.components.TitleBuilder;

public class RequestConfigStageController extends StageController<RequestConfigStageController>
{
    @FXML public ScrollPane paneContainer;
    @FXML public Label errorLabel;

    private ConfigPaneController<?> configPaneController;
    
    private String title;
    private String iconName;

    public RequestConfigStageController(@NonNull ConfigPaneController<?> configPaneController)
    {
        this.configPaneController = configPaneController;
        
        this.title = configPaneController.getTitle();
        this.iconName = configPaneController.getIconName();
    }

    @Override
    @FXML
    protected void initialize()
    {
        ((VBox) getRoot()).getChildren().addFirst(
                new TitleBuilder()
                        .setText(title)
                        .setIconName(iconName)
                        .build()
        );
        
        paneContainer.setContent(configPaneController.getRoot());
        configPaneController = configPaneController.getController();
    }

    @FXML
    public void accept(ActionEvent actionEvent)
    {
        ConfigPaneController.ValidationResult validationResult = configPaneController.apply();
        
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

    public RequestConfigStageController setTitle(String title)
    {
        this.title = title;
        return this;
    }
    
    public RequestConfigStageController setIconName(String iconName)
    {
        this.iconName = iconName;
        return this;
    }
    
    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.APPLICATION_MODAL)
                .setStageStyle(StageStyle.UNDECORATED)
                .setIconName(configPaneController.getIconName())
                .setResizeable(true);
    }

    @Override
    public String getTitle()
    {
        return "";
    }
}
