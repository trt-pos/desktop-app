package org.lebastudios.theroundtable.tasks;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.ui.LoadingPaneController;
import org.lebastudios.theroundtable.ui.StageBuilder;

class TaskStageController extends StageController<TaskStageController>
{
    @FXML private Label taskNameLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label messageLabel;
    @FXML private Button cancelButton;
    
    private final Task<?> task;
    private final boolean cancelable;

    public TaskStageController(Task<?> task, boolean cancelable)
    {
        this.task = task;
        this.cancelable = cancelable;
    }
    
    public TaskStageController(Task<?> task)
    {
        this(task, false);
    }

    @Override
    @FXML
    protected void initialize()
    {
        taskNameLabel.setText(task.getTaskName());
        
        cancelButton.setVisible(cancelable);

        cancelButton.setOnAction(_ -> task.cancel());

        task.progressProperty().addListener((_, _, newValue) ->
        {
            progressBar.setProgress(newValue.doubleValue());
        });

        task.messageProperty().addListener((_, _, newValue) ->
        {
            messageLabel.setText(newValue);
        });

        task.setOnSucceeded(_ -> close());

        task.setOnFailed(_ -> close());

        task.setOnCancelled(_ -> close());

        new Thread(task).start();
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setModality(Modality.APPLICATION_MODAL)
                .setResizeable(false)
                .setStageStyle(StageStyle.UNDECORATED);
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
