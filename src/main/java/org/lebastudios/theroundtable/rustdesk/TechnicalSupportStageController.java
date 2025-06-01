package org.lebastudios.theroundtable.rustdesk;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.components.LabeledIconButton;
import org.lebastudios.theroundtable.components.StageBuilder;
import org.lebastudios.theroundtable.controllers.StageController;

public class TechnicalSupportStageController extends StageController<TechnicalSupportStageController>
{
    @FXML public Label rustdeskIdLabel;
    
    @FXML public VBox downloadRustDeckContainer;
    @FXML public VBox askForSupportContainer;

    @Override
    protected void initialize()
    {
        RustDeckIntrospector rustDeckIntrospector = new RustDeckIntrospector();
        
        if (!rustDeckIntrospector.isInstalled()) 
        {
            ((VBox) this.getRoot()).getChildren().remove(askForSupportContainer);
            return;
        }

        ((VBox) this.getRoot()).getChildren().remove(downloadRustDeckContainer);
        updateLabels();
    }

    @Override
    public String getTitle()
    {
        return "Technical support";
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(true);
    }

    @FXML
    public void askForSupport(ActionEvent actionEvent)
    {
        
    }

    @FXML
    public void downloadRustDeck(ActionEvent actionEvent)
    {
        new RustDeskInstallTask().setOnTaskComplete(_ ->
        {
            ((VBox) this.getRoot()).getChildren().remove(downloadRustDeckContainer);
            ((VBox) this.getRoot()).getChildren().add(askForSupportContainer);

            updateLabels();
        }).setCancelable(true).execute();
    }
    
    private void updateLabels()
    {
        RustDeckIntrospector rustDeckIntrospector = new RustDeckIntrospector();
        
        rustdeskIdLabel.setText(rustDeckIntrospector.getID());
    }
}
