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
        MODULE, DIR, MD, FAQ;

        public String getIconName()
        {
            return switch (this)
            {
                case MD -> "md-help-file.png";
                case DIR -> "directory.png";
                case MODULE -> "module.png";
                case FAQ -> "faq-help-file.png";
            };
        }
        
        public boolean hasInnerEntries()
        {
            return this == MODULE || this == DIR;
        }
        public boolean hasContentToShow() { return this == MD ||this == FAQ; }
    }

    public HelpEntry filteredByKeywords(String text)
    {
        List<HelpEntry> filteredEntries = new ArrayList<>(innerEntries.length);

        for (var innerEntry : innerEntries)
        {
            // TODO: MODULE entries should be treated as DIRs
            if (innerEntry.metedata.helpEntryType == Type.DIR)
            {
                HelpEntry filtered = innerEntry.filteredByKeywords(text);
                
                if (filtered.innerEntries().length > 0) 
                {
                    filteredEntries.add(filtered);
                }
                continue;
            }

            String[] keywords = innerEntry.metedata.keywords;
            if (keywords == null) continue;

            boolean keywordMatchFound = false;
            int i = 0;

            while (!(keywordMatchFound || i >= keywords.length))
            {
                String keyword = keywords[i].toLowerCase();
                if (keyword.matches(text))
                {
                    filteredEntries.add(innerEntry.filteredByKeywords(text));
                    keywordMatchFound = true;
                }

                i++;
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
        TreeItem<HelpEntry> root = new TreeItem<>(this);

        for (var entry : innerEntries)
        {
            root.getChildren().add(entry.intoTreeItem());
        }

        return root;
    }

    @SneakyThrows
    public static HelpEntry[] introspectHelp(Class<?> clazz)
    {
        URL helpUrl = clazz.getResource("user-manual");

        if (helpUrl == null) return new HelpEntry[0];

        File entry = new File(helpUrl.toURI());

        return introspectHelp(entry);
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

            HelpEntry[] innerEntries = metadata.helpEntryType.hasInnerEntries()
                    ? introspectHelp(new File(file, entryMetadata.getName().replace(".yaml", "")))
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
