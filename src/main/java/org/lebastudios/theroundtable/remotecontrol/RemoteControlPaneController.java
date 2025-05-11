package org.lebastudios.theroundtable.remotecontrol;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.camelot.converters.StringConverter;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.database.entities.AppInstallation;
import org.lebastudios.theroundtable.env.TrtUUIDReader;
import org.lebastudios.theroundtable.events.CamelotEvent;
import org.lebastudios.theroundtable.plugins.Version;
import org.lebastudios.theroundtable.ui.IconButton;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RemoteControlPaneController extends PaneController<RemoteControlPaneController>
{
    @FXML public TableView2<AppInstallationTableItem> installationsTableView;
    @FXML public TableColumn2<AppInstallationTableItem, String> uuidColumn;
    @FXML public TableColumn2<AppInstallationTableItem, String> nameColumn;
    @FXML public TableColumn2<AppInstallationTableItem, AppInstallation.Status> statusColumn;
    @FXML public TableColumn2<AppInstallationTableItem, Version> versionColumn;
    @FXML public TableColumn2<AppInstallationTableItem, String> ipColumn;
    @FXML public TableColumn2<AppInstallationTableItem, String> subnetColumn;
    @FXML public TableColumn2<AppInstallationTableItem, Boolean> isMasterColumn;
    @FXML public TableColumn2<AppInstallationTableItem, Void> menuColumn;

    @Override
    protected void initialize()
    {
        super.initialize();

        installationsTableView.setColumnResizePolicy(TableView2.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        uuidColumn.setCellValueFactory(cellData -> cellData.getValue().uuid);
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().name);
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().status);
        versionColumn.setCellValueFactory(cellData -> cellData.getValue().version);
        ipColumn.setCellValueFactory(cellData -> cellData.getValue().ip);
        subnetColumn.setCellValueFactory(cellData -> cellData.getValue().subnet);
        isMasterColumn.setCellValueFactory(cellData -> cellData.getValue().isMaster);

        menuColumn.setCellFactory(new Callback<>()
        {
            @Override
            public TableCell<AppInstallationTableItem, Void> call(
                    TableColumn<AppInstallationTableItem, Void> param)
            {
                return new TableCell<>()
                {
                    // TODO: The button should show a menu with options or a new page to manage the
                    //  selected installation
                    private final IconButton iconButton = new IconButton("remote-control.png");

                    {
                        iconButton.setOnAction(event ->
                        {
                            AppInstallationTableItem installation = getTableView().getItems().get(getIndex());
                            SHUTDOWN_APP_EVENT.invoke(new StringConverter(installation.uuid.get()));
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        
                        if (empty)
                        {
                            setGraphic(null);
                        }
                        else
                        {
                            setGraphic(iconButton);
                        }
                    }
                };
            }
        });

        populateTable();

        // Starting job that will update the table every 5 seconds
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() ->
        {
            if (!this.getStage().isShowing())
            {
                scheduler.close();
            }

            populateTable();
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void populateTable()
    {
        installationsTableView.getItems().clear();

        List<AppInstallationTableItem> installations = Database.getInstance().connectQuery(session ->
        {
            return session.createQuery(
                            "from AppInstallation i order by i.createdAt asc",
                            AppInstallation.class
                    ).stream()
                    .map(AppInstallationTableItem::new)
                    .toList();
        });

        installationsTableView.getItems().setAll(installations);
    }

    private static final CamelotEvent<StringConverter> SHUTDOWN_APP_EVENT = new CamelotEvent<>(
            "core-plugin:shutdown",
            new StringConverter()
    )
    {
        {
            this.addListener(converter ->
            {
                if (!converter.getString().equals(new TrtUUIDReader().getTrtUUID())) return;

                TheRoundTableApplication.exitAplication(0);
            });
        }
    };

    public record AppInstallationTableItem(
            SimpleStringProperty uuid,
            SimpleStringProperty name,
            SimpleObjectProperty<AppInstallation.Status> status,
            SimpleObjectProperty<Version> version,
            SimpleStringProperty ip,
            SimpleStringProperty subnet,
            SimpleBooleanProperty isMaster
    )
    {
        public AppInstallationTableItem(AppInstallation appInstallation)
        {
            this(
                    new SimpleStringProperty(appInstallation.getUuid()),
                    new SimpleStringProperty(appInstallation.getName()),
                    new SimpleObjectProperty<>(appInstallation.getStatus()),
                    new SimpleObjectProperty<>(appInstallation.getVersion()),
                    new SimpleStringProperty(appInstallation.getIp()),
                    new SimpleStringProperty(appInstallation.getSubnet()),
                    new SimpleBooleanProperty(appInstallation.isMaster())
            );
        }
    }
}
