package org.lebastudios.theroundtable.accounts;

import javafx.application.Application;
import org.lebastudios.theroundtable.config.PreferencesConfigData;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.events.LocalEvent;
import org.lebastudios.theroundtable.themes.Theme;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public final class AccountEvents
{
    public static final LocalEvent<Account> onAccountLogIn = new LocalEvent<>();
    public static final LocalEvent<Account> onAccountLogOutBefore = new LocalEvent<>();
    public static final LocalEvent<Account> onAccountLogOutAfter = new LocalEvent<>();
    
    static {
        onAccountLogIn.addListener(_ -> {
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
