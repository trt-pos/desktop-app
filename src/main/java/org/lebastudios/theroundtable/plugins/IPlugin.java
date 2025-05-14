package org.lebastudios.theroundtable.plugins;

import com.google.gson.Gson;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.apparience.ImageLoader;
import org.lebastudios.theroundtable.config.SettingsItem;
import org.lebastudios.theroundtable.database.IDatabaseUpdater;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;
import org.lebastudios.theroundtable.components.LabeledIconButton;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public interface IPlugin extends IDatabaseUpdater
{
    void initialize();

    default List<Button> getRightButtons()
    {
        return new ArrayList<>();
    }

    default List<Button> getLeftButtons()
    {
        return new ArrayList<>();
    }

    default List<LabeledIconButton> getHomeButtons()
    {
        return new ArrayList<>();
    }
    
    default TreeItem<SettingsItem> getSettingsRootTreeItem()
    {
        return null;
    }

    default List<Class<?>> getPluginEntities() { return new ArrayList<>(); }
    
    default Task<PurgeTaskResult> purgeTask() { return null; }
    
    default File getPluginFolder()
    {
        return new File(TheRoundTableApplication.getUserDirectory(), getPluginData().pluginId);
    }
    
    default Image getPluginIcon()
    {
        InputStream inputStream = this.getClass().getResourceAsStream("plugin-icon.png");

        if (inputStream == null)
        {
            return ImageLoader.getIcon("plugins.png");
        }

        return new Image(inputStream);
    }    
    
    default PluginData getPluginData()
    {
        InputStream is = this.getClass().getResourceAsStream("pluginData.json");
        
        if (is == null) 
        {
            is = this.getClass().getResourceAsStream("plugin-data.json");
        }
        
        if (is == null) 
        {
            throw new IllegalStateException("The pluginData.json file is missing");
        }
        
        try (InputStreamReader reader = new InputStreamReader(is))
        {
            return new Gson().fromJson(
                    reader,
                    PluginData.class
            );
        }
        catch (IOException e)
        {
            Logs.getInstance().log("Failed to load plugin data (" + this.getClass().getName() + ")", e);
            return null;
        }
    }
    
    default PluginRepoData getPluginRepoMetadata()
    {
        if (this.getPluginData().pluginId.equals(CorePlugin.getInstance().getPluginData().pluginId)) 
        {
            return null;
        }
        
        String jarPath = this.getClass()
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath();

        File jarFile = new File(jarPath);
        File repoFolder = jarFile.getParentFile();
        File repoMetadata = new File(repoFolder, "metadata.json");
        
        try (Reader reader = new FileReader(repoMetadata))
        {
            return new Gson().fromJson(reader, PluginRepoData.class);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load plugin repo metadata", e);
        }
    }
    
    default Plugin intoPluginObject()
    {
        return new Plugin(
                getPluginData(),
                getPluginRepoMetadata(),
                this
        );
    }
    
    record PurgeTaskResult(boolean success, String message) {}
}
