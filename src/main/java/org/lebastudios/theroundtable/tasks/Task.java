package org.lebastudios.theroundtable.tasks;

import javafx.concurrent.Worker;
import lombok.Getter;

import java.util.function.Consumer;

public abstract class Task<T> extends javafx.concurrent.Task<T>
{
    @Getter private final String iconName;
    private Task<?> rootTask;
    boolean cancelable;

    public Task(String iconName, Consumer<T> onTaskComplete)
    {
        this.iconName = iconName;
        this.stateProperty().addListener((_, _, newValue) ->
        {
            if (newValue == Worker.State.SUCCEEDED)
            {
                onTaskComplete.accept(this.getValue());
            }
        });
    }
    
    public Task(String iconName)
    {
        this(iconName, _ -> {});
    }

    public Task()
    {
        this("task.png");
    }
    
    protected void addTask(Task<?> task)
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

    public void execute(boolean wait)
    {
        TaskManager.getInstance().startNewTask(this, wait);
    }
    
    public void execute()
    {
        execute(true);
    }

    public void executeInBackGround(boolean daemon)
    {
        TaskManager.getInstance().startNewBackgroundTask(this, daemon);
    }
    
    public void executeInBackGround()
    {
        executeInBackGround(false);
    }
}
