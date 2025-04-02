package org.lebastudios.theroundtable.tasks;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import lombok.Getter;
import org.lebastudios.theroundtable.config.DatabaseConfigPaneController;
import org.lebastudios.theroundtable.config.RequestConfigStageController;
import org.lebastudios.theroundtable.dialogs.ExceptionDialogController;
import org.lebastudios.theroundtable.events.Event1;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class Task<T> extends javafx.concurrent.Task<T>
{
    @Getter private final String iconName;
    private Task<?> rootTask;
    boolean cancelable;
    
    public final Event1<Task<?>> onSubtaskStarted = new Event1<>();
    
    private static final BiConsumer<Throwable, Task<?>> defaultErrorHandler = (e, task) -> 
    {
        // Only the root task should show the error dialog as default
        if (task.rootTask != null) return;
        
        new ExceptionDialogController(e)
                .instantiate(true);
    };

    public Task(String iconName)
    {
        this.iconName = iconName;
    }

    public Task()
    {
        this("task.png");
        this.setOnFailure(e -> defaultErrorHandler.accept(e, this));
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
    
    public Task<T> setOnFailure(Consumer<Throwable> onFailure)
    {
        this.setOnFailed((_) -> onFailure.accept(this.getException()));
        return this;
    }
    
    public Task<T> setOnCancelled(Consumer<WorkerStateEvent> onCancelled)
    {
        this.setOnCancelled((EventHandler<WorkerStateEvent>) onCancelled::accept);
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

    protected synchronized void executeInFxThread(Runnable runnable) throws InterruptedException
    {
        Platform.runLater(() ->
        {
            try
            {
                runnable.run();
            }
            catch (Exception ex)
            {
                Logs.getInstance().log(
                        "Error executing blocking action inside a task",
                        ex
                );
            }
            synchronized (this)
            {
                this.notify();
            }
        });
        
        this.wait();
    }
    
    protected <R> R executeSubtask(Task<R> task) throws Exception
    {
        Task<?> rootTask = this.rootTask == null ? this : this.rootTask;
        task.rootTask = rootTask;

        task.messageProperty().addListener((_, _, newValue) -> {
            rootTask.updateMessage(newValue);
        });

        task.progressProperty().addListener((_, _, newValue) -> {
            rootTask.updateProgress(newValue.doubleValue(), 1);
        });

        Logs.getInstance().log(Logs.LogType.INFO, "Executing subtask: " + task.getClass().getCanonicalName());
        rootTask.onSubtaskStarted.invoke(task);
        task.run();
        return task.get();
    }
}
