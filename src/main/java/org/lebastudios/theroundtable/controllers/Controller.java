package org.lebastudios.theroundtable.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.lebastudios.theroundtable.help.HelpStageController;
import org.lebastudios.theroundtable.locale.LangBundleLoader;

import java.io.IOException;
import java.net.URL;

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
                    "FXML root is null after loading. Check if the fx:id of the root node is correct.");
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

        final var fxmlLoader = getFXMLLoader();
        try
        {
            LangBundleLoader.loadLang(fxmlLoader, getBundleClass());

            if (!hasFXMLControllerDefined())
            {
                fxmlLoader.setController(this);
            }

            this.root = fxmlLoader.load();
            
            if (!this.getClass().equals(HelpStageController.class)) 
            {
                root.addEventFilter(KeyEvent.KEY_PRESSED, event ->
                {
                    if (event.isConsumed() || event.getCode() != KeyCode.F1) return;

                    event.consume();
                    HelpStageController controller = new HelpStageController();
                    controller.instantiate();
                    controller.getController().selectHelpEntryByController(this.getClass().getName());
                });
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error loading resource " + getFXML());
        }

        this.controller = fxmlLoader.getController();
    }

    public final T getController()
    {
        return controller == null ? (T) this : controller;
    }

    public boolean hasFXMLControllerDefined()
    {
        return false;
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
        return new FXMLLoader(getFXML());
    }
}
