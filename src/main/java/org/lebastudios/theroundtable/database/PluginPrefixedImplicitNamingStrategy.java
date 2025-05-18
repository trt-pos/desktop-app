package org.lebastudios.theroundtable.database;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitEntityNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.lebastudios.theroundtable.plugins.PluginLoader;
import org.lebastudios.theroundtable.plugins.PluginsManager;

class PluginPrefixedImplicitNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl
{
    @Override
    public Identifier determinePrimaryTableName(ImplicitEntityNameSource source)
    {
        String tableName;

        try
        {
            Class<?> entityClass = PluginLoader.getInstance().getPluginsClassLoader().loadClass(
                    source.getEntityNaming().getClassName()
            );
            String prefix = PluginsManager.getInstance()
                    .getPluginOf(entityClass).orElseThrow()
                    .getPluginData()
                    .getDbTablePrefix();

            PluginTable pluginTable = entityClass.getAnnotation(PluginTable.class);
            
            String entityName = pluginTable == null ? entityClass.getSimpleName() : pluginTable.name();
            tableName = prefix + "_" + entityName;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }

        return new Identifier(tableName, false);
    }
}
