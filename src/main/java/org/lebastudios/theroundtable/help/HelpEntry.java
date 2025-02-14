package org.lebastudios.theroundtable.help;

import javafx.scene.control.TreeItem;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.config.data.PreferencesConfigData;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

record HelpEntry(File path, HelpEntryMetadata metedata, HelpEntry[] innerEntries)
{
    public enum Type
    {
        MODULE, DIR, MD, LINK, FAQ;

        public String getIconName()
        {
            return switch (this)
            {
                case MD -> "md-help-file.png";
                case DIR -> "directory.png";
                case MODULE -> "module.png";
                case LINK -> "link-help-file.png";
                case FAQ -> "faq-help-file.png";
            };
        }
        
        public boolean hasInnerEntries()
        {
            return this == MODULE || this == DIR;
        }
        public boolean hasContentToShow() { return this == MD ||this == FAQ; }
    }

    public HelpEntry filteredByRegex(String regex)
    {
        List<HelpEntry> filteredEntries = new ArrayList<>(innerEntries.length);

        innerEntriesLoop:
        for (var innerEntry : innerEntries)
        {
            HelpEntryMetadata metadata = innerEntry.metedata;
            
            if (metadata.helpEntryType.hasInnerEntries())
            {
                // If the name of the entry matches the regex, add it to the filtered entries with all his childs
                if (LangFileLoader.getTranslation(metadata.name).toLowerCase().matches(regex))
                {
                    filteredEntries.add(innerEntry);
                    continue;
                }
                
                // The name didn't match, so a new entry is created with the filtered inner entries
                HelpEntry filtered = innerEntry.filteredByRegex(regex);
                
                if (filtered.innerEntries().length > 0) 
                {
                    filteredEntries.add(filtered);
                }
                continue;
            }

            // NOTE: The entries are no longer DIRs or MODULEs
            // If the name of the entry matches the regex
            if (LangFileLoader.getTranslation(metadata.name).toLowerCase().matches(regex)) 
            {
                filteredEntries.add(innerEntry);
                continue;
            }
            
            // If any keyword of the entry matches the regex
            String[] keywords = metadata.keywords;
            if (keywords == null) continue;

            for (var keyword : keywords)
            {
                if (keyword.toLowerCase().matches(regex))
                {
                    filteredEntries.add(innerEntry);
                    continue innerEntriesLoop;
                }
            }
        }

        return new HelpEntry(
                path,
                metedata,
                filteredEntries.toArray(new HelpEntry[0])
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
        return intoTreeItem(false);
    }
    
    public TreeItem<HelpEntry> intoTreeItem(boolean expanded)
    {
        TreeItem<HelpEntry> root = new TreeItem<>(this);
        root.setExpanded(expanded);
        
        for (var entry : innerEntries)
        {
            root.getChildren().add(entry.intoTreeItem(expanded));
        }

        return root;
    }

    @SneakyThrows
    public static HelpEntry[] introspectManual(Class<?> clazz, ManualType type)
    {
        URL manualURL = clazz.getResource(type.getResourceFolderName());

        if (manualURL == null) return new HelpEntry[0];

        File entry = new File(manualURL.toURI());

        return introspectManual(entry);
    }

    public static HelpEntry[] introspectManual(File file)
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

            HelpEntry[] innerEntries = metadata.helpEntryType.hasInnerEntries()
                    ? introspectManual(new File(file, entryMetadata.getName().replace(".yaml", "")))
                    : new HelpEntry[0];

            HelpEntry helpEntry = new HelpEntry(
                    entryMetadata,
                    metadata,
                    innerEntries
            );

            helpEntries[i] = helpEntry;
        }
        
        Arrays.sort(helpEntries, Comparator.comparingInt((HelpEntry o) -> o.metedata.helpEntryType.ordinal())
                .thenComparing(o -> LangFileLoader.getTranslation(o.metedata.name)));
        
        return helpEntries;
    }
}
