package org.lebastudios.theroundtable.config;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.camelot.CamelotServiceManager;

import java.net.Socket;

public class CamelotServerConfigPaneController extends ConfigPaneController<CamelotServerConfigData>
{
    @FXML private TextField clientName;
    @FXML private TextField serverAddress;
    @FXML private TextField serverPort;

    public CamelotServerConfigPaneController()
    {
        super(new CamelotServerConfigData());
    }

    @Override
    public void updateConfigData(CamelotServerConfigData configData)
    {
        configData.clientName = clientName.getText();
        configData.host = serverAddress.getText();
        configData.port = Integer.parseInt(serverPort.getText());
    }

    @Override
    public void updateUI(CamelotServerConfigData configData)
    {
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

        if (!clientName.getText().matches("[a-zA-Z0-9_-]+"))
        {
            UIEffects.shakeNode(clientName);
            return false;
        }

        if (!serverPort.getText().matches("[0-9]+"))
        {
            UIEffects.shakeNode(serverPort);
            return false;
        }

        try (Socket socket = new Socket(serverAddress.getText(), Integer.parseInt(serverPort.getText()))) {}
        catch (IllegalArgumentException e)
        {
            UIEffects.shakeNode(serverPort);
            return false;
        }
        catch (Exception ignore)
        {
            // Maybe the server isn't up yet.
            // Whe handle this when we call reload and the CamelotServiceManager tries to connect
        }

        return true;
    }

    @SneakyThrows
    @Override
    public void onSave(CamelotServerConfigData configData)
    {
        CamelotServiceManager.getInstance().reload(configData);
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }
}
