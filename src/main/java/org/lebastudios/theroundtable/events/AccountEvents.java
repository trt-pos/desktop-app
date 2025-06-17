package org.lebastudios.theroundtable.events;

import javafx.application.Application;
import org.lebastudios.theroundtable.config.PreferencesConfigData;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.themes.Theme;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public final class AccountEvents
{
    public static final Event1<Account> OnAccountLogIn = new Event1<>();
    public static final Event1<Account> OnAccountLogOutBefore = new Event1<>();
    public static final Event OnAccountLogOutAfter = new Event();
    
    static {
        OnAccountLogIn.addListener(_ -> {
            String styleURL;
            try
            {
                styleURL = new File(new URI(new PreferencesConfigData().load().theme).toURL().getFile()).exists()
                        ? new PreferencesConfigData().load().theme
                        : Theme.DEFAULT.url().toExternalForm();
            }
            catch (MalformedURLException | URISyntaxException e)
            {
                styleURL = Theme.DEFAULT.url().toExternalForm();
            }

            Application.setUserAgentStylesheet(styleURL);
        });
    }
}
