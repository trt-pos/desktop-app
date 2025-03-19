package org.lebastudios.theroundtable.printers;

import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.Getter;
import lombok.Setter;
import org.lebastudios.theroundtable.config.PrintersConfigData;

import javax.print.PrintService;
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

    public PrintService getDefaultPrintService() throws IllegalArgumentException
    {
        var defaultPrinterName = new PrintersConfigData().load().defaultPrinter;

        final var key = "default:" + defaultPrinterName;
        if (printServices.containsKey(key)) 
        {
            return printServices.get(key);
        }

        final var defaultPrintService = PrinterOutputStream.getPrintServiceByName(defaultPrinterName);
        printServices.put(key, defaultPrintService);
        return defaultPrintService;
    }

    public String[] getAvailablePrinters()
    {
        return PrinterOutputStream.getListPrintServicesNames();
    }
}
