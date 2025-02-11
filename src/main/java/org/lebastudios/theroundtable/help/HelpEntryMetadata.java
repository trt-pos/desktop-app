package org.lebastudios.theroundtable.help;

import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;

public class HelpEntryMetadata
{
    private static final Yaml YAML_PARSER = new Yaml();
    
    public int fileFormatVersion = 1;
    public HelpEntry.Type helpEntryType;
    public String uiName;
    public String defaultLocale;
    
    public static HelpEntryMetadata module(String uiName)
    {
        HelpEntryMetadata metadata = new HelpEntryMetadata();
        metadata.helpEntryType = HelpEntry.Type.MODULE;
        metadata.uiName = uiName;
        return metadata;
    }
    
    public static HelpEntryMetadata fromYaml(FileReader reader)
    {
        return YAML_PARSER.loadAs(reader, HelpEntryMetadata.class);
    }
}
