package org.lebastudios.theroundtable.env;

public enum PlatformOS
{
    WINDOWS,
    LINUX,
    MAC,
    ANDROID,
    UNKNOWN;

    public String getBinaryExtension()
    {
        return switch (this) {
            case WINDOWS -> ".exe";
            default -> "";
        };
    }
    
    @Override
    public String toString()
    {
        return switch (this) {
            case WINDOWS -> "win";
            case LINUX -> "linux";
            case MAC -> "mac";
            case ANDROID -> "android";
            default -> "unknown";
        };
    }
}
