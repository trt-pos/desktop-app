package org.lebastudios.theroundtable.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.lebastudios.theroundtable.locale.LangBundleLoader;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

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
            return;
        }
        catch (ClassNotFoundException ignore) {}
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

        loadFXML(true);
    }

    private void loadFXML(boolean injectController)
    {
        try
        {
            FXMLLoader fxmlLoader = getFXMLLoader();
            LangBundleLoader.loadLang(fxmlLoader, getBundleClass());
            
            fxmlLoader.setController(injectController ? this : null);
            this.root = fxmlLoader.load();
            this.controller = fxmlLoader.getController();
        }
        catch (IOException e)
        {
            if (e.getMessage().contains("Controller value already specified."))
            {
                loadFXML(false);
            }
            else
            {
                Logs.getInstance().log(
                        "Unexpected error while trying to load the FXML for "
                                + this.getClass().getName() + ": " + this.getFXML().getFile(),
                        e
                );
            }
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
        return (Stage) getRoot().getScene().getWindow();
    }

    public final Class<? extends IPlugin> getBundleClass()
    {
        return PluginsManager.getInstance().getPluginOf(this.getClass())
                .orElseThrow()
                .getClass();
    }

    public URL getFXML()
    {
        Class<?> clazz = getClass();
        String fxmlNameLowerCamelCase =
                clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
        String fxmlNameWithoutController = fxmlNameLowerCamelCase.replace("Controller", "");
        return clazz.getResource(fxmlNameWithoutController + ".fxml");
    }

    public final FXMLLoader getFXMLLoader()
    {
        final var fxml = getFXML();
        
        if (fxml == null) 
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "FXML loader resource is null for the controller " + this.getClass().getName()
            );
        }
        
        var fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        return fxmlLoader;
    }
}
