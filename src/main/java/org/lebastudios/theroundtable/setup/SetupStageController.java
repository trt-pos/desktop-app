package org.lebastudios.theroundtable.setup;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import org.lebastudios.theroundtable.config.EstablishmentConfigPaneController;
import org.lebastudios.theroundtable.config.PrintersConfigPaneController;
import org.lebastudios.theroundtable.controllers.StageController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.tasks.Task;
import org.lebastudios.theroundtable.components.StageBuilder;
import org.lebastudios.theroundtable.TheRoundTableApplication;

public class SetupStageController extends StageController<SetupStageController>
{
    private static final SetupPaneController[] setupPanes = {
            new AccountSetupPaneController(),
            new ConfigPaneWrapperController(new EstablishmentConfigPaneController()),
            new ConfigPaneWrapperController(new PrintersConfigPaneController()),
    };

    private int currentPane = -1;

    @FXML public Button backButton;
    @FXML public Button nextButton;
    @FXML public ScrollPane mainPane;

    public static boolean isSetupDone()
    {
        return Database.getInstance().connectQuery(session -> {
            return session.createQuery("select count(*) from Account a where a.type=:type", Long.class)
                    .setParameter("type", Account.AccountType.ROOT)
                    .getSingleResult();
        }) > 0;
    }

    @FXML
    @Override
    protected void initialize()
    {
        backButton.setDisable(true);
    }

    @Override
    protected void customizeStageBuilder(StageBuilder stageBuilder)
    {
        stageBuilder.setResizeable(true)
                .setStageConsumer(s -> s.setOnCloseRequest(e ->
                {
                    new ConfirmationTextDialogController(
                            Translator.getInstance().t("core:textblock.closingsetup"),
                            response ->
                            {
                                if (response)
                                {
                                    TheRoundTableApplication.exitAplication(0);
                                }
                            }
                    ).instantiate();
                    e.consume();
                }));
    }

    

    @FXML
    public void backButtonAction(ActionEvent actionEvent)
    {
        currentPane--;

        onCurrentPaneUpdate();
    }

    @FXML
    public void nextButtonAction(ActionEvent actionEvent)
    {
        if (currentPane > setupPanes.length - 1) return;
        if (currentPane >= 0 && !setupPanes[currentPane].validate()) return;

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

            return null;
        }
    }
}
