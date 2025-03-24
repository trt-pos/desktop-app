package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.camelot.CamelotClient;
import org.lebastudios.theroundtable.camelot.CamelotServiceManager;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;

public class CamelotServerConfigPaneController extends ConfigPaneController<CamelotServerConfigData>
{
    @FXML private TextField clientName;
    @FXML private TextField serverAddress;
    @FXML private TextField serverPort;
    @FXML private CheckBox defaultConfigCheckbox;

    public CamelotServerConfigPaneController()
    {
        super(new CamelotServerConfigData(), "Camelot", "server.png");
    }

    @Override
    @FXML
    protected void initialize()
    {
        defaultConfigCheckbox.selectedProperty().addListener((_, _, newValue) ->
        {
            clientName.setDisable(newValue);
            serverAddress.setDisable(newValue);
            serverPort.setDisable(newValue);

            if (newValue) updateUI(new CamelotServerConfigData());
        });

        super.initialize();
    }

    @Override
    public void updateConfigData(CamelotServerConfigData configData)
    {
        configData.defaultConfig = defaultConfigCheckbox.isSelected();
        configData.clientName = clientName.getText();
        configData.host = serverAddress.getText();
        configData.port = Integer.parseInt(serverPort.getText());
    }

    @Override
    public void updateUI(CamelotServerConfigData configData)
    {
        defaultConfigCheckbox.setSelected(configData.defaultConfig);
        clientName.setText(configData.clientName);
        serverAddress.setText(configData.host);
        serverPort.setText(Integer.toString(configData.port));
    }

    @Override
    public boolean validate()
    {
        clientName.setText(clientName.getText().trim());
        serverAddress.setText(serverAddress.getText().trim());
        serverPort.setText(serverPort.getText().trim());

        if (serverAddress.getText().isBlank())
        {
            serverAddress.setText("localhost");
        }

        if (serverPort.getText().isBlank())
        {
            serverPort.setText("1237");
        }

        if (!clientName.getText().matches("[a-zA-Z0-9_.]*"))
        {
            UIEffects.shakeNode(clientName);
            return false;
        }

        if (!serverPort.getText().matches("[0-9]+"))
        {
            UIEffects.shakeNode(serverPort);
            return false;
        }

        boolean[] valid = {false};

        try (CamelotClient client = new CamelotClient("tmp", serverAddress.getText(),
                Integer.parseInt(serverPort.getText())))
        {
            Task<Void> task = client.validateTask()
                    .setCancelable(true);

            task.setOnSucceeded(_ -> valid[0] = true);
            task.execute(true);
        }
        catch (Exception e)
        {
            Logs.getInstance().log(
                    "Error closing temporal camelot client",
                    e
            );
        }

        if (!valid[0]) 
        {
            UIEffects.shakeNode(serverAddress);
            UIEffects.shakeNode(serverPort);
            return false;
        }
        
        return true;
    }

    @SneakyThrows
    @Override
    public void onSave(CamelotServerConfigData configData)
    {
        CamelotServiceManager.getInstance().reloadTask(configData)
                .setCancelable(true)
                .execute(true);
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }
}
