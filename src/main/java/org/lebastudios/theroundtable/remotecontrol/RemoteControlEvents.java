package org.lebastudios.theroundtable.remotecontrol;

import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.camelot.converters.StringConverter;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.ExceptionDialogController;
import org.lebastudios.theroundtable.entities.AppInstallation;
import org.lebastudios.theroundtable.env.TrtUUIDReader;
import org.lebastudios.theroundtable.events.CamelotEvent;
import org.lebastudios.theroundtable.plugins.PluginSyncronizer;

class RemoteControlEvents
{
    static final RemoteControlEvent SHUTDOWN_APP_EVENT = new RemoteControlEvent("shutdown")
    {
        @Override
        public void execute()
        {
            TheRoundTableApplication.exitAplication(0);
        }
    };

    static final RemoteControlEvent RESTART_APP_EVENT = new RemoteControlEvent("restart")
    {
        @Override
        public void execute()
        {
            Launcher.restartAplication();
        }
    };
    
    static final RemoteControlEvent DISABLE_APP_EVENT = new RemoteControlEvent("disable")
    {
        @Override
        public void execute()
        {
            Database.getInstance().connectTransaction(session ->
            {
                AppInstallation appInstallation = AppInstallation.thisInstalation(session);
                appInstallation.setStatus(AppInstallation.Status.DISABLED);
                session.merge(appInstallation);
            });
            
            TheRoundTableApplication.exitAplication(0);
        }
    };
    
    static final RemoteControlEvent SYNC_PLUGINS_EVENT = new RemoteControlEvent("sync-plugins")
    {
        @Override
        public void execute()
        {
            try
            {
                new PluginSyncronizer().syncWithMaster();
                Launcher.restartAplication();
            }
            catch (Exception e)
            {
                new ExceptionDialogController(e)
                        .instantiate(true);
            }
        }
    };
    
    abstract static class RemoteControlEvent extends CamelotEvent<StringConverter>
    {
        public RemoteControlEvent(String eventName)
        {
            super("core-plugin:remote-control-" + eventName, new StringConverter());

            this.addListener(converter ->
            {
                if (!converter.getString().equals(new TrtUUIDReader().getTrtUUID())) return;

                execute();
            });
        }

        public abstract void execute();
    }
}
