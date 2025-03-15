package org.lebastudios.theroundtable.tasks;

import javafx.collections.ListChangeListener;
import lombok.Getter;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.TaskProgressView;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.IconView;

import java.util.function.Consumer;

public class TaskManager extends IconButton
{
    @Getter private static TaskManager instance;

    private final TaskProgressView<Task<?>> taskProgressView;

    public TaskManager()
    {
        super("task.png");
        this.setDisable(true);
        taskProgressView = new TaskProgressView<>();
        taskProgressView.setMaxHeight(150);
        taskProgressView.getTasks().addListener(this::onChanged);
        taskProgressView.setGraphicFactory(task -> new IconView(task.getIconName()));

        this.setOnAction(event -> onClick());

        instance = this;
    }

    private void onChanged(ListChangeListener.Change<? extends Task<?>> c)
    {
        this.setDisable(taskProgressView.getTasks().isEmpty());
    }

    private void onClick()
    {
        var pop = new PopOver(taskProgressView);

        pop.setTitle(LangFileLoader.getTranslation("button.tasks"));
        pop.setDetachable(false);

        pop.show(this);
    }

    public void startNewBackgroundTask(Task<?> task)
    {
        startNewBackgroundTask(task, true);
    }

    public void startNewBackgroundTask(Task<?> task, boolean daemon)
    {
        taskProgressView.getTasks().add(task);

        var thread = new Thread(task);
        thread.setDaemon(daemon);
        thread.start();
    }

    public void startNewTask(Task<?> task)
    {
        startNewTask(task, true);
    }

    public void startNewTask(Task<?> task, boolean wait)
    {
        new TaskStageController(task).instantiate(wait);
    }
}
