package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.server.LicenseValidatorTask;

public class LicenseConfigPaneController extends ConfigPaneController<LicenseConfigData>
{
    @FXML public TextField licenseId;

    public LicenseConfigPaneController()
    {
        super(new LicenseConfigData(), LangFileLoader.getTranslation("word.license"), "user.png");
    }

    @Override
    public void updateConfigData(LicenseConfigData configData)
    {
        configData.license = licenseId.getText();
    }

    @Override
    public void updateUI(LicenseConfigData configData)
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

}
