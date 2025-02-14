package org.lebastudios.theroundtable.controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.lebastudios.theroundtable.help.HelpStageController;
import org.lebastudios.theroundtable.help.OpenHelp;
import org.lebastudios.theroundtable.locale.LangBundleLoader;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.function.Consumer;

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
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error loading resource " + getFXML());
        }

        this.controller = fxmlLoader.getController();

        processHelpFunctionality();
    }

    private void processHelpFunctionality()
    {
        T uiController = getController();

        // This block adds all the help support
        if (uiController.getClass().getAnnotation(OpenHelp.class) != null)
        {
            root.addEventHandler(KeyEvent.KEY_PRESSED, event ->
            {
                if (event.isConsumed() || event.getCode() != KeyCode.F1) return;

                event.consume();
                HelpStageController controller = new HelpStageController();
                controller.instantiate();
                controller.getController().selectHelpEntryByController(uiController.getClass().getName());
            });
        }

        Arrays.stream(uiController.getClass().getDeclaredFields())
                .parallel()
                .forEach(f ->
                {
                    if (!Node.class.isAssignableFrom(f.getType())) return;
                    if (f.getAnnotation(OpenHelp.class) == null)  return;
                    
                    f.setAccessible(true);
                    Node node;
                    try
                    {
                        node = (Node) f.get(uiController);
                    }
                    catch (IllegalAccessException e)
                    {
                        Logs.getInstance().log("The field annotatted isn't accesible", e);
                        return;
                    }

                    if (node == null)
                    {
                        Logs.getInstance().log(
                                Logs.LogType.WARNING,
                                String.format(
                                        "Field %s marked with OpenHelp in class %s is null. Not showing help for it.",
                                        f.getName(), uiController.getClass().getName()
                                )
                        );
                        return;
                    }

                    node.addEventFilter(KeyEvent.KEY_PRESSED, event ->
                    {
                        if (event.isConsumed() || event.getCode() != KeyCode.F1) return;

                        event.consume();
                        HelpStageController controller = new HelpStageController();
                        controller.instantiate();
                        controller.getController().selectHelpEntryByController(uiController.getClass().getName());
                        
                        String id = f.getAnnotation(OpenHelp.class).id();
                        
                        if (!id.isBlank()) 
                        {
                            controller.showElementWithId(id);
                        }
                    });
                });
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
