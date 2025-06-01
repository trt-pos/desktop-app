package org.lebastudios.theroundtable.rustdesk;

import org.lebastudios.theroundtable.env.Platform;
import org.lebastudios.theroundtable.env.PlatformOS;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class RustDeskInstallTask extends Task<Void>
{
    @Override
    protected Void call() throws Exception
    {
        updateTitle("Installing RustDesk");
        updateMessage("Preparing download...");

        PlatformOS os = Platform.getPlatformOS();

        String scriptExtension = switch (os)
        {
            case WINDOWS -> "win.cmd";
            case LINUX -> "linux";
            case MAC -> "macos";
            default -> throw new IllegalStateException("Not valid platform");
        };

        String installScript = "rustdesk-installer-" + scriptExtension;

        Path installationScriptPath = Path.of(System.getProperty("java.io.tmpdir"), installScript);

        try (InputStream is = this.getClass().getResourceAsStream(installScript))
        {
            assert is != null;
            Files.copy(is, installationScriptPath, StandardCopyOption.REPLACE_EXISTING);
        }

        installationScriptPath.toFile().setExecutable(true, false);
        
        ProcessBuilder pb = new ProcessBuilder("pkexec", "/bin/bash",
                installationScriptPath.toAbsolutePath().toString()
        ).directory(new File(System.getProperty("java.io.tmpdir")))
                .inheritIO();

        Process process = pb.start();

        updateMessage("Downloading RustDesk...");
        float maximunStep = 1f / 25f;
        float progress = 0f;
        float maximumProgress = 1f;

        while (process.isAlive())
        {
            Thread.sleep(1000);
            float step = (maximumProgress - progress) / 2f;
            if (step > maximunStep)
            {
                step = maximunStep;
            }

            progress += step;
            updateProgress(progress, maximumProgress);
        }

        return null;
    }
}
