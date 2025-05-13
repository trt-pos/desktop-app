package org.lebastudios.theroundtable.server;

import javafx.application.Platform;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.server.requests.Updates;
import org.lebastudios.theroundtable.tasks.Task;

public class CheckAppUpdateTask extends Task<Void>
{
    @Override
    protected Void call() throws Exception
    {
        if (Updates.isUpdateAvailable())
        {
            Platform.runLater(() -> new ConfirmationTextDialogController(
                    Translator.getInstance().t("textblock.confupdate"),
                    response ->
                    {
                        if (!response) return;

                        Updates.donwloadAppLastVersion(() -> MainStageController.getInstance().requestRestart());
                    }
            ).instantiate());
        }
        
        return null;
    }
}
