package org.lebastudios.theroundtable.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.lang.reflect.InvocationTargetException;

public abstract class Controller<T extends Controller<T>>
{
    @FXML protected Node root;
    private T controller;

    @FXML
    protected void initialize() {}

    public Node getRoot()
    {
        if (root == null) loadFXML();

        if (root == null)
        {
            throw new IllegalStateException(
                    "FXML root is null after loading. Check if the fx:id of the root node is correct and the " +
                            "Controller and FXML file configurations are correct."
            );
        }

        return this.root;
    }

    protected void loadFXML()
    {
        if (root != null) return;
        
        try
        {
            String viewClassName = this.getClass().getName().replace("Controller", "$View");
            Class<?> viewClass = this.getClass().getClassLoader().loadClass(
                    viewClassName
            );

            this.root = (Node) viewClass.getConstructors()[0].newInstance(this);
            this.initialize();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public T getController()
    {
        return controller == null ? (T) this : controller;
    }

    public final Parent getParent()
    {
        return (Parent) getRoot();
    }

    public final Stage getStage()
    {
        Scene scene = getRoot().getScene();
        
        if (scene == null) return null;
        
        return (Stage) scene.getWindow();
    }

    public final Class<? extends IPlugin> getBundleClass()
    {
        return PluginsManager.getInstance().getPluginOf(this.getClass())
                .orElseThrow()
                .getClass();
    }
}
