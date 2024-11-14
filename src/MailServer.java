import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;
import java.util.*;

public class MailServer extends Application {
    private static final int PORT = 12345;
    private Map<String, ServerMailbox> mailboxes;
    private TextArea logArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mailboxes = new HashMap<>();
        loadMailboxes();

        logArea = new TextArea();
        logArea.setEditable(false);

        VBox root = new VBox(logArea);
        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("Mail Server Log");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(this::startServer).start();
    }

    private void loadMailboxes() {
        String[] accounts = {"giorgio@mia.mail.com", "maria@mia.mail.com", "luca@mia.mail.com"};
        for (String account : accounts) {
            mailboxes.put(account, new ServerMailbox(account));
            mailboxes.get(account).loadMessages();
        }
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            log("Error starting server: " + e.getMessage());
        }
    }

    private void log(String message) {
        javafx.application.Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    private class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

                Email email = (Email) in.readObject();
                log("Received email from " + email.getSender());

                for (String recipient : email.getRecipients()) {
                    ServerMailbox mailbox = mailboxes.get(recipient);
                    if (mailbox != null) {
                        mailbox.addMessage(email);
                        mailbox.saveMessages();
                        log("Delivered to " + recipient);
                    } else {
                        log("Delivery failed: " + recipient + " not found");
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                log("Error handling client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}
