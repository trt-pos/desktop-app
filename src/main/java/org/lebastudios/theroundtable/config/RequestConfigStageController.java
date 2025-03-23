package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import lombok.NonNull;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.ui.StageBuilder;

public class RequestConfigStageController extends StageController<RequestConfigStageController>
{
    // TODO: Improve this stage. ConfigPane also has 
    //  to provide a pane name and icon that will be used 
    //  in the title bar and in the settings tre view.
    private ConfigPaneController<?> configPaneController;

    public RequestConfigStageController(@NonNull ConfigPaneController<?> configPaneController)
    {
        this.configPaneController = configPaneController;
    }

    @Override
    @FXML
    protected void initialize()
    {
        ((VBox) getRoot()).getChildren().addFirst(configPaneController.getRoot());
        configPaneController = configPaneController.getController();
    }

    @FXML
    public void accept()
    {
        configPaneController.accept();
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.APPLICATION_MODAL)
                .setStageStyle(StageStyle.UNDECORATED)
                .setResizeable(true);
    }

    @Override
    public String getTitle()
    {
        return "";
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }
}
