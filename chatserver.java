import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    // A thread-safe set to store all connected client sockets
    private static Set<Socket> clientSockets = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        int port = 12345; // Port number for the server

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port + ". Waiting for clients...");

            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket); // Track the client socket
                System.out.println("New client connected: " + clientSocket);

                // Start a new thread to handle communication with this client
                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    // Thread class to manage a single client's messages
    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                // Input stream to receive messages from client
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Output stream to send messages to client
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Client handler error: " + e.getMessage());
            }
        }

        public void run() {
            String message;

            try {
                // Continuously listen for messages from this client
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);

                    // Send the message to all other connected clients
                    for (Socket s : clientSockets) {
                        if (s != socket) { // Don't send to the sender
                            PrintWriter clientOut = new PrintWriter(s.getOutputStream(), true);
                            clientOut.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection lost: " + e.getMessage());
            } finally {
                // Clean up when client disconnects
                try {
                    clientSockets.remove(socket);
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}
