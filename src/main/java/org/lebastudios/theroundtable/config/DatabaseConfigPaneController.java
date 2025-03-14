package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.config.data.DatabaseConfigData;
import org.lebastudios.theroundtable.config.data.JSONFile;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.InformationTextDialogController;
import org.lebastudios.theroundtable.logs.Logs;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.Arrays;

public class DatabaseConfigPaneController extends SettingsPaneController
{
    @FXML private CheckBox enableRemoteDb;

    @FXML private Node localDbSection;
    @FXML private Node remoteDbSection;

    @FXML private Label databasesDirectory;
    @FXML private CheckBox enableBackups;
    @FXML private Node backupSection;
    @FXML private Label databasesBackupDirectory;
    @FXML private TextField numMaxBackups;
    
    @FXML private TextField remoteDbHost;
    @FXML private TextField remoteDbPort;
    @FXML private TextField remoteDbUser;
    @FXML private TextField remoteDbPassword;
    @FXML private TextField remoteDbName;

    @Override
    @FXML
    protected void initialize()
    {
        var data = new JSONFile<>(DatabaseConfigData.class).get();

        databasesDirectory.setText(data.databaseFolder);
        enableBackups.setSelected(data.enableBackups);
        databasesBackupDirectory.setText(data.backupFolder);
        numMaxBackups.setText(new JSONFile<>(DatabaseConfigData.class).get().numMaxBackups + "");

        DatabaseConfigData.RemoteDbData remoteDbData = data.remoteDbData;

        if (remoteDbData != null)
        {
            remoteDbHost.setText(remoteDbData.host);
            remoteDbPort.setText(remoteDbData.port);
            remoteDbUser.setText(remoteDbData.user);
            remoteDbPassword.setText(remoteDbData.password);
            remoteDbName.setText(remoteDbData.database);
        }

        backupSection.disableProperty().bind(enableBackups.selectedProperty().not());
        remoteDbSection.disableProperty().bind(enableRemoteDb.selectedProperty().not());
        localDbSection.disableProperty().bind(enableRemoteDb.selectedProperty());

        enableRemoteDb.setSelected(data.enableRemoteDb);
    }

    @Override
    public void apply()
    {
        if (!validateValues()) return;

        var data = new JSONFile<>(DatabaseConfigData.class);

        // Saving remote db configuration 
        {
            data.get().enableRemoteDb = enableRemoteDb.isSelected();

            var remoteDbData = new DatabaseConfigData.RemoteDbData();

            remoteDbData.host = remoteDbHost.getText();
            remoteDbData.port = remoteDbPort.getText();
            remoteDbData.user = remoteDbUser.getText();
            remoteDbData.password = remoteDbPassword.getText();
            remoteDbData.database = remoteDbName.getText();

            data.get().remoteDbData = remoteDbData;
        }

        // Applying local db changes even if remote db is enabled to avois bugs
        {
            // When database directory changes
            try
            {
                updateDatabaseDirectory(data.get()); // Side effect: Reloads and moves SQLite db file
                updateBackupDirectory(data.get()); // Side effect: Moves backup files
            }
            catch (Exception e)
            {
                new InformationTextDialogController("ERROR: " + e.getMessage()).instantiate();
                return;
            }

            data.get().enableBackups = enableBackups.isSelected();
            data.get().numMaxBackups = Integer.parseInt(numMaxBackups.getText());
        }

        var oldConf = new JSONFile<>(DatabaseConfigData.class);
        
        // Validating the db connection values and migrate the tables
        try (Connection newDbConnection = data.get().getConnection();
            Connection oldDbConnection = oldConf.get().getConnection())
        {
            // Saving the new conf so the reloadDatabase knows what parametters to use
            data.save();
            
            // TODO: Start a task pane and convert this into an AppTask
            // Reload should happend first so the plugins can create the db structure in the new database
            if (!data.get().getJdbcUrl().equals(oldConf.get().getJdbcUrl())) 
            {
                Database.getInstance().reloadDatabase();

                final Exception migrationError = Database.getInstance().migrateTables(oldDbConnection, newDbConnection);
                if (migrationError != null)
                {
                    new InformationTextDialogController("Migration failed\n" + migrationError).instantiate();
                    oldConf.save(); // If the migration failed, we roll back the configuration and reload the db connections
                    Database.getInstance().reloadDatabase();
                    return;
                }
            }
            
            if (data.get().enableBackups && !data.get().enableRemoteDb)
            {
                Database.getInstance().initBackup();
            }
            else
            {
                Database.getInstance().stopBackup();
            }
        }
        catch (Exception e)
        {
            oldConf.save();
            Database.getInstance().reloadDatabase();
            Logs.getInstance().log("Couldn't validate the new db conn string", e);

            UIEffects.shakeNode(data.get().enableRemoteDb
                    ? remoteDbSection
                    : localDbSection);
        }
    }

    private boolean validateValues()
    {
        try
        {
            int numMaxBackupsValue = Integer.parseInt(numMaxBackups.getText());

            if (numMaxBackupsValue < 1) throw new IllegalStateException();

        }
        catch (Exception exception)
        {
            UIEffects.shakeNode(numMaxBackups);
            return false;
        }

        if (enableRemoteDb.isSelected())
        {
            if (!remoteDbPort.getText().matches("\\d{1,5}"))
            {
                UIEffects.shakeNode(remoteDbPort);
                return false;
            }
        }

        return true;
    }

    private void updateDatabaseDirectory(DatabaseConfigData data)
    {
        if (!databasesDirectory.getText().equals(data.databaseFolder))
        {
            File oldDirectory = new File(data.databaseFolder);
            File newDirectory = new File(databasesDirectory.getText());

            if (!newDirectory.exists() && !newDirectory.mkdirs())
            {
                throw new RuntimeException("Failed to create new database directory.");
            }

            File databaseFile = data.getSQLiteDatabaseFile();

            if (!databaseFile.exists()) return;

            databaseFile.renameTo(new File(newDirectory.getAbsolutePath(), databaseFile.getName()));

            data.databaseFolder = databasesDirectory.getText();

            try
            {
                if (oldDirectory.list().length == 0) oldDirectory.delete();
            }
            catch (Exception ignore) {}

            Database.getInstance().reloadDatabase();
        }
    }

    private void updateBackupDirectory(DatabaseConfigData data)
    {
        if (!databasesBackupDirectory.getText().equals(data.backupFolder))
        {
            File oldDirectory = new File(data.backupFolder);
            File newDirectory = new File(databasesBackupDirectory.getText());

            if (!newDirectory.exists() && !newDirectory.mkdirs())
            {
                throw new RuntimeException("Failed to create new backup directory.");
            }

            if (!oldDirectory.exists()) return;

            Arrays.stream(oldDirectory.listFiles())
                    .filter(file -> file.isFile() && file.getName().endsWith(".zip"))
                    .forEach(file -> file.renameTo(new File(newDirectory.getAbsolutePath(), file.getName())));

            data.backupFolder = databasesBackupDirectory.getText();

            try
            {
                if (oldDirectory.list().length == 0) oldDirectory.delete();
            }
            catch (Exception ignore) {}
        }
    }

    @FXML
    private void selectDatabasesBackupDirectory(ActionEvent actionEvent)
    {
        File path = getDirectoryChooser("Select Backup Directory").showDialog(getStage());
        if (path == null) return;

        databasesBackupDirectory.setText(path.getAbsolutePath());
    }

    @FXML
    private void selectDatabasesDirectory(ActionEvent actionEvent)
    {
        File path = getDirectoryChooser("Select Databases Directory").showDialog(getStage());
        if (path == null) return;

        databasesDirectory.setText(path.getAbsolutePath());
    }

    private DirectoryChooser getDirectoryChooser(String title)
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(TheRoundTableApplication.getUserDirectory()));
        directoryChooser.setTitle(title);
        return directoryChooser;
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }

    @Override
    public boolean hasFXMLControllerDefined()
    {
        return true;
    }

    @Override
    public URL getFXML()
    {
        return AboutConfigPaneController.class.getResource("databaseConfigPane.fxml");
    }
}
