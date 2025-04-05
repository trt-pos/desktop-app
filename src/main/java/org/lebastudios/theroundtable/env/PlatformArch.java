package org.lebastudios.theroundtable.env;

public enum PlatformArch
{
    ARM, X86, X64, UNKNOWN, ARM64;

    @Override
    public String toString()
    {
        return switch (this) {
            case ARM -> "arm";
            case X86 -> "x86";
            case X64 -> "x64";
            case ARM64 -> "aarch64";
            default -> "unknown";
        };
    }
}
