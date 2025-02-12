package org.lebastudios.theroundtable.help;

import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;

public class HelpEntryMetadata
{
    private static final Yaml YAML_PARSER = new Yaml();
    
    public int fileFormatVersion = 1;
    public HelpEntry.Type helpEntryType;
    public String name;
    public String defaultLocale;
    public String[] keywords;
    public String[] relControllers;
    
    public static HelpEntryMetadata module(String name)
    {
        HelpEntryMetadata metadata = new HelpEntryMetadata();
        metadata.helpEntryType = HelpEntry.Type.MODULE;
        metadata.name = name;
        return metadata;
    }
    
    public static HelpEntryMetadata fromYaml(FileReader reader)
    {
        return YAML_PARSER.loadAs(reader, HelpEntryMetadata.class);
    }
}
