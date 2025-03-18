package org.lebastudios.theroundtable.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.lebastudios.theroundtable.locale.LangBundleLoader;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public abstract class Controller<T extends Controller<T>>
{
    @FXML private Node root;
    private T controller;
    private Thread loadingRoot;

    @FXML
    protected void initialize() {}

    public Node getRoot()
    {
        if (loadingRoot != null)
        {
            try
            {
                loadingRoot.join();
            }
            catch (InterruptedException _) {}
        }

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

    public void loadAsync()
    {
        Thread loadingThread = new Thread(() ->
        {
            loadFXML();
            loadingRoot = null;
        });
        loadingThread.start();

        loadingRoot = loadingThread;
    }

    public final void loadFXML()
    {
        if (root != null) return;
        
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
                Logs.getInstance().log(
                        Logs.LogType.INFO,
                        "Controller is already specified in the FXML for " + this.getClass().getName() + ". "
                                + "Loading the FXML again without injecting this instance as the controller"
                );

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
    
    public final T getController()
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

    public abstract Class<?> getBundleClass();

    private static final HashMap<Class<Controller<?>>, FXMLLoader> loadersHashMap = new HashMap<>();
    
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
        var fxmlLoader = new FXMLLoader(getFXML());
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        return fxmlLoader;
    }

    // TODO: Pre-compile FXML files using a custom compiler that writes into every controller a private subclass
    //  called View that generated the node defined in the FXML file.
    //  Also is possible to use https://github.com/Paullo612/mlfx but everything marked as @FXML will be public.
}
