package org.lebastudios.theroundtable.tasks;

import javafx.concurrent.Worker;
import lombok.Getter;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public abstract class Task<T> extends javafx.concurrent.Task<T>
{
    @Getter private final String iconName;
    private Task<?> rootTask;
    boolean cancelable;

    public Task(String iconName)
    {
        this.iconName = iconName;
    }

    public Task()
    {
        this("task.png");
    }
    
    public Task<T> setCancelable(boolean cancelable)
    {
        this.cancelable = cancelable;
        return this;
    }
    
    public Task<T> setOnTaskComplete(Consumer<T> onTaskComplete)
    {
        this.setOnSucceeded((_) -> onTaskComplete.accept(this.getValue()));
        return this;
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

    protected <R> R executeSubtask(Task<R> task)
    {
        Task<?> rootTask = this.rootTask == null ? this : this.rootTask;
        task.rootTask = rootTask;

        task.messageProperty().addListener((_, _, newValue) -> {
            rootTask.updateMessage(newValue);
        });

        task.progressProperty().addListener((_, _, newValue) -> {
            rootTask.updateProgress(newValue.doubleValue(), 1);
        });

        try
        {
            task.run();
            return task.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            Logs.getInstance().log("Error executing subtask " + task.getTitle(), e);
            return null;
        }
    }
}
