package org.lebastudios.theroundtable.controllers;

import javafx.stage.Stage;
import javafx.stage.Window;
import org.lebastudios.theroundtable.ui.SceneBuilder;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.util.function.Consumer;

public abstract class StageController<T extends Controller<T>> extends Controller<T>
{
    private Window owner;
    
    public void instantiate(Consumer<T> acceptController, boolean shouldWait)
    {
        StageBuilder stageBuilder = getDefaultStageBuilder();
        
        customizeStageBuilder(stageBuilder);
        Stage stage = stageBuilder.build();

        acceptController.accept(getController());
        
        if (shouldWait) 
        {
            stage.showAndWait();
        }
        else
        {
            stage.show();
        }
        
    }
    
    public final void instantiate(boolean shouldWait)
    {
        instantiate(_ -> {}, shouldWait);
    }
    
    public final void instantiate()
    {
        instantiate(_ -> {}, false);
    }
    
    public T setOwner(Window owner)
    {
        this.owner = owner;
        return (T) this;
    }
    
    private StageBuilder getDefaultStageBuilder()
    {
        return new StageBuilder(getDefaultSceneBuilder().build())
                .setTitle(getTitle())
                .setOwner(owner);
    }
    
    private SceneBuilder getDefaultSceneBuilder()
    {
        var sceneBuilder = new SceneBuilder(getParent());
        
        customizeSceneBuilder(sceneBuilder);
        
        return sceneBuilder;
    }
    
    protected void customizeSceneBuilder(SceneBuilder sceneBuilder) {}
    protected void customizeStageBuilder(StageBuilder stageBuilder) {}
    
    public abstract String getTitle();
    
    protected final void close()
    {
        getStage().close();
    }
}
