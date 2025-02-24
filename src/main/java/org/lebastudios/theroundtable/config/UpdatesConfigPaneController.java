package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.UpdatesConfigData;

import java.net.URL;

public class UpdatesConfigPaneController extends SettingsPaneController
{
    @Override
    protected void initialize()
    {
        UpdatesConfigData data = new JSONFile<>(UpdatesConfigData.class).get();
    }

    @Override
    public void apply()
    {
        
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

    @Override
    public boolean hasFXMLControllerDefined()
    {
        return true;
    }

    @Override
    public URL getFXML()
    {
        return UpdatesConfigPaneController.class.getResource("updatesConfigPane");
    }
}
