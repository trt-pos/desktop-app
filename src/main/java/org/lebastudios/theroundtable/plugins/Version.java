package org.lebastudios.theroundtable.plugins;

import lombok.Getter;

@Getter
public class Version implements Comparable<Version>
{
    private final int major;
    private final int minor;
    private final int patch;

    public Version(int major, int minor, int patch)
    {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(String version)
    {
        if (!version.matches("\\d+\\.\\d+\\.\\d+"))
        {
            throw new IllegalArgumentException(
                    "Invalid version format. Must be in the format 'major.minor.patch'. Provided: " + version
            );
        }
        
        String[] versionNum = version.split("\\.");
        
        major = Integer.parseInt(versionNum[0]);
        minor = Integer.parseInt(versionNum[1]);
        patch = Integer.parseInt(versionNum[2]);
    }

    public boolean isGreaterThan(Version o)
    {
        return compareTo(o) > 0;
    }
    
    public boolean isLessThan(Version o)
    {
        return compareTo(o) < 0;
    }
    
    public boolean isEqualTo(Version o)
    {
        return compareTo(o) == 0;
    }
    
    public boolean hasSameMajor(Version o) { return major == o.major; }
    
    @Override
    public int compareTo(Version o)
    {
        if (major != o.major) return major - o.major;
        if (minor != o.minor) return minor - o.minor;
        if (patch != o.patch) return patch - o.patch;
        return 0;
    }

    @Override
    public String toString()
    {
        return major + "." + minor + "." + patch;
    }
}
