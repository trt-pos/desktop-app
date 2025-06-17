package org.lebastudios.theroundtable.themes;

import javafx.util.StringConverter;
import lombok.NonNull;
import org.lebastudios.theroundtable.CorePlugin;
import org.lebastudios.theroundtable.plugins.IPlugin;

import java.net.URL;

public record Theme(String name, String id, IPlugin plugin, URL url)
{
    public static final StringConverter<Theme> STRING_CONVERTER = new StringConverter<>()
    {
        @Override
        public String toString(Theme theme)
        {
            return theme.name();
        }

        @Override
        public Theme fromString(String string)
        {
            return null;
        }
    };
    
    public static final Theme DEFAULT = new Theme("Cupertino Light", "cupertino-light", CorePlugin.getInstance());
    
    public Theme(String name, String id, @NonNull IPlugin plugin)
    {
        this(name, id, plugin, plugin.getClass().getResource("themes/" + id + ".css"));
    }
}
