package org.lebastudios.theroundtable.env;

public class Platform
{
    public static PlatformOS getPlatformOS() 
    {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) return PlatformOS.WINDOWS;
        if (os.contains("nix") || os.contains("nux")) return PlatformOS.LINUX;
        if (os.contains("mac")) return PlatformOS.MAC;
        if (os.contains("droid")) return PlatformOS.ANDROID;
        
        return PlatformOS.UNKNOWN;
    }
    
    public static PlatformArch getPlatformArch()
    {
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (arch.contains("64")) return PlatformArch.X64;
        if (arch.contains("32")) return PlatformArch.X86;
        if (arch.contains("arm")) return PlatformArch.ARM;
        if (arch.contains("aarch64")) return PlatformArch.ARM64;
        
        return PlatformArch.UNKNOWN;
    }
}
