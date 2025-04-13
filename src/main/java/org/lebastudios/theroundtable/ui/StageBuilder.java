package org.lebastudios.theroundtable.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class StageBuilder
{
    private final Scene scene;
    private StageStyle stageStyle = StageStyle.DECORATED;
    private String iconName = null;
    private String title = "";
    private boolean resizeable = false;
    private Modality modality = Modality.NONE;
    private Consumer<Stage> stageConsumer;
    private Window owner;

    private static Image defaultIcon;

    static
    {
        try (InputStream is = CorePlugin.class.getResourceAsStream("plugin-icon.png"))
        {
            assert is != null;
            defaultIcon = new Image(is);
        }
        catch (IOException e)
        {
            Logs.getInstance().log("Error loading default stage icon", e);
        }
    }

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

        if (modality == Modality.WINDOW_MODAL && owner == null)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "An stage with modality '" + modality + "' does not have a window owner an it's recommended. " +
                            "Loading as APPLICATION_MODAL instead until you fix it."
            );
            modality = Modality.APPLICATION_MODAL;
        }

        stage.initModality(modality);

        Image image = iconName == null
                ? defaultIcon
                : ImageLoader.getIcon(iconName);

        stage.getIcons().add(image);

        if (stageConsumer != null) stageConsumer.accept(stage);

        return stage;
    }
}
