package org.lebastudios.theroundtable.setup;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.config.DatabaseConfigPaneController;
import org.lebastudios.theroundtable.config.EstablishmentConfigPaneController;
import org.lebastudios.theroundtable.config.GeneralConfigData;
import org.lebastudios.theroundtable.config.PrintersConfigPaneController;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.plugins.IPlugin;
import org.lebastudios.theroundtable.tasks.Task;
import org.lebastudios.theroundtable.ui.LoadingPaneController;
import org.lebastudios.theroundtable.ui.StageBuilder;

import java.net.URL;

public class SetupStageController extends StageController<SetupStageController>
{
    private static final SetupPaneController[] setupPanes = {
            new AccountSetupPaneController(),
            new ConfigPaneWrapperController(new EstablishmentConfigPaneController()),
            new ConfigPaneWrapperController(new PrintersConfigPaneController()),
            new ConfigPaneWrapperController(new DatabaseConfigPaneController()),
    };

    private int currentPane = -1;

    @FXML private Button backButton;
    @FXML private Button nextButton;
    @FXML private ScrollPane mainPane;

    public static boolean checkIfStart()
    {
        return !new GeneralConfigData().load().setupComplete;
    }

    @FXML
    @Override
    protected void initialize()
    {
        backButton.setDisable(true);
    }

    @Override
    public Class<? extends IPlugin> getBundleClass()
    {
        return CorePlugin.class;
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setResizeable(true)
                .setStageConsumer(s -> s.setOnCloseRequest(e ->
                {
                    new ConfirmationTextDialogController(
                            LangFileLoader.getTranslation("textblock.closingsetup"),
                            response ->
                            {
                                if (response)
                                {
                                    AppLifeCicleEvents.OnAppClose.invoke(e);
                                    System.exit(0);
                                }
                            }
                    ).instantiate();
                    e.consume();
                }));
    }

    @Override
    public URL getFXML()
    {
        return SetupStageController.class.getResource("setupStage.fxml");
    }

    @FXML
    private void backButtonAction(ActionEvent actionEvent)
    {
        currentPane--;

        onCurrentPaneUpdate();
    }

    @FXML
    private void nextButtonAction(ActionEvent actionEvent)
    {
        if (currentPane > setupPanes.length - 1) return;
        if (currentPane >= 0 && !setupPanes[currentPane].getController().validate()) return;

        currentPane++;

        onCurrentPaneUpdate();
    }

    private void onCurrentPaneUpdate()
    {
        backButton.setDisable(currentPane <= 0);

        if (currentPane == setupPanes.length - 1)
        {
            nextButton.setText("Finish");
        }
        else
        {
            nextButton.setText("Next");
        }

        if (currentPane == setupPanes.length)
        {
            new ApplyConfigTask()
                    .setOnTaskComplete(_ -> close())
                    .execute(true);
            return;
        }

        mainPane.setContent(setupPanes[currentPane].getRoot());
    }

    @Override
    public String getTitle()
    {
        return "Setup";
    }

    private static class ApplyConfigTask extends Task<Void>
    {
        @Override
        protected Void call() throws Exception
        {
            updateTitle("Applying configuration");

            for (int i = 0; i < setupPanes.length; i++)
            {
                final var actualPane = setupPanes[i];

                Platform.runLater(() ->
                {
                    synchronized (actualPane)
                    {

                        actualPane.apply();
                        actualPane.notify();
                    }
                });
                
                synchronized (actualPane)
                {
                    actualPane.wait();
                }
                updateProgress(i + 1, setupPanes.length);
            }

            final var settingsData = new GeneralConfigData().load();
            settingsData.setupComplete = true;
            settingsData.save();

            return null;
        }
    }
}
