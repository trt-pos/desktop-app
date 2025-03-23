package org.lebastudios.theroundtable.config;

import lombok.Getter;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.logs.Logs;

public abstract class ConfigPaneController<T extends ConfigData<T>> extends PaneController<ConfigPaneController<T>>
{
    private final T configData;
    @Getter private final String iconName;
    @Getter private final String title;

    public ConfigPaneController(T configData, String title, String iconName)
    {
        this.configData = configData.load();
        this.title = title;
        this.iconName = iconName;
    }

    @Override
    protected void initialize()
    {
        updateUI(configData);
    }

    public abstract void updateConfigData(T configData);
    public abstract void updateUI(T configData);
    public abstract boolean validate();
    public void onSave(T configData) {}
    
    public final void updateConfigData()
    {
        updateConfigData(configData);
    }
    
    public final void updateUI()
    {
        updateUI(configData);
    }
    
    public final void accept()
    {
        if (!apply()) return;

        getStage().close();
    }

    public final boolean apply() 
    {
        if (!validate())
        {
            Logs.getInstance().log(Logs.LogType.INFO, "Invalid settings");
            return false;
        }

        updateConfigData(configData);
        configData.save();
        onSave(configData);
        return true;
    }

    public final void cancel()
    {
        updateUI(configData);

        getStage().close();
    }
}
