package org.lebastudios.theroundtable.help;

import javafx.scene.control.TreeItem;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.PreferencesConfigData;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

record HelpEntry(File path, HelpEntryMetadata metedata, HelpEntry[] innerEntries)
{
    public enum Type
    {
        MD, DIR, MODULE;

        public String getIconName()
        {
            return switch (this)
            {
                case MD -> "md-help-file.png";
                case DIR -> "directory.png";
                case MODULE -> "module.png";
            };
        }
    }

    public HelpEntry filteredByKeywords(String text)
    {
        List<HelpEntry> filteredInnerEntries = new ArrayList<>(innerEntries.length);
        
        for (var innerEntry : innerEntries)
        {
            // DIR entries should be added and the content filtered
            if (innerEntry.metedata.helpEntryType == Type.DIR) 
            {
                HelpEntry filteredInnerEnreies = innerEntry.filteredByKeywords(text);
                filteredInnerEntries.add(filteredInnerEnreies);
                continue;
            }
            
            String[] keywords = innerEntry.metedata.keywords;
            if (keywords == null) continue;
            
            boolean keywordMatchFound = false;
            int i = 0;
            
            while (!(keywordMatchFound || i >= keywords.length))
            {
                String keyword = keywords[i];
                if (keyword.matches(text))
                {
                    filteredInnerEntries.add(innerEntry.filteredByKeywords(text));
                    keywordMatchFound = true;
                }
                
                i++;
            }
        }
        
        return new HelpEntry(
                path,
                metedata,
                filteredInnerEntries.toArray(new HelpEntry[0])
        );
    }
    
    public MarkdownHelpToHtml intoMarkdownHelp()
    {
        String lang = new JSONFile<>(PreferencesConfigData.class).get().language;
        File file = new File(path.getAbsolutePath().replace(".yaml", "_" + lang + ".md"));

        if (!file.exists())
        {
            String defaultLang = this.metedata.defaultLocale;
            file = new File(path.getAbsolutePath().replace(".yaml", "_" + defaultLang + ".md"));
        }

        if (!file.exists())
        {
            Logs.getInstance().log(Logs.LogType.ERROR, "Failed to load help file defined by: " + path.getName());
            return new MarkdownHelpToHtml(path);
        }

        return new MarkdownHelpToHtml(file);
    }

    public TreeItem<HelpEntry> intoTreeItem()
    {
        TreeItem<HelpEntry> root = new TreeItem<>(this);

        for (var entry : innerEntries)
        {
            root.getChildren().add(entry.intoTreeItem());
        }

        return root;
    }

    @SneakyThrows
    public static HelpEntry introspectHelp(Class<?> clazz)
    {
        URL helpUrl = clazz.getResource("/help");

        if (helpUrl == null) return null;

        File entry = new File(helpUrl.toURI());

        return new HelpEntry(
                entry,
                HelpEntryMetadata.module(clazz.getSimpleName()),
                introspectHelp(entry)
        );
    }

    public static HelpEntry[] introspectHelp(File file)
    {
        File[] entries = file.listFiles(fil -> fil.isFile() && fil.getName().endsWith(".yaml"));

        if (entries == null) return new HelpEntry[0];

        HelpEntry[] helpEntries = new HelpEntry[entries.length];

        for (int i = 0; i < helpEntries.length; i++)
        {
            File entryMetadata = entries[i];
            HelpEntryMetadata metadata;
            
            try (FileReader fileReader = new FileReader(entryMetadata))
            {
                metadata = HelpEntryMetadata.fromYaml(fileReader);
            }
            catch (IOException e)
            {
                Logs.getInstance().log("Failed to read help entryMetadata: " + entryMetadata.getName(), e);
                continue;
            }

            HelpEntry[] innerEntries = metadata.helpEntryType == Type.DIR
                    ? introspectHelp(new File(file, entryMetadata.getName().replace(".yaml", "")))
                    : new HelpEntry[0];

            HelpEntry helpEntry = new HelpEntry(
                    entryMetadata,
                    metadata,
                    innerEntries
            );

            helpEntries[i] = helpEntry;
        }

        return helpEntries;
    }
}
