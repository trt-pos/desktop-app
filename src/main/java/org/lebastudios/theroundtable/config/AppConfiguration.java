package org.lebastudios.theroundtable.config;

import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.env.Directories;

import java.io.File;

public class AppConfiguration
{
    public static File getGlobalDir()
    {
        return new File(get(), "global");
    }

    public static File get()
    {
        return new File(Directories.getHomeDir() + "/config");
    }

    public static File getUserDir()
    {
        var accountName = AccountManager.getInstance().getCurrentLoggedAccountName();

        return new File(get(), accountName);
    }
}
