package org.lebastudios.theroundtable.config;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.InformationTextDialogController;
import org.lebastudios.theroundtable.locale.LangFileLoader;
import org.lebastudios.theroundtable.tasks.Task;
import org.lebastudios.theroundtable.ui.SwitcheableNodePane;

import java.io.File;
import java.sql.Connection;
import java.util.Arrays;

public class DatabaseConfigPaneController extends ConfigPaneController<DatabaseConfigData>
{
    @FXML public CheckBox enableRemoteDb;
    @FXML public SwitcheableNodePane formContainer;
    @FXML public Label errorLabel;

    @FXML public Node localDbSection;
    @FXML public Node remoteDbSection;

    @FXML public Label databasesDirectory;
    @FXML public CheckBox enableBackups;
    @FXML public Node backupSection;
    @FXML public Label databasesBackupDirectory;
    @FXML public TextField numMaxBackups;

    @FXML public TextField remoteDbHost;
    @FXML public TextField remoteDbPort;
    @FXML public TextField remoteDbUser;
    @FXML public PasswordField remoteDbPassword;
    @FXML public TextField remoteDbName;
    

    private DatabaseConfigData oldConfig;

    public DatabaseConfigPaneController()
    {
        super(new DatabaseConfigData(), LangFileLoader.getTranslation("word.database"), "database.png");

        oldConfig = new DatabaseConfigData().load();
    }

    @Override
    protected void initialize()
    {
        ((Pane) remoteDbSection.getParent()).getChildren().remove(remoteDbSection);
        ((Pane) localDbSection.getParent()).getChildren().remove(localDbSection);
        
        enableRemoteDb.selectedProperty().addListener((_, _, newValue) ->
        {
            formContainer.switchContent(newValue ? remoteDbSection : localDbSection);
        });
        
        formContainer.switchContent(localDbSection);
        
        backupSection.disableProperty().bind(enableBackups.selectedProperty().not());
        
        super.initialize();
    }
    
    @Override
    public void updateUI(DatabaseConfigData configData)
    {
        enableRemoteDb.setSelected(configData.enableRemoteDb);
        
        databasesDirectory.setText(configData.databaseFolder);
        enableBackups.setSelected(configData.enableBackups);
        databasesBackupDirectory.setText(configData.backupFolder);
        numMaxBackups.setText(configData.numMaxBackups + "");

        DatabaseConfigData.RemoteDbData remoteDbData = configData.remoteDbData;

        if (remoteDbData != null)
        {
            remoteDbHost.setText(remoteDbData.host);
            remoteDbPort.setText(remoteDbData.port);
            remoteDbUser.setText(remoteDbData.user);
            remoteDbPassword.setText(remoteDbData.password);
            remoteDbName.setText(remoteDbData.database);
        }
    }

    @Override
    public void updateConfigData(DatabaseConfigData configData)
    {
        // Saving remote db configuration 
        {
            configData.enableRemoteDb = enableRemoteDb.isSelected();

            var remoteDbData = new DatabaseConfigData.RemoteDbData();

            remoteDbData.host = remoteDbHost.getText();
            remoteDbData.port = remoteDbPort.getText();
            remoteDbData.user = remoteDbUser.getText();
            remoteDbData.password = remoteDbPassword.getText();
            remoteDbData.database = remoteDbName.getText();

            configData.remoteDbData = remoteDbData;
        }

        // Applying local db changes even if remote db is enabled to avoid bugs
        {
            // When database directory changes
            try
            {
                updateDatabaseDirectory(configData); // Side effect: Reloads and moves SQLite db file
                updateBackupDirectory(configData); // Side effect: Moves backup files
            }
            catch (Exception e)
            {
                new InformationTextDialogController("ERROR: " + e.getMessage()).instantiate();
                return;
            }

            configData.enableBackups = enableBackups.isSelected();
            configData.numMaxBackups = Integer.parseInt(numMaxBackups.getText());
        }
    }

    @Override
    public boolean validate()
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

        DatabaseConfigData configData = new DatabaseConfigData();
        updateConfigData(configData);

        try (Connection _ = configData.getConnection())
        {
            return true;
        }
        catch (Exception e)
        {
            errorLabel.setText(e.getMessage());
            
            UIEffects.shakeNode(configData.enableRemoteDb
                    ? remoteDbSection
                    : localDbSection);
            return false;
        }
    }

    @Override
    public void onSave(DatabaseConfigData configData)
    {
        if (configData.enableBackups && !configData.enableRemoteDb)
        {
            Database.getInstance().initBackup();
        }
        else
        {
            Database.getInstance().stopBackup();
        }

        new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Reloading database connections");

                updateMessage("Connecting to the new database");
                executeSubtask(Database.getInstance().reloadTask());

                // No need to migrate if the database is the same
                if (configData.isSameDatabase(oldConfig)) return null;

                updateMessage("Migrating accounts");
                try (Connection newDbConnection = configData.getConnection();
                     Connection oldDbConnection = oldConfig.getConnection())
                {
                    final Exception migrationError =
                            Database.getInstance().migrateTables(oldDbConnection, newDbConnection);
                    if (migrationError != null)
                    {
                        updateMessage("Migration failed. Rolling back changes");
                        new InformationTextDialogController("Migration failed\n" + migrationError).instantiate();
                        oldConfig.save();
                        updateUI(oldConfig);
                        // If the migration failed, 
                        // we roll back the configuration and reload the db connections
                        executeSubtask(Database.getInstance().reloadTask());
                        return null;
                    }
                }

                oldConfig = configData;
                return null;
            }
        }.setOnFailure(_ ->
        {
            oldConfig.save();
            updateUI(oldConfig);
        }).execute(true);
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

            Database.getInstance().reloadTask().execute(true);
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
    public void selectDatabasesBackupDirectory(ActionEvent actionEvent)
    {
        File path = getDirectoryChooser("Select Backup Directory").showDialog(getStage());
        if (path == null) return;

        databasesBackupDirectory.setText(path.getAbsolutePath());
    }

    @FXML
    public void selectDatabasesDirectory(ActionEvent actionEvent)
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
}
