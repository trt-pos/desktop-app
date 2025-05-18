package org.lebastudios.theroundtable.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.database.PluginTable;
import org.lebastudios.theroundtable.plugins.Version;

import java.net.URI;

@Entity
@Setter
@Getter
@NoArgsConstructor
@PluginTable(name = "plugin")
public class Plugin
{
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    
    @Column(name = "repo", nullable = false)
    @Convert(converter = URIConverter.class)
    private URI repo;
    
    @Column(name = "version", nullable = false)
    @Convert(converter = VersionConverter.class)
    private Version version;
    
    @Converter
    private static class URIConverter implements AttributeConverter<URI, String>
    {
        @Override
        public String convertToDatabaseColumn(URI uri) {
            return uri != null ? uri.toString() : null;
        }

        @Override
        public URI convertToEntityAttribute(String dbData) {
            return dbData != null ? URI.create(dbData) : null;
        }
    }
    
    @Converter
    private static class VersionConverter implements AttributeConverter<Version, String>
    {
        @Override
        public String convertToDatabaseColumn(Version version) {
            return version != null ? version.toString() : null;
        }

        @Override
        public Version convertToEntityAttribute(String dbData) {
            return dbData != null ? new Version(dbData) : null;
        }
    }
}
