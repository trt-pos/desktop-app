package org.lebastudios.theroundtable;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.accounts.AccountManager;
import org.lebastudios.theroundtable.accounts.AccountStageController;
import org.lebastudios.theroundtable.camelot.CamelotServiceManager;
import org.lebastudios.theroundtable.config.UpdatesConfigData;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.ExceptionDialogController;
import org.lebastudios.theroundtable.env.Directories;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.locale.LangLoader;
import org.lebastudios.theroundtable.locale.LocaleManager;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.plugins.PluginLoader;
import org.lebastudios.theroundtable.setup.SetupStageController;
import org.lebastudios.theroundtable.tasks.Task;
import org.lebastudios.theroundtable.ui.SceneBuilder;
import org.lebastudios.theroundtable.server.CheckAppUpdateTask;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class TheRoundTableApplication extends Application
{
    public static String getAppVersion()
    {
        return CorePlugin.getInstance().getPluginData().pluginVersion;
    }

    public static String getUserDirectory()
    {
        return Directories.homeDir();
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

        // Would like to differenciate between CorePlugin translations and basic app translations
        // cause this is executed twice,
        // one here and another when loading the plugins
        LangLoader.loadLang(CorePlugin.class, LocaleManager.getInstance().getActualLocale());
        
        new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Starting The Round Table");
                
                updateMessage("Starting Camelot");
                executeSubtask(CamelotServiceManager.getInstance().initTask());
                
                updateMessage("Loading plugins");
                executeSubtask(PluginLoader.getInstance().loadPluginsTask());

                updateMessage("Starting database");
                executeSubtask(Database.getInstance().initTask());

                return null;
            }
        }.setOnFailure(e ->
        {
            new ExceptionDialogController(e).instantiate(true);
            System.exit(-1);
        }).execute(true);

        if (!SetupStageController.isSetupDone()) new SetupStageController().instantiate(true);
        
        new AccountStageController().instantiate(true);

        stage.setTitle("The Round Table - " + AccountManager.getInstance().getCurrentLoggedAccountName());
        stage.getIcons().add(CorePlugin.getInstance().getPluginIcon());

        Scene mainScene = new SceneBuilder(new MainStageController().getParent()).build();
        stage.setScene(mainScene);
        stage.show();

        if (AccountManager.getInstance().isAccountAdmin() && new UpdatesConfigData().load().checkUpdates)
        {
            new CheckAppUpdateTask().executeInBackGround();
        }

        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
        {
            AppLifeCicleEvents.OnAppCloseRequest.invoke(e);

            if (!e.isConsumed())
            {
                AppLifeCicleEvents.OnAppClose.invoke(e);
                System.exit(0);
            }
        });
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
}