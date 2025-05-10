package org.lebastudios.theroundtable.communications;

import org.hibernate.Session;
import org.lebastudios.theroundtable.TheRoundTableApplication;
import org.lebastudios.theroundtable.database.Database;
import org.lebastudios.theroundtable.dialogs.ExceptionDialogController;
import org.lebastudios.theroundtable.env.Directories;
import org.lebastudios.theroundtable.events.AppLifeCicleEvents;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FileTransferServiceManager
{
    private static FileTransferServiceManager instance;
    private static final int port = 1621;
    private ServerSocket serverSocket;

    public static FileTransferServiceManager getInstance()
    {
        if (instance == null) instance = new FileTransferServiceManager();

        return instance;
    }

    private FileTransferServiceManager() 
    {
        AppLifeCicleEvents.OnAppClose.addListener(_ -> stop());
    }

    public Task<Void> initTask()
    {
        return new Task<>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Starting File Transfer Service");

                updateMessage("Checking port");
                if (!PortDistributor.getInstance().isPortAvailable(port))
                {
                    throw new IOException("Port " + port + " is not available");
                }

                startServer();

                return null;
            }
        };
    }

    public InputStream getNetFileInputStream(String filePath)
    {
        return Database.getInstance().connectQuery(session ->
        {
            return this.getNetFileInputStream(filePath, session);
        });
    }

    public InputStream getNetFileInputStream(String filePath, Session session)
    {
        String masterIp = session.createQuery(
                "select i.ip from AppInstallation i where i.is_master = true",
                String.class
        ).getSingleResult();

        try
        {
            Socket socket = new Socket(masterIp, port);
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
            writer.writeUTF(filePath);

            return new BufferedInputStream(socket.getInputStream())
            {
                @Override
                public void close() throws IOException
                {
                    super.close();
                    socket.close();
                }
            };
        }
        catch (IOException e)
        {
            Logs.getInstance().log("Error reading file from file transfer service: ", e);
            return null;
        }
    }

    private void startServer()
    {
        new Thread(() ->
        {
            try (ServerSocket serverSocket = new ServerSocket(FileTransferServiceManager.port))
            {
                FileTransferServiceManager.this.serverSocket = serverSocket;
                
                while (true)
                {
                    try
                    {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(new ClientHandler(clientSocket)).start();
                    }
                    catch (SocketException e)
                    {
                        if (serverSocket.isClosed()) break;
                        Logs.getInstance().log(
                                "Error accepting client connection on file transfer service: ",
                                e
                        );
                    }
                    catch (IOException e)
                    {
                        Logs.getInstance().log(
                                "Error accepting client connection on file transfer service: ",
                                e
                        );
                    }
                }
            }
            catch (IOException e)
            {
                Logs.getInstance().log(
                        "Error starting file transfer service: ",
                        e
                );
                TheRoundTableApplication.executeInFxThreadAndWait(() ->
                        new ExceptionDialogController(e).instantiate(true)
                );
                TheRoundTableApplication.exitAplication(1);
            }
        }, "File Transfer Service Thread").start();
    }

    public void stop()
    {
        if (serverSocket == null) return;
        
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            Logs.getInstance().log(
                    "Error stopping file transfer service: ",
                    e
            );
        }
    }
    
    private record ClientHandler(Socket socket) implements Runnable
    {
        public void run()
        {
            try (DataInputStream reader = new DataInputStream(socket.getInputStream());
                 DataOutputStream writer = new DataOutputStream(socket.getOutputStream()))
            {
                String reqFilePath = reader.readUTF();
                File file = new File(reqFilePath);

                if (!file.exists())
                {
                    writer.write(new byte[]{});
                    return;
                }

                if (!file.getAbsolutePath().startsWith(Directories.homeDir()))
                {
                    writer.write(new byte[]{});
                    return;
                }

                try (FileInputStream fileInputStream = new FileInputStream(file))
                {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1)
                    {
                        writer.write(buffer, 0, bytesRead);
                    }
                }
            }
            catch (IOException e)
            {
                Logs.getInstance().log(
                        "Error handling client connection on file transfer service: ",
                        e
                );
            }
        }
    }
}
