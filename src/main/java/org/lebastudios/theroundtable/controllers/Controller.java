package org.lebastudios.theroundtable.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class Controller<T extends Controller<T>>
{
    @FXML protected Node root;

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
}
