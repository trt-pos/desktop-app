package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.camelot.CamelotServiceManager;

import java.io.IOException;
import java.net.Socket;

public class CamelotServerConfigPaneController extends ConfigPaneController<CamelotServerConfigData>
{
    @FXML public TextField serverAddress;
    @FXML public TextField serverPort;
    @FXML public CheckBox defaultConfigCheckbox;
    @FXML public Label serviceStatus;

    public CamelotServerConfigPaneController()
    {
        super(new CamelotServerConfigData(), "Camelot", "core:server.png");
    }

    @Override
    @FXML
    protected void initialize()
    {
        defaultConfigCheckbox.selectedProperty().addListener((_, _, newValue) ->
        {
            serverAddress.setDisable(newValue);
            serverPort.setDisable(newValue);

            if (newValue)
            {
                CamelotServerConfigData defaultConfigData = new CamelotServerConfigData();
                
                if (CamelotServiceManager.getInstance().isRunning())
                {
                    defaultConfigData.port = CamelotServiceManager.getInstance().getServerPort();
                }
                
                updateUI(defaultConfigData);
            }
        });

        super.initialize();
    }

    @Override
    public void updateConfigData(CamelotServerConfigData configData)
    {
        configData.defaultConfig = defaultConfigCheckbox.isSelected();
        configData.host = serverAddress.getText();
        configData.port = Integer.parseInt(serverPort.getText());
    }

    @Override
    public void updateUI(CamelotServerConfigData configData)
    {
        defaultConfigCheckbox.setSelected(configData.defaultConfig);
        serverAddress.setText(configData.host);
        serverPort.setText(Integer.toString(configData.port));

        boolean isRunning = CamelotServiceManager.getInstance().isRunning();

        String text;
        String style;

        if (isRunning)
        {
            int port = CamelotServiceManager.getInstance().getServerPort();
            text = "The local Camelot server is running on port " + port;
            style = "-fx-text-fill: green; -fx-font-size: 10";
        }
        else
        {
            text = "The local Camelot server is not running";
            style = "-fx-text-fill: red; -fx-font-size: 10";
        }

        serviceStatus.setText(text);
        serviceStatus.setStyle(style);
    }

    @Override
    public ValidationResult validate()
    {
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

        if (!serverPort.getText().matches("[0-9]+"))
        {
            UIEffects.shakeNode(serverPort);
            return ValidationResult.invalid("Port must be a number");
        }

        try (Socket _ = new Socket(
                serverAddress.getText(),
                Integer.parseInt(serverPort.getText())
        )) {}
        catch (IOException e)
        {
            UIEffects.shakeNode(serverAddress);
            UIEffects.shakeNode(serverPort);
            return ValidationResult.invalid("Cannot connect to server: " + e.getMessage());
        }
        
        return ValidationResult.valid();
    }

    @SneakyThrows
    @Override
    public void onSave(CamelotServerConfigData configData)
    {
        CamelotServiceManager.getInstance()
                .reloadTask(configData)
                .execute(true);
    }
}
