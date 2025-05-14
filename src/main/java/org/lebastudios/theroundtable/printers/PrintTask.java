package org.lebastudios.theroundtable.printers;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.output.PrinterOutputStream;
import lombok.Setter;
import org.lebastudios.theroundtable.dialogs.ExceptionDialogController;
import org.lebastudios.theroundtable.tasks.Task;

import javax.print.PrintService;
import java.io.IOException;

public abstract class PrintTask extends Task<Void>
{
    private final EscPos escpos;
    @Setter private int feed = 5;
    @Setter private EscPos.CutMode cutMode = EscPos.CutMode.PART;
    @Setter private boolean cut = true;

    public PrintTask(EscPos escPos)
    {
        this.escpos = escPos;
    }
    
    public PrintTask(PrintService printService)
    {
        try
        {
            this.escpos = new EscPos(new PrinterOutputStream(printService));
        }
        catch (IOException e)
        {
            new ExceptionDialogController(e).instantiate(true);
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected Void call() throws Exception
    {
        updateTitle("Printing...");
        updateMessage("Executing printting job");
        
        try (EscPos escpos = print(this.escpos))
        {
            if (cut) escpos.feed(feed).cut(cutMode);
        }
        return null;
    }

    protected abstract EscPos print(EscPos escpos) throws IOException;
}
