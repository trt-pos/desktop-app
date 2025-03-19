package org.lebastudios.theroundtable.server;

import javafx.application.Platform;
import org.lebastudios.theroundtable.config.AccountConfigData;
import org.lebastudios.theroundtable.dialogs.InformationTextDialogController;
import org.lebastudios.theroundtable.dialogs.RequestTextDialogController;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.server.requests.Licenses;
import org.lebastudios.theroundtable.tasks.Task;

import java.util.function.Consumer;
import java.util.function.Function;

public class LicenseValidatorTask extends Task<Void>
{
    private static int tries = 0;

    private Consumer<Boolean> computeResult = new Consumer<>()
    {
        @Override
        public void accept(Boolean validation)
        {
            if (!validation)
            {
                Platform.runLater(() -> new RequestTextDialogController(
                                onLicenseIntroduced,
                                "License", "XXXX-XXXX-XXXX", licenseFormatValidator,
                                "Introduce your license", "Invalid license format", Platform::exit
                        ).instantiate()
                );
            }
            else
            {
                tries = 0;
            }
        }
    };

    public LicenseValidatorTask(Consumer<Boolean> computeResult)
    {
        this.computeResult = computeResult;
    }

    public LicenseValidatorTask() {}

    @Override
    protected Void call() throws Exception
    {
        tries++;

        updateMessage("Reading license...");
        var license = new AccountConfigData().load().license;

        updateMessage("Validating license...");
        var validation = Licenses.isLicenseValid(license);

        if (validation != null)
        {
            computeResult.accept(validation);
        }
        else
        {
            if (license == null || license.isEmpty())
            {
                Platform.runLater(() ->
                {
                    new InformationTextDialogController(
                            "An error ocurred while validating your license. " +
                                    "Check your internet connection and try again."
                    ).instantiate(true);

                    AppLifeCicleEvents.OnAppClose.invoke(null);
                    Platform.exit();
                });

                return null;
            }
        }

        return null;
    }

    private final Consumer<String> onLicenseIntroduced = license ->
    {
        if (tries >= 3)
        {
            AppLifeCicleEvents.OnAppClose.invoke(null);
            Platform.exit();
            return;
        }

        var accountData = new AccountConfigData().load();
        license = license.replace("-", "");
        accountData.license = license;
        accountData.save();

        this.execute();
    };

    private final Function<String, Boolean> licenseFormatValidator = license ->
            license.matches("[A-Za-z0-9\\-]{12,28}");
}
