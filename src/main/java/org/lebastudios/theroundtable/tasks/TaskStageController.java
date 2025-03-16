package org.lebastudios.theroundtable.tasks;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.ui.StageBuilder;

class TaskStageController extends StageController<TaskStageController>
{
    @FXML private Label taskTitleLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label messageLabel;
    @FXML private Button cancelButton;
    
    private final Task<?> task;

    public TaskStageController(Task<?> task)
    {
        this.task = task;
    }

    @Override
    @FXML
    protected void initialize()
    {
        cancelButton.setVisible(task.cancelable);

        cancelButton.setOnAction(_ -> task.cancel());
        
        task.progressProperty().addListener((_, _, newValue) ->
        {
            progressBar.setProgress(newValue.doubleValue());
        });

        task.messageProperty().addListener((_, _, newValue) ->
        {
            messageLabel.setText(newValue);
        });

        task.titleProperty().addListener((_, _, newValue) ->
        {
            if (newValue != null)
            {
                taskTitleLabel.setText(newValue);
            }
        });
        
        task.stateProperty().addListener((_, _, newValue) ->
        {
            switch (newValue)
            {
                case SUCCEEDED -> {
                    Logs.getInstance().log(Logs.LogType.INFO, "Task " + task.getTitle() + " done");
                    close();
                }
                case FAILED -> {
                    task.getException().printStackTrace();
                    Logs.getInstance().log("Task " + task.getTitle() + " failed", task.getException());
                    close();
                }
                case CANCELLED -> {
                    Logs.getInstance().log(Logs.LogType.INFO, "Task " + task.getTitle() + " cancelled");
                    close();
                }
            }
        });

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
