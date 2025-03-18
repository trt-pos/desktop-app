package org.lebastudios.theroundtable.printers;

import com.github.anastaciocintra.escpos.EscPos;
import org.lebastudios.theroundtable.files.JsonFile;
import org.lebastudios.theroundtable.config.PrintersConfigData;

import java.io.IOException;

public class OpenCashDrawer implements IPrinter
{
    @Override
    public EscPos print(EscPos escpos) throws IOException
    {
        byte[] command = new PrintersConfigData().load().getOpenCashDrawerCommand();
        
        escpos.write(command, 0, command.length);
        return escpos;
    }
}
