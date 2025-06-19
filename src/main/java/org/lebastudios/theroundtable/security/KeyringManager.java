package org.lebastudios.theroundtable.security;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.IPlugin;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
    
    public Optional<String> getSecret(IPlugin plugin, String key)
    {
        try
        {
            return Optional.of(keyring.getPassword("TheRoundTable", normalizeKey(plugin, key)));
        }
        catch (PasswordAccessException e)
        {
            if (e.getMessage().contains("No stored credentials match"))
            {
                return Optional.empty();
            }
            else
            {
                if (!showAccesKeyRingError(e))
                {
                    TheRoundTableApplication.exitAplication(1);
                    return Optional.empty();
                }
                
                return getSecret(plugin, key);
            }
        }
    }
    
    public void setSecret(IPlugin plugin, String key, String secret)
    {
        try
        {
            keyring.setPassword("TheRoundTable", normalizeKey(plugin, key), secret);
        }
        catch (PasswordAccessException e)
        {
            if (!showAccesKeyRingError(e))
            {
                TheRoundTableApplication.exitAplication(1);
                return;
            }
            
            setSecret(plugin, key, secret);
        }
    }
    
    public void deleteSecret(IPlugin plugin, String key)
    {
        try
        {
            keyring.deletePassword("TheRoundTable", normalizeKey(plugin, key));
        }
        catch (PasswordAccessException e)
        {
            if (!showAccesKeyRingError(e))
            {
                TheRoundTableApplication.exitAplication(1);
                return;
            }
            
            deleteSecret(plugin, key);
        }
    }
    
    public String normalizeKey(IPlugin plugin, String key)
    {
        return plugin.getPluginData().id + "::" + key;
    }
    
    private boolean showAccesKeyRingError(Exception e)
    {
        Logs.getInstance().log(
                "Couldn't access the keyring",
                e
        );

        AtomicBoolean response = new AtomicBoolean(false);

        TheRoundTableApplication.executeInFxThreadAndWait(() ->
        {
            new ConfirmationTextDialogController(
                    "Couldn't access the keyring. Do you want to retry?",
                    response::set
            ).instantiate(true);
        });
        
        return response.get();
    }
}
