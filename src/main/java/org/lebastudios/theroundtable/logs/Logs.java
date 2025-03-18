package org.lebastudios.theroundtable.logs;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;

public class Logs
{
    private static Logs instance;

    public enum LogType
    {
        INFO, WARNING, ERROR, EXCEPTION;
        
        public boolean isError()
        {
            return this == ERROR || this == EXCEPTION;
        }
    }

    public static Logs getInstance()
    {
        if (instance == null) instance = new Logs();

        return instance;
    }

    private Logs() {}

    public void log(LogType type, String message)
    {
        PrintStream out = type.isError() ? System.err : System.out;
        
        final String date = getDateString();
        final String thread = Thread.currentThread().getName();

        out.printf("%s [%s] [%s] %s%n", date, thread, type, message);
    }

    public void log(String message, Throwable e)
    {
        StackTraceElement element = e.getStackTrace()[0];
        log(LogType.EXCEPTION, message + " (" + e.getMessage() + ") in "
                + element.getClassName() + "." + element.getMethodName() + ": " + element.getLineNumber());
    }

    private static String getDateString()
    {
        return java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }


}
