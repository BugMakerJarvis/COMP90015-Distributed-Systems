package edu.unimelb.jarvis.server;

import edu.unimelb.jarvis.utils.MyThreadPool;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TCPInteractiveServer {
    private static final AtomicInteger clientCount = new AtomicInteger(0);
    private static int clientId = -1;
    private static final MyThreadPool threadPool = new MyThreadPool(10, 100);

    public static void start(int port) {
        ServerSocket listeningSocket = null;
        Socket clientSocket;

        try {
            // Create a server socket listening on port 4444
            listeningSocket = new ServerSocket(port);
            log.info("Server started, listening on port: " + port);
            while (true) {
                // Accept an incoming client connection request
                clientSocket = listeningSocket.accept(); // This method will block until a connection request is received
                clientCount.incrementAndGet();
                clientId++;
                log.info("Client connection number: " + clientCount + ", new client index: " + clientId);
//                new Thread(new ClientHandler(clientSocket, clientCount, clientId)).start();
                threadPool.execute(new ClientHandler(clientSocket, clientCount, clientId));
            }
        } catch (IOException e) {
            log.error("Error starting server: " + e.getMessage());
        } finally {
            if (listeningSocket != null) {
                try {
                    // close the server socket
                    listeningSocket.close();
                } catch (IOException e) {
                    log.error("Error closing server socket: " + e.getMessage());
                }
            }
            threadPool.shutdown();
        }
    }
}
