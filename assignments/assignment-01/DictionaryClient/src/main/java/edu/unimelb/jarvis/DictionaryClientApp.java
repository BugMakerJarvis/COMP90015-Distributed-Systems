package edu.unimelb.jarvis;

import edu.unimelb.jarvis.client.TCPInteractiveClient;
import edu.unimelb.jarvis.javafx.JavaFXApp;
import lombok.extern.slf4j.Slf4j;

/**
 * Dictionary Client
 */
@Slf4j
public class DictionaryClientApp {
    // java –jar DictionaryClient.jar <server-address> <server-port>
    public static void main(String[] args) {
        // init TCP connection
        initTCPConnection(args);
        // start JavaFX
        JavaFXApp.main(args);
    }

    private static void initTCPConnection(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 4444;
        try {
            if (args.length == 2) {
                serverAddress = args[0];
                serverPort = Integer.parseInt(args[1]);
            } else if (args.length != 0) {
                throw new IllegalArgumentException("Invalid command line arguments");
            }
        } catch (Exception e) {
            log.error("Error parsing command line arguments: " + e.getMessage());
            log.info("A sample command to start the client is: java –jar DictionaryClient.jar <server-address> <server-port>");
            log.info("Connecting to server using default address and port...");
        }
        TCPInteractiveClient client = TCPInteractiveClient.getInstance(serverAddress, serverPort);
        Thread t = new Thread(client);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            log.error("Error waiting for TCPInteractiveClient thread to complete: " + e.getMessage());
        }
    }
}
