package edu.unimelb.jarvis.client;

import edu.unimelb.jarvis.dict.Word;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class TCPInteractiveClient implements Runnable {
    private static volatile TCPInteractiveClient instance;
    private final String serverAddress;
    private final int serverPort;
    private Socket socket;
    private ObjectInputStream in;
    private BufferedWriter out;

    private TCPInteractiveClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public static TCPInteractiveClient getInstance(String serverAddress, int serverPort) {
        if (instance == null) {
            synchronized (TCPInteractiveClient.class) {
                if (instance == null) {
                    instance = new TCPInteractiveClient(serverAddress, serverPort);
                }
            }
        }
        return instance;
    }

    public static TCPInteractiveClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TCPInteractiveClient has not been initialized yet");
        }
        return instance;
    }

    @Override
    public void run() {
        try {
            // Establish the connection and create the input and output streams
            socket = new Socket(serverAddress, serverPort);
            in = new ObjectInputStream(socket.getInputStream());
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            log.info("Connection established");
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            // Close the input and output streams
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            // Close the socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
    }

    public boolean isAlive() {
        return socket != null && socket.isConnected();
    }

    public String sendMessageToServer(String message) {
        try {
            out.write(message + "\n");
            out.flush();
            log.info("Message sent: " + message);

            String received = in.readUTF(); // This method blocks until there is something to read from the input stream
            log.info("Message received: " + received);
            return received;
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            return "FAIL IOException: " + e.getMessage();
        } catch (NullPointerException e) {
            log.error("NullPointerException: The output stream has not been initialized yet");
            return "FAIL NullPointerException: The output stream has not been initialized yet";
        }
    }

    public List<Word> receiveDictionaryFromServer() {
        Object dictObj = receiveObjectFromServer("GET_DICT");
        if (dictObj != null) {
            List<Word> dictionary = (List<Word>) dictObj;
            log.info("Dictionary received: " + dictionary.size() + " words");
            return dictionary;
        }
        return null;
    }

    public List<Word> receiveWordsFromServer(String word) {
        Object wordsObj = receiveObjectFromServer("SEARCH " + word);
        if (wordsObj != null) {
            List<Word> words = (List<Word>) wordsObj;
            log.info("Words received: " + words);
            return words;
        }
        return null;
    }

    private Object receiveObjectFromServer(String request) {
        try {
            out.write(request + "\n");
            out.flush();
            return in.readObject(); // This method blocks until there is something to read from the input stream
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFoundException: " + e.getMessage());
        } catch (NullPointerException e) {
            log.error("NullPointerException: The input stream has not been initialized yet");
        }
        return null;
    }
}
