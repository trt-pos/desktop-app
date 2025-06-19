package org.lebastudios.theroundtable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.accounts.AccountStageController;
import org.lebastudios.theroundtable.accounts.PrivilegeScalationStageController;
import org.lebastudios.theroundtable.camelot.CamelotServiceManager;
import org.lebastudios.theroundtable.communications.FileTransferServiceManager;
import org.lebastudios.theroundtable.config.PreferencesConfigData;
import org.lebastudios.theroundtable.config.UpdatesConfigData;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.ConfirmationTextDialogController;
import org.lebastudios.theroundtable.dialogs.ExceptionDialogController;
import org.lebastudios.theroundtable.entities.Account;
import org.lebastudios.theroundtable.entities.AppInstallation;
import org.lebastudios.theroundtable.entities.Plugin;
import org.lebastudios.theroundtable.env.TrtUUIDReader;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.locale.LocaleManager;
import org.lebastudios.theroundtable.locale.Translator;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.*;
import org.lebastudios.theroundtable.server.CheckAppUpdateTask;
import org.lebastudios.theroundtable.setup.SetupStageController;
import org.lebastudios.theroundtable.themes.Theme;
import org.lebastudios.theroundtable.tasks.MajorVersionMigratorTask;
import org.lebastudios.theroundtable.tasks.Task;
import org.lebastudios.theroundtable.components.SceneBuilder;

import java.io.File;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class TheRoundTableApplication extends Application
{
    public static String getAppVersion()
    {
        return CorePlugin.getInstance().getPluginData().version;
    }

    public static String getAppDirectory()
    {
        return new File(CorePlugin.class.getProtectionDomain().getCodeSource()
                .getLocation().getFile()).getParentFile().getParent();
    }

    @SneakyThrows
    @Override
    public void start(Stage stage)
    {
        Class.forName("org.mariadb.jdbc.Driver");
        String styleURL = new File(new URI(new PreferencesConfigData().load().theme).toURL().getFile()).exists() 
                ? new PreferencesConfigData().load().theme
                : Theme.DEFAULT.url().toExternalForm();

        Application.setUserAgentStylesheet(styleURL);
        
        // Would like to differenciate between CorePlugin translations and basic app translations
        // cause this is executed twice,
        // one here and another when loading the plugins.
        // Maybe the solution is to create a separated
        // module for the core plugin and use TRT as a framework
        Translator.getInstance().loadT(CorePlugin.class, LocaleManager.getInstance().getActualLocale());

        // Starting Camelot Service -> Plugins -> Database
        // This order cannot be changed, loading the plugins updates the database so hibernate
        // needs to wait for the plugins to do their thing
        new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Starting The Round Table");

                updateMessage("Starting database");
                executeSubtask(Database.getInstance().initTask());

                updateMessage("Starting file transfer service");
                executeSubtask(FileTransferServiceManager.getInstance().initTask());

                // Checking if the app installation is registered, checking if 
                // the app is disabled and, if not, updating the state of the installation
                updateMessage("Checking for the installation state");
                Database.getInstance().connectTransaction(session ->
                {
                    if (AppInstallation.thisInstalation(session) == null)
                    {
                        AppInstallation appInstallation = AppInstallation.defaultInstalation();
                        appInstallation.setMaster(
                                session.createQuery("select count(*) " +
                                                "from AppInstallation i " +
                                                "where i.isMaster = true",
                                        Long.class).getSingleResult() == 0);
                        session.persist(appInstallation);
                    }
                });
                boolean result = Database.getInstance().connectQuery(session ->
                {
                    AppInstallation thisInstalation = AppInstallation.thisInstalation(session);

                    if (thisInstalation.getStatus() == AppInstallation.Status.DISABLED)
                    {
                        AtomicBoolean isAdmin = new AtomicBoolean(false);

                        TheRoundTableApplication.executeInFxThreadAndWait(() ->
                        {
                            new PrivilegeScalationStageController(Account.AccountType.ADMIN, isAdmin::set)
                                    .setOwner(stage)
                                    .instantiate(true);
                        });

                        return isAdmin.get();
                    }

                    return true;
                });

                if (!result)
                {
                    TheRoundTableApplication.exitAplication(0);
                    return null;
                }

                updateMessage("Checking master state");
                result = Database.getInstance().connectQuery(session ->
                {
                    AppInstallation master = session.createQuery(
                                    "from AppInstallation i where i.isMaster = true",
                                    AppInstallation.class)
                            .getSingleResultOrNull();

                    if (master == null)
                    {
                        AtomicBoolean response = new AtomicBoolean(false);
                        new ConfirmationTextDialogController(
                                "There is no master in this network, " +
                                        "continue and make this installation the master?",
                                response::set
                        ).instantiate(true);

                        if (response.get())
                        {
                            new PrivilegeScalationStageController(
                                    Account.AccountType.ADMIN,
                                    response::set
                            ).instantiate(true);

                            if (response.get())
                            {
                                Database.getInstance().connectTransaction(session1 ->
                                {
                                    AppInstallation installation = AppInstallation.thisInstalation(session1);
                                    installation.setMaster(true);
                                    session1.persist(installation);
                                });
                            }
                            
                            return response.get();
                        }
                        else
                        {
                            return false;
                        }
                    }
                    
                    if (master.getUuid().equals(new TrtUUIDReader().getTrtUUID()))
                    {
                        return true;
                    }

                    if (master.getStatus() != AppInstallation.Status.ACTIVE)
                    {
                        AtomicBoolean response = new AtomicBoolean(false);
                        new ConfirmationTextDialogController(
                                "The master installation isn't active, continue?",
                                response::set
                        ).instantiate(true);

                        if (response.get())
                        {
                            new PrivilegeScalationStageController(
                                    Account.AccountType.ADMIN,
                                    response::set
                            ).instantiate(true);

                            return response.get();
                        }
                        else
                        {
                            return false;
                        }
                    }

                    return true;
                });

                if (!result)
                {
                    TheRoundTableApplication.exitAplication(0);
                    return null;
                }

                updateMessage("Sync plugins");
                new PluginSyncronizer().syncWithMaster();
                
                updateMessage("Starting Camelot");
                executeSubtask(CamelotServiceManager.getInstance().initTask());

                updateMessage("Loading plugins");
                executeSubtask(PluginLoader.getInstance().loadPluginsTask());

                updateMessage("Reloading database");
                executeSubtask(Database.getInstance().reloadTask());
                
                updateMessage("Initializing plugins");
                for (IPlugin plugin : PluginsManager.getInstance().getLoadedPlugins())
                {
                    plugin.initialize();
                }
                
                updateMessage("Updating state");
                Database.getInstance().connectTransaction(session ->
                {
                    AppInstallation thisInstalation = AppInstallation.thisInstalation(session);
                    thisInstalation.setStatus(AppInstallation.Status.ACTIVE);

                    Version actualVersion = new Version(TheRoundTableApplication.getAppVersion());
                    Version lastVersion = thisInstalation.getVersion();

                    if (lastVersion.isLessThan(actualVersion))
                    {
                        new MajorVersionMigratorTask(lastVersion.getMajor(), actualVersion.getMajor()).execute(true);
                    }

                    thisInstalation.setVersion(actualVersion);
                    session.merge(thisInstalation);

                    if (!thisInstalation.isMaster()) return;

                    PluginsManager.getInstance().getInstalledPlugins()
                            .stream().filter(plugin ->
                            {
                                // We ignore the Core plugin bcs its version represents the installation
                                // version and cannot be syncing the same way a regular plugin does
                                return !plugin.getPluginData().id.equals(
                                        CorePlugin.getInstance().getPluginData().id);
                            })
                            .forEach(pluign ->
                            {
                                Plugin plugin = session.get(Plugin.class, pluign.getPluginData().id);

                                if (plugin == null)
                                {
                                    plugin = new Plugin();
                                    plugin.setId(pluign.getPluginData().id);
                                    plugin.setRepo(URI.create(pluign.getPluginRepoMetadata().url));
                                    plugin.setVersion(new Version(pluign.getPluginData().version));
                                    session.persist(plugin);
                                }
                                else
                                {
                                    plugin.setVersion(new Version(pluign.getPluginData().version));
                                    session.merge(plugin);
                                }
                            });
                });

                return null;
            }
        }.setOnFailure(e ->
        {
            new ExceptionDialogController(e).instantiate(true);
            TheRoundTableApplication.exitAplication(1);
        }).execute(true);

        if (!SetupStageController.isSetupDone()) new SetupStageController().instantiate(true);

        new AccountStageController().instantiate(true);

        stage.setTitle("The Round Table - " + AccountManager.getInstance().getCurrentLoggedAccountName());
        stage.getIcons().add(CorePlugin.getInstance().getPluginIcon());

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
        {
            AppLifeCicleEvents.OnAppCloseRequest.invoke(e);

            if (!e.isConsumed())
            {
                TheRoundTableApplication.exitAplication(0);
            }
        });

        Scene mainScene = new SceneBuilder(new MainStageController().getParent()).build();
        stage.setScene(mainScene);
        stage.show();

        if (AccountManager.getInstance().isAccountAdmin() && new UpdatesConfigData().load().checkUpdates)
        {
            new CheckAppUpdateTask().executeInBackGround();
        }
    }

    public static void executeInFxThreadAndWait(Runnable runnable)
    {
        if (Platform.isFxApplicationThread())
        {
            runnable.run();
            return;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        Platform.runLater(() ->
        {
            try
            {
                runnable.run();
                future.complete(null);
            }
            catch (Exception ex)
            {
                Logs.getInstance().log(
                        "Error executing blocking action inside a task",
                        ex
                );
                future.completeExceptionally(ex);
            }
        });

        future.join();
    }

    public static void exitAplication(int code)
    {
        Platform.exit();
        System.exit(code);
    }
}