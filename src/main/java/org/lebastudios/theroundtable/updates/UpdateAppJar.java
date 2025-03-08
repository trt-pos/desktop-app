package org.lebastudios.theroundtable.updates;

import javafx.application.Platform;
import org.lebastudios.theroundtable.MainStageController;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.server.requests.Updates;

public class UpdateAppJar
{
    public void update()
    {
        if (Updates.isUpdateAvailable())
        {
            Platform.runLater(() -> new ConfirmationTextDialogController(
                    LangFileLoader.getTranslation("textblock.confupdate"),
                    response ->
                    {
                        if (!response) return;

                        Updates.donwloadAppLastVersion(() -> MainStageController.getInstance().requestRestart());
                    }
            ).instantiate());
        }
    }
}
