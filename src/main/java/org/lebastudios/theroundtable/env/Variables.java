package org.lebastudios.theroundtable.env;

public class Variables
{
    private static EnvironmentType envType = detectEnvironmentType();
    private static Boolean showExceptionsBacktrace = null;
    private static Boolean isVerbose = null;

    public enum EnvironmentType
    {
        DEV,
        TEST,
        PROD
    }

    public static boolean isDev()
    {
        return getEnvironmentType() == EnvironmentType.DEV;
    }

    public static boolean isProd()
    {
        return getEnvironmentType() == EnvironmentType.PROD;
    }

    public static boolean isTest()
    {
        return getEnvironmentType() == EnvironmentType.TEST;
    }

    public static EnvironmentType getEnvironmentType()
    {
        if (envType == null)
        {
            envType = detectEnvironmentType();
        }

        return envType;
    }

    public static boolean showExceptionsBacktrace()
    {
        if (showExceptionsBacktrace == null)
        {
            String value = System.getenv("TRT_BACKTRACE");

            if (value == null)
            {
                showExceptionsBacktrace = false;
            }
            else
            {
                showExceptionsBacktrace = value.equals("1");
            }
        }

        return showExceptionsBacktrace;
    }

    public static boolean isVerbose()
    {
        if (isVerbose == null)
        {
            String value = System.getenv("TRT_VERBOSE");

            if (value == null)
            {
                isVerbose = false;
            }
            else
            {
                isVerbose = value.equals("1");
            }
        }

        return isVerbose;
    }

    public static String getTestServerUrl()
    {
        return System.getenv("TRT_TEST_SERVER_URL");
    }

    public static String getTestCentralPluginRepoUrl()
    {
        return System.getenv("TRT_TEST_CENTRAL_PLUGIN_REPO_URL");
    }

    private static EnvironmentType detectEnvironmentType()
    {
        String enviroment = System.getenv("TRT_ENV");

        if (enviroment == null) return EnvironmentType.PROD;

        return switch (enviroment.toLowerCase())
        {
            case "dev" -> EnvironmentType.DEV;
            case "test" -> EnvironmentType.TEST;
            default -> EnvironmentType.PROD;
        };
    }
}
