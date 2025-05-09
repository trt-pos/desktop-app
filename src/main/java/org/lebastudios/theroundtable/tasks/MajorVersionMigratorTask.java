package org.lebastudios.theroundtable.tasks;

import org.lebastudios.theroundtable.logs.Logs;

public class MajorVersionMigratorTask extends Task<Void>
{
    private final int oldMajor;
    private final int newMajor;

    public MajorVersionMigratorTask(int oldMajor, int newMajor)
    {
        this.oldMajor = oldMajor;
        this.newMajor = newMajor;
    }

    @Override
    protected Void call() throws Exception
    {
        this.updateTitle("Migrating to a new version");
        
        Logs.getInstance().log(
                Logs.LogType.INFO,
                "Migrating database from version " + oldMajor + " to " + newMajor
        );

        updateProgress(0, 1);
        for (int i = oldMajor; i <= newMajor; i++)
        {
            updateMessage("Migrating to major version " + i);
            this.getClass().getMethod("migrateToMajor" + i).invoke(this);
            updateProgress(i - newMajor, newMajor);
        }

        return null;
    }

    private void migrateToMajor1() {}

    private void migrateToMajor2() {}

    private void migrateToMajor3()
    {

    }
}
