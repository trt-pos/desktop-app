package org.lebastudios.theroundtable.printers;

import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.Getter;
import lombok.Setter;
import org.lebastudios.theroundtable.config.PrintersConfigData;
import org.lebastudios.theroundtable.config.PrintersConfigPaneController;
import org.lebastudios.theroundtable.config.RequestConfigStageController;

import javax.print.PrintService;
import java.io.IOException;
import java.util.HashMap;

@Setter
@Getter
public class PrinterManager
{
    private HashMap<String, PrintService> printServices = new HashMap<>();
    
    private static PrinterManager instance;

    private PrinterManager() {}

    public static PrinterManager getInstance()
    {
        if (instance == null) instance = new PrinterManager();

        return instance;
    }

    public PrintService getDefaultPrintService()
    {
        try
        {
            return getPrintServiceByName(new PrintersConfigData().load().defaultPrinter);
        }
        catch (IOException e)
        {
            new RequestConfigStageController(new PrintersConfigPaneController())
                    .setTitle("Error trying to obtain the default printer")
                    .instantiate(true);
            return getDefaultPrintService();
        }
    }

    public PrintService getPrintServiceByName(String printerName) throws IOException
    {
        if (printServices.containsKey(printerName))
        {
            return printServices.get(printerName);
        }

        PrintService defaultPrintService;

        try
        {
            defaultPrintService = PrinterOutputStream.getPrintServiceByName(printerName);
        }
        catch (IllegalArgumentException exception)
        {
            throw new IOException(exception);
        }

        printServices.put(printerName, defaultPrintService);
        return defaultPrintService;
    }
    
    public String[] getAvailablePrinters()
    {
        return PrinterOutputStream.getListPrintServicesNames();
    }
}
