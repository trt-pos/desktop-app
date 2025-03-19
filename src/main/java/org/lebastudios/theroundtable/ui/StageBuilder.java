package org.lebastudios.theroundtable.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.logs.Logs;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class StageBuilder
{
    private final Scene scene;
    private StageStyle stageStyle = StageStyle.DECORATED;
    private String iconName = "the-round-table-logo.png";
    private String title = "";
    private boolean resizeable = false;
    private Modality modality = Modality.NONE;
    private Consumer<Stage> stageConsumer;
    private Window owner;

    public StageBuilder(Scene scene)
    {
        this.scene = scene;
    }

    public StageBuilder(Parent root)
    {
        this.scene = new SceneBuilder(root).build();
    }

    public StageBuilder setStageStyle(StageStyle style)
    {
        this.stageStyle = style;
        return this;
    }

    public StageBuilder setIconName(String iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public StageBuilder setResizeable(boolean resizeable)
    {
        this.resizeable = resizeable;
        return this;
    }

    public StageBuilder setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public StageBuilder setModality(Modality modality)
    {
        this.modality = modality;
        return this;
    }

    public StageBuilder setStageConsumer(Consumer<Stage> consumer)
    {
        this.stageConsumer = consumer;
        return this;
    }
    
    public StageBuilder setOwner(Window owner)
    {
        this.owner = owner;
        return this;
    }
    
    public Stage build()
    {
        Stage stage = new Stage(stageStyle);
        stage.setScene(scene);

        stage.setTitle(title);
        stage.setResizable(resizeable);
        stage.initOwner(owner);
        
        stage.initModality(modality);

        if (modality == Modality.WINDOW_MODAL && owner == null) 
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "An stage with modality '" + modality + "' does not have a window owner an it's recommended."
            );
        }
        
        stage.getIcons().add(ImageLoader.getIcon(iconName));

        if (stageConsumer != null) stageConsumer.accept(stage);
        
        return stage;
    }
}
