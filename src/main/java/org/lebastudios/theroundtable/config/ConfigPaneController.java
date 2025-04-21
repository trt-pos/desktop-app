package org.lebastudios.theroundtable.config;

import lombok.Getter;
import org.lebastudios.theroundtable.controllers.PaneController;

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

    public abstract ValidationResult validate();

    public void onSave(T configData) {}

    public final void updateConfigData()
    {
        updateConfigData(configData);
    }

    public final void updateUI()
    {
        updateUI(configData);
    }

    public final ValidationResult apply()
    {
        ValidationResult result = validate();

        if (result.success)
        {
            updateConfigData(configData);
            configData.save();
            onSave(configData);
        }

        return result;
    }

    public final void cancel()
    {
        updateUI(configData);
    }

    public record ValidationResult(boolean success, String message)
    {
        public static ValidationResult valid()
        {
            return new ValidationResult(true, "");
        }
        
        public static ValidationResult invalid(String message)
        {
            return new ValidationResult(false, message);
        }

        public static ValidationResult invalid()
        {
            return invalid("");
        }
    }
}
