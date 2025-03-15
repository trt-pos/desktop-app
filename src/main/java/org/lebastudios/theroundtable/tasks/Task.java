package org.lebastudios.theroundtable.tasks;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public abstract class Task<T> extends javafx.concurrent.Task<T>
{
    @Getter private final String iconName;
    @Getter @Setter private String taskName = "";
    private Task<?> rootTask;

    public Task(String iconName, Consumer<T> onTaskComplete)
    {
        this.iconName = iconName;
        this.setOnSucceeded(_ -> onTaskComplete.accept(this.getValue()));
    }
    
    public Task(String iconName)
    {
        this(iconName, _ -> {});
    }

    public Task()
    {
        this("task.png");
    }
    
    protected void addAndExecuteTask(Task<?> task)
    {
        Task<?> rootTask = this.rootTask == null ? this : this.rootTask;
        task.rootTask = rootTask;
        
        task.messageProperty().addListener((_, _, newValue) -> {
            rootTask.updateMessage(newValue);
        });
        
        task.progressProperty().addListener((_, _, newValue) -> {
            rootTask.updateProgress(newValue.doubleValue(), 1);
        });
        
        task.run();
    }
}
