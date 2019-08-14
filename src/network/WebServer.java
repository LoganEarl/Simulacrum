package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Responsible for creating and maintaining a single server thread for sending messages and any number of client threads used
 * to communicate with connected clients
 *
 * @author Logan Earl
 */
public class WebServer {
    private Thread serverThread;
    private boolean serverRunning = false;

    private OnMessageReceivedListener clientListener;

    /**Used to denote the end of a message*/
    public static final String MESSAGE_DIVIDER = "<!EOM!>";

    private List<ClientConnection> connectedClients = new ArrayList<>();

    /**
     * Creates the server thread, but does not start it yet. To do that, use the startServer() method
     * @param port the port to start the server on
     */
    public WebServer(final int port) {
        serverThread = new Thread(() -> {
            try {
                serverRunning = true;
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    Socket newSocket = serverSocket.accept();
                    ClientConnection clientConnection = new ClientConnection(newSocket);
                    connectedClients.add(clientConnection);
                    clientConnection.start();
                }

            } catch (Exception e) {
                serverRunning = false;
                System.out.println("Failed to start server thread\n" +
                        Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void setMessageReceivedListener(OnMessageReceivedListener clientListener){
        this.clientListener = clientListener;
    }

    /**
     * Starts the server thread, allowing clients to connect.
     */
    public void startServer() {
        if (serverThread != null && !serverRunning)
            serverThread.start();
    }

    /**
     * sends the given message to the given clients. If no client is given, all are notified
     * @param message the message to send to the given clients
     * @param toNotify the identifiers of all clients to notify
     */
    public void notifyClients(ServerMessage message, String... toNotify) {
        for (ClientConnection conn : connectedClients)
            if (toNotify.length == 0 || Arrays.asList(toNotify).contains(conn.identifier))
                conn.sendMessage(message);
    }

    /**
     * disconnects the given client connections. If none are specified, disconnects all connections
     * @param toDisconnect the identifiers of all clients to disconnect
     */
    public void disconnectClients(String... toDisconnect){
        List<ClientConnection> toRemove = new ArrayList<>(1);
        for (ClientConnection conn : connectedClients)
            if (toDisconnect.length == 0 || Arrays.asList(toDisconnect).contains(conn.identifier)) {
                conn.kill();
                toRemove.add(conn);
            }

        for(ClientConnection conn: toRemove)
            connectedClients.remove(conn);
    }

    private class ClientConnection extends Thread{
        private Socket clientSocket;
        private String identifier;

        private BufferedOutputStream out;
        private BufferedInputStream in;

        private Vector<ServerMessage> messageQueue = new Vector<>();

        private boolean alive = true;

        ClientConnection(Socket clientSocket) {
            this.clientSocket = clientSocket;
            identifier = clientSocket.getInetAddress().toString();

            try{
                out = new BufferedOutputStream(clientSocket.getOutputStream());
                in = new BufferedInputStream(clientSocket.getInputStream());
                clientSocket.setKeepAlive(true);
            }catch (Exception ignored){}
        }

        private void sendMessage(ServerMessage toSend) {
            if(toSend != null)
                messageQueue.add(toSend);
        }

        @Override
        public void run(){
            while(alive){
                try {
                    while (!messageQueue.isEmpty()) {
                        ServerMessage message = messageQueue.remove(0);
                        out.write(message.getHeader().getBytes());
                        out.write("\n".getBytes());
                        out.write(message.getBytes());
                        out.flush();
                    }
                    if(in.available() > 0){
                        byte[] allRawMessages = new byte[in.available()];
                        in.read(allRawMessages);
                        String allMessages = new String(allRawMessages);
                        String[] messages = allMessages.split(MESSAGE_DIVIDER);

                        if(clientListener != null)
                            for(String message: messages)
                                if(message != null && !message.isEmpty())
                                    clientListener.onClientMessage(identifier,message);
                    }
                }catch (SocketException e) {
                    alive = false;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            try {
                in.close();
                out.close();
                clientSocket.close();
            }catch (Exception ignored){}
        }

        private void kill(){
            alive = false;
        }
    }

    /**
     * Interface for a message being sent from the server to clients. All messages must be able to be converted int a
     * standard byte[] for transfer to the client. All messages must terminate with the following sequence of characters
     * {@value MESSAGE_DIVIDER}
     */
    public interface ServerMessage {
        byte[] getBytes();
        String getHeader();
    }

    /**
     * Listener used to alert the program at large of a newly received message from a client.
     */
    public interface OnMessageReceivedListener{
        /**
         * method called when a new message is received
         * @param client the client's internet address
         * @param rawMessage the message the client sent
         */
        void onClientMessage(String client, String rawMessage);
    }
}
