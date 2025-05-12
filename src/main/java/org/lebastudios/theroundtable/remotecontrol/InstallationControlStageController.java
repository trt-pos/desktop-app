package org.lebastudios.theroundtable.remotecontrol;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.camelot.converters.StringConverter;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.database.entities.AppInstallation;
import org.lebastudios.theroundtable.ui.StageBuilder;

public class InstallationControlStageController extends StageController<InstallationControlStageController>
{
    private final AppInstallation installation;
    public Label nameLabel;
    public Label uuidLabel;

    public InstallationControlStageController(AppInstallation installation)
    {
        this.installation = installation;
    }

    @Override
    protected void initialize()
    {
        nameLabel.setText(installation.getName());
        uuidLabel.setText(installation.getUuid());
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(false);
    }

    @FXML
    public void closeAppInstallationAction(ActionEvent actionEvent)
    {
        RemoteControlEvents.SHUTDOWN_APP_EVENT.invoke(new StringConverter(installation.getUuid()));
    }

    @FXML
    public void disableAppInstallationAction(ActionEvent actionEvent) 
    {
        RemoteControlEvents.DISABLE_APP_EVENT.invoke(new StringConverter(installation.getUuid()));
    }

    @FXML
    public void syncPluginsAppInstallationAction(ActionEvent actionEvent)
    {
        RemoteControlEvents.SYNC_PLUGINS_EVENT.invoke(new StringConverter(installation.getUuid()));
    }

    @FXML
    public void restartAppInstallationAction(ActionEvent actionEvent)
    {
        RemoteControlEvents.RESTART_APP_EVENT.invoke(new StringConverter(installation.getUuid()));
    }

    public void editAppInstallationAction(ActionEvent actionEvent)
    {
        
    }

    @Override
    public String getTitle()
    {
        return "Remote control - " + installation.getName();
    }
}
