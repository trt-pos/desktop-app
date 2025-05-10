package org.lebastudios.theroundtable.security;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;

import java.util.Optional;

public class KeyringManager
{
    private static KeyringManager instance;
    
    public static KeyringManager getInstance()
    {
        if (instance == null) instance = new KeyringManager();
        
        return instance;
    }
    
    private final Keyring keyring;
    
    private KeyringManager() 
    {
        Keyring tmpKeyring;
        try
        {
            tmpKeyring = Keyring.create();
        }
        catch (BackendNotSupportedException e)
        {
            Logs.getInstance().log(
                    "Keyring backend not supported",
                    e
            );
            tmpKeyring = null;
        }
        
        keyring = tmpKeyring;
    }
    
    public Optional<String> getSecret(IPlugin plugin, String key) throws PasswordAccessException
    {
        try
        {
            return Optional.of(keyring.getPassword("TheRoundTable", renameKey(plugin, key)));
        }
        catch (PasswordAccessException e)
        {
            if (e.getMessage().contains("No stored credentials match"))
            {
                return Optional.empty();
            }
            else
            {
                throw e;
            }
        }
    }
    
    public void setSecret(IPlugin plugin, String key, String secret) throws PasswordAccessException
    {
        keyring.setPassword("TheRoundTable", renameKey(plugin, key), secret);
    }
    
    public void deleteSecret(IPlugin plugin, String key) throws PasswordAccessException
    {
        keyring.deletePassword("TheRoundTable", renameKey(plugin, key));
    }
    
    public String renameKey(IPlugin plugin, String key)
    {
        return plugin.getPluginData().pluginId + "::" + key;
    }
}
