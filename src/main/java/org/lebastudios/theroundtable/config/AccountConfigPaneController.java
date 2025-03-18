package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.server.LicenseValidatorTask;

public class AccountConfigPaneController extends ConfigPaneController<AccountConfigData>
{
    @FXML private TextField licenseId;

    public AccountConfigPaneController()
    {
        super(new AccountConfigData());
    }

    @Override
    public void updateConfigData(AccountConfigData configData)
    {
        configData.license = licenseId.getText();
    }

    @Override
    public void updateUI(AccountConfigData configData)
    {
        licenseId.setText(configData.license);
    }

    @Override
    public boolean validate()
    {
        boolean[] valid = {false};
        
        new LicenseValidatorTask(validation -> valid[0] = validation).execute(true);
        
        return valid[0];
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

}
