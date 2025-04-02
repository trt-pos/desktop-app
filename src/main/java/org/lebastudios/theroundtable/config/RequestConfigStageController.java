package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import lombok.NonNull;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.ui.StageBuilder;
import org.lebastudios.theroundtable.ui.TitleBuilder;

public class RequestConfigStageController extends StageController<RequestConfigStageController>
{
    @FXML public ScrollPane paneContainer;
    
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
        configPaneController.accept();
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
