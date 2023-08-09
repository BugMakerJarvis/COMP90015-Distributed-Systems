package edu.unimelb.jarvis.server;

import edu.unimelb.jarvis.dict.DictionaryService;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader in;
    private final ObjectOutputStream out;
    private final AtomicInteger clientCount;
    private final int clientId;

    public ClientHandler(Socket clientSocket, AtomicInteger clientCount, int clientId) throws IOException {
        this.clientSocket = clientSocket;
        this.clientCount = clientCount;
        this.clientId = clientId;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            String clientMsg;
            while ((clientMsg = in.readLine()) != null) {
                log.info("Message from client " + clientId + ": " + clientMsg);
                if (clientMsg.equals("GET_DICT")) {
                    try {
                        out.writeObject(DictionaryService.getDictionary());
                        out.flush();
                    } catch (IOException e) {
                        log.error("Error sending dictionary to client " + clientId + ": " + e.getMessage());
                    }
                } else if (clientMsg.startsWith("ADD")) {
                    // ADD word meaning
                    String[] parts = clientMsg.split(" ", 3);
                    String word = parts[1];
                    String meaning = parts[2];
                    try {
                        String res = DictionaryService.addWord(word, meaning);
                        if (res.equals("REPEAT")) {
                            out.writeUTF("REPEAT - word already exists: " + word);
                            out.flush();
                        } else {
                            out.writeUTF("SUCCESS - added word: " + word);
                            out.flush();
                        }
                    } catch (Exception e) {
                        out.writeUTF("FAIL - " + e.getMessage());
                        out.flush();
                    }
                } else if (clientMsg.startsWith("DELETE")) {
                    // DELETE id
                    String id = clientMsg.split(" ")[1];
                    try {
                        String res = DictionaryService.deleteWord(Long.parseLong(id));
                        if (res.equals("NOT_FOUND")) {
                            out.writeUTF("NOT_FOUND - word with id " + id + " not found");
                            out.flush();
                        } else {
                            out.writeUTF("SUCCESS - deleted word with id: " + id);
                            out.flush();
                        }
                    } catch (Exception e) {
                        out.writeUTF("FAIL - " + e.getMessage());
                        out.flush();
                    }
                } else if (clientMsg.startsWith("UPDATE")) {
                    // UPDATE id word meaning
                    String[] parts = clientMsg.split(" ", 4);
                    String id = parts[1];
                    String word = parts[2];
                    String meaning = parts[3];
                    try {
                        String res = DictionaryService.updateWord(Long.parseLong(id), word, meaning);
                        if (res.equals("NOT_FOUND")) {
                            out.writeUTF("NOT_FOUND - word with id " + id + " not found");
                            out.flush();
                        } else {
                            out.writeUTF("SUCCESS - updated word with id: " + id);
                            out.flush();
                        }
                    } catch (Exception e) {
                        out.writeUTF("FAIL - " + e.getMessage());
                        out.flush();
                    }
                } else if (clientMsg.startsWith("SEARCH")) {
                    // SEARCH word
                    String word = clientMsg.split(" ", 2)[1];
                    try {
                        out.writeObject(DictionaryService.searchWord(word));
                        out.flush();
                    } catch (IOException e) {
                        log.error("Error sending search results to client " + clientId + ": " + e.getMessage());
                    }
                } else {
                    out.writeUTF("Unknown command: " + clientMsg);
                    out.flush();
                }
                log.info("Response sent");
            }
            log.info("Server closed the client connection! - received null");
        } catch (SocketException e) {
            log.error("SocketException: " + e.getMessage());
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                clientCount.decrementAndGet();
                log.info("Client connection number: " + clientCount + ", client " + clientId + " disconnected");
            } catch (IOException e) {
                log.error("IOException: " + e.getMessage());
            }
        }
    }
}