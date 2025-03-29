package org.lebastudios.theroundtable.dialogs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.net.URL;

public class InformationTextDialogController extends StageController<InformationTextDialogController>
{
    private final String informationText;
    @FXML public Label textLabel;
    
    public InformationTextDialogController(String informationText)
    {
        this.informationText = informationText;
    }

    @FXML @Override protected void initialize()
    {
        textLabel.setText(informationText);
    }

    @FXML
    public void accept(ActionEvent actionEvent)
    {
        close();
    }

    @Override
    public Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }

    @Override
    public URL getFXML()
    {
        return InformationTextDialogController.class.getResource("informationTextDialog.fxml");
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL);
    }

    @Override
    public String getTitle()
    {
        return LangFileLoader.getTranslation("title.infodialog");
    }
}
