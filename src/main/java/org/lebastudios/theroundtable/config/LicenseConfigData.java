package org.lebastudios.theroundtable.config;

import java.io.File;

public class LicenseConfigData extends ConfigData<LicenseConfigData>
{
    public String email = "";
    public String password = "";
    public String license = "";

    @Override
    public File getFile()
    {
        return new File(AppConfiguration.getGlobalDir() + "/account.json");
    }
}
