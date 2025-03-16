package org.lebastudios.theroundtable.logs;

import java.time.format.DateTimeFormatter;

public class Logs
{
    private static Logs instance;

    public enum LogType
    {
        INFO, WARNING, ERROR, EXCEPTION;
    }

    public static Logs getInstance()
    {
        if (instance == null) instance = new Logs();

        return instance;
    }

    private Logs() {}

    public void log(LogType type, String message)
    {
        final String date = getDateString();
        final String thread = Thread.currentThread().getName();

        System.out.printf("%s [%s] [%s] %s%n", date, thread, type, message);
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
