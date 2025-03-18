package org.lebastudios.theroundtable.config;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.lebastudios.theroundtable.Launcher;
import org.lebastudios.theroundtable.apparience.UIEffects;
import org.lebastudios.theroundtable.printers.PrinterManager;

import java.util.Arrays;

public class PrintersConfigPaneController extends ConfigPaneController<PrintersConfigData>
{
    @FXML private CheckBox useOpenCashDrawerDefaultCommand;
    @FXML private TextField openCashDrawerCommand;
    @FXML private ChoiceBox<String> defaultPrinter;

    public PrintersConfigPaneController()
    {
        super(new PrintersConfigData());
    }

    @Override
    public void updateConfigData(PrintersConfigData configData)
    {
        configData.defaultPrinter = defaultPrinter.getValue();
        configData.setUseOpenCashDrawerDefaultCommand(useOpenCashDrawerDefaultCommand.isSelected());
        configData.setOpenCashDrawerCommand(parseCommand(openCashDrawerCommand.getText().trim()));
    }

    @Override
    public void updateUI(PrintersConfigData configData)
    {
        defaultPrinter.getItems().clear();

        defaultPrinter.getItems().addAll(
                PrinterManager.getInstance().getAvailablePrinters()
        );

        if (!configData.defaultPrinter.isEmpty())
        {
            defaultPrinter.setValue(configData.defaultPrinter);
        }
        else
        {
            defaultPrinter.setValue("");
        }

        openCashDrawerCommand.setText(parseCommand(configData.getOpenCashDrawerCommand()));

        useOpenCashDrawerDefaultCommand.selectedProperty().addListener((_, _, newValue) ->
        {
            if (newValue == null) return;

            openCashDrawerCommand.setDisable(newValue);

            if (newValue)
            {
                openCashDrawerCommand.setText(parseCommand(PrintersConfigData.OPEN_CASH_DRAWER_DEFAULT_COMMAND));
            }
        });

        useOpenCashDrawerDefaultCommand.setSelected(configData.isUseOpenCashDrawerDefaultCommand());
    }

    @Override
    public boolean validate()
    {
        if (parseCommand(openCashDrawerCommand.getText().trim()) == null) 
        {
            UIEffects.shakeNode(openCashDrawerCommand);
            return false;
        }
        
        return true;
    }

    private byte[] parseCommand(String command)
    {
        try
        {
            var commandParsed = Arrays.stream(command.trim().split(" "))
                    .map(String::trim)
                    .map(Byte::parseByte)
                    .map(b -> (byte) b.intValue())
                    .toArray(Byte[]::new);

            byte[] commandBytes = new byte[commandParsed.length];
            for (int i = 0; i < commandBytes.length; i++)
            {
                commandBytes[i] = commandParsed[i];
            }

            return commandBytes;
        }
        catch (Exception exception)
        {
            return null;
        }
    }

    private String parseCommand(byte[] command)
    {
        StringBuilder sb = new StringBuilder();
        for (var element : command)
        {
            sb.append(element).append(" ");
        }

        return sb.toString().trim();
    }

    @FXML
    private void testDefaultPrinter()
    {
        try (EscPos escPos = new EscPos(
                new PrinterOutputStream(PrinterOutputStream.getPrintServiceByName(defaultPrinter.getValue()))))
        {
            escPos.writeLF("Test")
                    .writeLF("Dolar: $100")
                    .writeLF("Euro: €100")
                    .writeLF("Special characters: áéñ#*=¿¡")
                    .feed(5)
                    .cut(EscPos.CutMode.PART);
        } catch (Exception _) { UIEffects.shakeNode(defaultPrinter); }
    }

    @Override
    public Class<?> getBundleClass()
    {
        return Launcher.class;
    }
}
