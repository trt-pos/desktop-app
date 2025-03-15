package org.lebastudios.theroundtable.env;

import lombok.Getter;

import java.io.File;

public class Platform
{
    @Getter private static final PlatformType platformType = detectPlatformType();

    public enum PlatformType
    {
        WINDOWS,
        LINUX,
        MAC,
        ANDROID,
        UNKNOWN
    }
    
    private static PlatformType detectPlatformType() 
    {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) return PlatformType.WINDOWS;
        if (os.contains("nix") || os.contains("nux")) return PlatformType.LINUX;
        if (os.contains("mac")) return PlatformType.MAC;
        if (os.contains("droid")) return PlatformType.ANDROID;
        
        return PlatformType.UNKNOWN;
    }
    
    public static File getTempDir()
    {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
