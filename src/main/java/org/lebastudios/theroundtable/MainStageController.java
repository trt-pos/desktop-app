package org.lebastudios.theroundtable;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.accounts.AccountStageController;
import org.lebastudios.theroundtable.camelot.CamelotEventsManager;
import org.lebastudios.theroundtable.camelot.FromStringToBytes;
import org.lebastudios.theroundtable.config.ConfigStageController;
import org.lebastudios.theroundtable.config.DatabaseConfigData;
import org.lebastudios.theroundtable.controllers.Controller;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.events.AccountEvents;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.plugins.PluginsManager;
import org.lebastudios.theroundtable.plugins.PluginsStageController;
import org.lebastudios.theroundtable.tasks.TaskManager;
import org.lebastudios.theroundtable.ui.IconButton;
import org.lebastudios.theroundtable.ui.LoadingPaneController;
import org.lebastudios.theroundtable.ui.SceneBuilder;

public class MainStageController extends PaneController<MainStageController>
{
    @Getter private static MainStageController instance;
    
    @FXML public IconButton openTasksPopupButton;
    @FXML public IconButton pluginsButton;
    @FXML public VBox leftTopButtons;
    @FXML public VBox leftBottomButtons;
    @FXML public VBox rightBottomButtons;
    
    private final Button homeButton;

    public MainStageController()
    {
        instance = this;
        
        homeButton = new IconButton("home.png");
        homeButton.setOnAction(_ -> setCentralNode(new HomePaneController()));
    }

    @SneakyThrows
    @FXML
    @Override
    protected void initialize()
    {
        leftTopButtons.getChildren().clear();
        leftTopButtons.getChildren().addAll(PluginsManager.getInstance().getLeftButtons());
        
        rightBottomButtons.getChildren().clear();
        rightBottomButtons.getChildren().addAll(PluginsManager.getInstance().getRightButtons());
        
        if (!PluginsManager.getInstance().getHomeButtons().isEmpty())
        {
            rightBottomButtons.getChildren().add(homeButton);
        }

        ((BorderPane) this.getRoot()).setCenter(
                new LogoPaneController().getRoot()
        );
        
        CamelotEventsManager.getInstance().invokeEvent("desktop-app:test", new FromStringToBytes("Hello World from Main stage!"));
    }

    public void setCentralNode(Controller<?> controller)
    {
        final BorderPane root = (BorderPane) getRoot();
        root.setCenter(new LoadingPaneController().getRoot());

        new Thread(() ->
        {
            final Node content = controller.getRoot();
            Platform.runLater(() -> root.setCenter(content));
        }).start();
    }

    public void setCentralNode(Node node)
    {
        BorderPane root = (BorderPane) getRoot();
        root.setCenter(node);
    }

    public Node getCentralNode()
    {
        return ((BorderPane) getRoot()).getCenter();
    }

    public void requestRestart()
    {
        showNotification(LangFileLoader.getTranslation("textblock.infrestartneeded"),
                new Action(LangFileLoader.getTranslation("word.restart"), _ ->
                        Launcher.restartAplication()));
    }

    public void showNotification(String message, Action action)
    {
        Notifications.create()
                .text(message)
                .owner(getRoot())
                .action(action)
                .show();
    }

    @FXML
    public void openTasksPopup(ActionEvent actionEvent)
    {
        TaskManager.getInstance().getTasksPopOver().show(openTasksPopupButton);
    }

    @SneakyThrows
    @FXML
    public void closeSession(ActionEvent actionEvent)
    {
        Stage stage = getStage();

        stage.hide();
        AccountManager.getInstance().logOut();

        new AccountStageController().instantiate(true);

        // Loads and set the new instance and then shows it.
        instance.initialize();
        stage.setTitle("The Round Table - " + AccountManager.getInstance().getCurrentLoggedAccountName());
        stage.show();
    }

    @Override
    public Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }

}