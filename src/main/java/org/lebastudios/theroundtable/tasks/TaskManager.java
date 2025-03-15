package org.lebastudios.theroundtable.tasks;

import lombok.Getter;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.TaskProgressView;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.ui.IconView;

public class TaskManager
{
    private static TaskManager instance;
    
    public static TaskManager getInstance()
    {
        if (instance == null) instance = new TaskManager();
        
        return instance;
    }

    private final TaskProgressView<Task<?>> taskProgressView;
    @Getter private final PopOver tasksPopOver;

    public TaskManager()
    {
        taskProgressView = new TaskProgressView<>();
        taskProgressView.setMaxHeight(150);
        taskProgressView.setGraphicFactory(task -> new IconView(task.getIconName()));

        tasksPopOver = new PopOver(taskProgressView);
        tasksPopOver.setTitle(LangFileLoader.getTranslation("button.tasks"));
        tasksPopOver.setDetachable(false);
    }
    
    void startNewBackgroundTask(Task<?> task, boolean daemon)
    {
        taskProgressView.getTasks().add(task);

        var thread = new Thread(task);
        thread.setDaemon(daemon);
        thread.start();
    }

    void startNewTask(Task<?> task, boolean wait)
    {
        new TaskStageController(task).instantiate(wait);
    }
}
