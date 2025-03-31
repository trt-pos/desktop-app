package org.lebastudios.theroundtable.dialogs;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.ui.StageBuilder;

public class ExceptionDialogController extends StageController<ExceptionDialogController>
{
    private final Throwable throwable;
    
    @FXML public Label textLabel;
    @FXML public Label backtraceLabel;
    @FXML public TitledPane titledPane;

    public ExceptionDialogController(Throwable throwable)
    {
        this.throwable = throwable;
    }

    @Override
    protected void initialize()
    {
        textLabel.setText("Error message: " + throwable.getMessage());
        backtraceLabel.setText(throwable.toString());
        
        if (throwable.getStackTrace().length > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : throwable.getStackTrace())
            {
                sb.append(element.toString()).append("\n");
            }
            backtraceLabel.setText(sb.toString());
        }
        
        titledPane.expandedProperty().addListener((_, _, _) -> 
                Platform.runLater(() -> this.getStage().sizeToScene())
        );
        
    }

    @FXML
    public void copyToClipboard(ActionEvent actionEvent) 
    {
        String text = textLabel.getText() + "\n" + backtraceLabel.getText();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        
        Clipboard.getSystemClipboard().setContent(content);
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.WINDOW_MODAL)
                .setResizeable(true);
    }

    @Override
    public String getTitle()
    {
        return "";
    }
}
