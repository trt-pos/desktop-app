package org.lebastudios.theroundtable.camelot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lebastudios.theroundtable.camelot.trtcp.*;
import org.lebastudios.theroundtable.logs.Logs;
import org.lebastudios.theroundtable.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Arrays;
import java.util.function.Consumer;

public class CamelotClient implements AutoCloseable
{
    @Getter private final String name;
    private final String host;
    private final int port;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final Container<Response> lastResponseContainer = new Container<>();
    @Setter private Consumer<Request> callbacksHandler = r ->
    {
        Logs.getInstance().log(Logs.LogType.WARNING,
                "No callbacks handler for Camelot client " + r.getHead().getCaller());
    };
    @Setter private Consumer<Exception> onErrorHandler = e ->
    {
        Logs.getInstance().log(
                "Default error handler for Camelot client",
                e
        );
    };

    public CamelotClient(String name, String host, int port)
    {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public Task<Void> validateTask()
    {
        return new Task<>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Validating Camelot connection");

                int retries = 0;
                boolean success = false;
                int milisToWaitPerTry = 5000;
                while (retries < 3 && !success)
                {
                    int secsToWait = (milisToWaitPerTry * retries) / 1000;

                    while (secsToWait-- > 0)
                    {
                        updateMessage("Failed to connect to Camelot, retrying in " + secsToWait + " seconds");
                        Thread.sleep(1000);
                    }
                    
                    try
                    {
                        socket = new Socket(host, port);
                        success = true;
                    }
                    catch (ConnectException exception)
                    {
                        retries++;
                    }
                }

                if (!success)
                {
                    throw new ConnectException("Failed to connect to Camelot");
                }

                return null;
            }
        };
    }

    public Task<Void> connectTask()
    {
        return new Task<>()
        {
            @Override
            protected Void call() throws Exception
            {
                updateTitle("Connecting to Camelot");

                executeSubtask(validateTask());

                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                new Thread(readCoroutine, "Camelot client " + name + " Thread").start();

                CamelotClient.this.write(Request.creteConnectRequest(name));

                return null;
            }
        };
    }

    @Override
    public void close() throws Exception
    {
        if (socket == null) return;

        socket.close();
        socket = null;
    }

    private Response write(IntoBytes data)
    {
        try
        {
            byte[] bytes = data.intoBytes();

            synchronized (this)
            {
                out.write(bytes);
                out.flush();
            }

            synchronized (lastResponseContainer)
            {
                if (lastResponseContainer.isEmpty())
                {
                    lastResponseContainer.wait();
                }

                Response response = lastResponseContainer.getValue();
                lastResponseContainer.clear();
                return response;
            }
        }
        catch (Exception exception)
        {
            onErrorHandler.accept(exception);
            return null;
        }
    }

    public void createAndListenEvent(String event)
    {
        String[] parts = event.split(":");

        if (parts.length != 2)
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "Invalid event name: " + event
            );
            return;
        }

        // Create the event in the server
        Request request = new Request(
                new Head(Version.actualProtocolVersion(), name),
                new Action(ActionType.CREATE, parts[0], parts[1]),
                new byte[0]
        );

        Response response = write(request);

        if (response.getStatusCode() != StatusCode.OK)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Failed to create event " + event + ": " + response.getStatusCode()
            );
        }

        // Add this client as a listener for the event
        request = new Request(
                new Head(Version.actualProtocolVersion(), name),
                new Action(ActionType.LISTEN, parts[0], parts[1]),
                new byte[0]
        );

        response = write(request);

        if (response.getStatusCode() != StatusCode.OK)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Failed to listen to event " + event + ": " + response.getStatusCode()
            );
        }
    }

    public void invokeEvent(String event, IntoBytes data)
    {
        String[] parts = event.split(":");

        if (parts.length != 2)
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "Invalid event name: " + event
            );
            return;
        }

        byte[] bytes = data.intoBytes();

        Request request = new Request(
                new Head(Version.actualProtocolVersion(), name),
                new Action(ActionType.INVOKE, parts[0], parts[1]),
                bytes
        );

        Response response = write(request);

        if (response.getStatusCode() != StatusCode.OK)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Failed to invoke event " + event + ": " + response.getStatusCode()
            );
        }
    }

    public void removeListener(String event)
    {
        String[] parts = event.split(":");

        if (parts.length != 2)
        {
            Logs.getInstance().log(
                    Logs.LogType.ERROR,
                    "Invalid event name: " + event
            );
            return;
        }

        Request request = new Request(
                new Head(Version.actualProtocolVersion(), name),
                new Action(ActionType.LEAVE, parts[0], parts[1]),
                new byte[0]
        );

        Response response = write(request);

        if (response.getStatusCode() != StatusCode.OK)
        {
            Logs.getInstance().log(
                    Logs.LogType.WARNING,
                    "Failed to remove listener from event " + event + ": " + response.getStatusCode()
            );
        }
    }

    private final Runnable readCoroutine = () ->
    {
        Socket tmpSocket = socket;

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (tmpSocket)
        {
            while (!socket.isClosed() && socket.isConnected() && !socket.isInputShutdown())
            {
                buffer.reset();

                byte msgType = in.readByte(); // msgType byte
                buffer.write(msgType);

                int length = in.readInt(); // length int
                buffer.write((byte) (length >> 24));
                buffer.write((byte) (length >> 16));
                buffer.write((byte) (length >> 8));
                buffer.write((byte) length);

                buffer.write(in.readNBytes(length)); // head, middle and body bytes

                byte[] packet = buffer.toByteArray();

                switch (msgType)
                {
                    case 0 ->
                    {
                        Request request;
                        try
                        {
                            request = new Request().fromBytes(packet);

                            Logs.getInstance().log(
                                    Logs.LogType.INFO,
                                    "Received request from Camelot (" + packet.length + " bytes) with of type: " +
                                            request.getAction().getType()
                            );
                        }
                        catch (ParseException exception)
                        {
                            Logs.getInstance().log(
                                    "Failed to parse packet from Camelot (" + Arrays.toString(packet) + ")",
                                    exception
                            );
                            continue;
                        }

                        if (request.getAction().getType() != ActionType.CALLBACK)
                        {
                            Logs.getInstance().log(
                                    Logs.LogType.WARNING,
                                    "Received a request with an invalid action type: " +
                                            request.getAction().getType()
                            );
                            continue;
                        }

                        callbacksHandler.accept(request);
                    }
                    case 1 ->
                    {
                        synchronized (lastResponseContainer)
                        {
                            try
                            {
                                Response response = new Response().fromBytes(packet);

                                Logs.getInstance().log(
                                        Logs.LogType.INFO,
                                        "Received response from Camelot (" + packet.length + " bytes) with status: " +
                                                response.getStatusCode()
                                );

                                lastResponseContainer.setValue(response);
                            }
                            catch (ParseException exception)
                            {
                                Logs.getInstance().log(
                                        "Failed to parse packet from Camelot (" + Arrays.toString(packet) + ")",
                                        exception
                                );
                            }

                            // Notify the waiting thread even if the response could be parsed or not to avoid a deadlock
                            lastResponseContainer.notify();
                        }
                    }
                    default -> Logs.getInstance().log(
                            Logs.LogType.WARNING,
                            "Received a packet with an invalid header: " + packet[0]
                    );
                }
            }
        }
        catch (Exception e)
        {
            onErrorHandler.accept(e);
        }

        synchronized (lastResponseContainer)
        {
            // Notify the waiting thread if an error ocurred while reading from the stream to avoid deadwaits
            lastResponseContainer.notify();
        }
    };

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Container<T>
    {
        private T value;

        public boolean isEmpty()
        {
            return value == null;
        }

        public void clear()
        {
            value = null;
        }
    }

}
