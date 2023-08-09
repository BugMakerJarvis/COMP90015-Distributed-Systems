package edu.unimelb.jarvis;

import edu.unimelb.jarvis.dict.DictionaryService;
import edu.unimelb.jarvis.server.TCPInteractiveServer;

/**
 * Dictionary Server
 */
public class DictionaryServerApp {
    // java –jar DictionaryServer.jar <port> <dictionary-file>
    public static void main(String[] args) {
        String dictionaryFile = "/dictionary.json";
        int port = 4444;
        try {
            if (args.length == 2) {
                port = Integer.parseInt(args[0]);
                dictionaryFile = args[1];
            } else if (args.length != 0) {
                throw new IllegalArgumentException("Invalid command line arguments");
            }
        } catch (Exception e) {
            System.out.println("Error parsing command line arguments: " + e.getMessage());
            System.out.println("A sample command to start the server is: java –jar DictionaryServer.jar <port> <dictionary-file>");
            System.out.println("Starting server using default port and dictionary file...");
        }
        // load dictionary to memory
        if (!DictionaryService.loadDictionary(dictionaryFile)) return;
        // start server
        TCPInteractiveServer.start(port);
    }
}
