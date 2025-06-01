package org.lebastudios.theroundtable.rustdesk;

import java.io.BufferedReader;
import java.io.IOException;

class RustDeckIntrospector
{
    public boolean isInstalled()
    {
        return getID() != null;
    }
    
    public String getID()
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder("rustdesk.exe", "--get-id");
            Process p = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(p.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = p.waitFor();
            if (exitCode == 0)
            {
                String id = output.toString().trim();
                return id.isEmpty() ? null : id;
            }
            else
            {
                return null;
            }
        }
        catch (IOException | InterruptedException e)
        {
            return null;
        }
    }
}
