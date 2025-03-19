package org.lebastudios.theroundtable.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public abstract class JsonFile<T extends JsonFile<T>> implements FilePersistenceable<T>
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    @SneakyThrows
    public T load()
    {
        File file = this.getFile();

        if (file == null)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    this.getClass().getSimpleName() + " file is null, is this intended?"
            );
            return (T) this;
        }
        
        if (!file.exists()) return (T) this;

        try (var reader = new FileReader(file))
        {
            return (T) GSON.fromJson(reader, this.getClass());
        }
        catch (Exception e)
        {
            Logs.getInstance().log(
                    "Error loading " + this.getClass().getSimpleName() + " json file",
                    e
            );
            return (T) this;
        }
    }

    @SneakyThrows
    public void save()
    {
        var file = this.getFile();

        if (file == null) 
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING, 
                    this.getClass().getSimpleName() + " file is null, is this intended?"
            );
            return;
        }
        
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) file.createNewFile();

        var fileContent = GSON.toJson(this);

        try (var writer = new FileWriter(file))
        {
            writer.write(fileContent);
        }
    }
}
