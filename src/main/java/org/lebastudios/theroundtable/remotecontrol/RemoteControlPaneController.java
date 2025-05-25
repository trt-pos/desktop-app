package org.lebastudios.theroundtable.remotecontrol;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.controllers.PaneController;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.entities.AppInstallation;
import org.lebastudios.theroundtable.plugins.Version;
import org.lebastudios.theroundtable.components.IconButton;

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

        menuColumn.setMinWidth(50);
        menuColumn.setMaxWidth(50);

        nameColumn.setCellFactory(TextField2TableCell.forTableColumn());
        subnetColumn.setCellFactory(TextField2TableCell.forTableColumn());
        isMasterColumn.setCellFactory(CheckBoxTableCell.forTableColumn(isMasterColumn));
        
        nameColumn.setOnEditCommit(event ->
        {
            if (!event.getNewValue().matches("[\\w\\s-]+"))
            {
                UIEffects.shakeNode(installationsTableView);
                return;
            }
            
            event.getRowValue().name.set(event.getNewValue());
            
            if (!updateAppInstallationItem(event.getRowValue()))
            {
                event.getRowValue().name.set(event.getOldValue());
            }
        });
        
        subnetColumn.setOnEditCommit(event ->
        {
            if (!event.getNewValue().matches(
                    "^(([12]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])[./]){4}([1-2]?[0-9]|3[0-2])$"))
            {
                UIEffects.shakeNode(installationsTableView);
                return;
            }

            event.getRowValue().subnet.set(event.getNewValue());

            if (!updateAppInstallationItem(event.getRowValue()))
            {
                event.getRowValue().subnet.set(event.getOldValue());
            }
        });
        
        isMasterColumn.setOnEditCommit(event ->
        {
            // Don't allow to uncheck the master installation
            if (event.getOldValue())
            {
                event.getRowValue().isMaster.set(true);
                return;
            }
            
            AppInstallationTableItem actualMaster = installationsTableView.getItems().stream()
                    .filter(i -> i.isMaster.get())
                    .findFirst()
                    .orElseThrow();
            
            actualMaster.isMaster.set(false);
            event.getRowValue().isMaster.set(true);
            
            if (!updateAppInstallationItem(event.getRowValue()) || !updateAppInstallationItem(actualMaster))
            {
                actualMaster.isMaster.set(true);
                event.getRowValue().isMaster.set(false);
            }
        });
        
        menuColumn.setCellFactory(new Callback<>()
        {
            @Override
            public TableCell<AppInstallationTableItem, Void> call(
                    TableColumn<AppInstallationTableItem, Void> param)
            {
                return new TableCell<>()
                {
                    private final IconButton iconButton = new IconButton("control-pane.png");

                    {
                        iconButton.setOnAction(event ->
                        {
                            AppInstallation appInstallation = Database.getInstance().connectQuery(session ->
                            {
                                return session.createQuery(
                                                "from AppInstallation i where i.uuid = :uuid",
                                                AppInstallation.class
                                        ).setParameter("uuid", getTableView().getItems().get(getIndex()).uuid.get())
                                        .uniqueResult();
                            });


                            new InstallationControlStageController(appInstallation)
                                    .setOwner(RemoteControlPaneController.this.getStage())
                                    .instantiate();
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
            if (this.getParent().getParent() != null)
            {
                scheduler.close();
            }

            populateTable();
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void populateTable()
    {
        int selected = installationsTableView.getSelectionModel().getSelectedIndex();

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
        installationsTableView.getSelectionModel().select(selected > installations.size() 
                ? installations.size() - 1 
                : selected
        );
    }

    private boolean updateAppInstallationItem(AppInstallationTableItem item)
    {
        return Database.getInstance().connectTransaction(session ->
        {
            AppInstallation appInstallation = session.get(AppInstallation.class, item.uuid.get());

            if (appInstallation == null) throw new IllegalStateException("appInstallation is null");

            appInstallation.setName(item.name.get());
            appInstallation.setSubnet(item.subnet.get());
            appInstallation.setMaster(item.isMaster.get());

            item.ip.set(appInstallation.getIp());
            
            session.merge(appInstallation);
        });
    }
    
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
