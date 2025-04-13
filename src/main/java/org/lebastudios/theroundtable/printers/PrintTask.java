package org.lebastudios.theroundtable.printers;

import com.github.anastaciocintra.escpos.EscPos;
import lombok.Setter;
import org.lebastudios.theroundtable.tasks.Task;

public abstract class PrintTask extends Task<Void>
{
    private final EscPos escpos;
    @Setter private int feed = 5;
    @Setter private EscPos.CutMode cutMode = EscPos.CutMode.PART;

    public PrintTask(EscPos escPos)
    {
        this.escpos = escPos;
    }

    @Override
    protected Void call() throws Exception
    {
        updateTitle("Printing...");
        updateMessage("Executing printting job");
        
        try (EscPos escpos = print(this.escpos))
        {
            print(escpos);

            escpos.feed(feed).cut(cutMode);
        }
        return null;
    }

    protected abstract EscPos print(EscPos escpos);
}
