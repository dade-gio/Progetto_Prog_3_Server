import CommonResources.Email;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerEmailApp extends Application {

    private static final int PORT = 12345;
    private ExecutorService threadPool;
    private ServerSocket serverSocket;
    private ServerEmailModel emailModel;
    private int lette = 0;

    @Override
    public void start(Stage primaryStage) {
        try {

            emailModel = new ServerEmailModel("src/email.csv");

            lette = emailModel.getCntLette();

            ServerEmailView emailView = new ServerEmailView(emailModel);

            // Layout dell'interfaccia utente
            BorderPane root = new BorderPane();
            root.setCenter(emailView);
            Scene scene = new Scene(root, 1000, 600);
            primaryStage.setTitle("Server commonResources.Email");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Avvio del server
            startServer();

            // Gestione della chiusura della finestra
            primaryStage.setOnCloseRequest(event -> stopServer());
        } catch (Exception e) {
            System.err.println("Errore nell'avvio del server: " + e.getMessage());
        }
    }

    private void startServer() {
        threadPool = Executors.newFixedThreadPool(10);

        new Thread(() -> {
            try {
                // Specifica l'IP e la porta
                InetAddress bindAddress = InetAddress.getByName("0.0.0.0"); // Sostituisci con l'IP desiderato
                serverSocket = new ServerSocket(PORT, 50, bindAddress);
                emailModel.addLog("Server in ascolto su IP: " + bindAddress + " e porta: " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket, lette));
                }
            } catch (IOException e) {
                emailModel.addLog("Errore nella comunicazione con il server: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket clientSocket, int numLette) {
        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            // Leggi l'elenco degli utenti registrati
            Set<String> validUsers = loadRegisteredUsers("src/users.txt");

            // Attendi email dell'utente
            String currentUser = in.readUTF();
            emailModel.addLog("Tentativo di connessione da parte di: " + currentUser);

            // Verifica se l'utente è registrato
            if (!validUsers.contains(currentUser.toLowerCase())) {
                emailModel.addLog("Connessione rifiutata: utente non registrato - " + currentUser);
                out.writeObject(currentUser);
                out.writeInt(lette);
                out.flush();
            }

            emailModel.setCntLette(0);

            // Ottieni email dell'utente dal modello
            ArrayList<Email> userEmails = emailModel.getEmailDataForUser(currentUser);
            if (userEmails == null) {
                emailModel.addLog("Utente non esistente");
                out.writeUTF("Utente non esistente");
                out.flush();
                return;
            }

            numLette = emailModel.getCntLette();


            out.writeObject(userEmails);
            out.writeInt(numLette);
            out.flush();

            while (true) {
                String switchCase = in.readUTF();
                ClientHandler clientHandler = new ClientHandler(emailModel, currentUser, switchCase, in, out);
                clientHandler.run();
            }

        } catch (IOException e) {
            e.printStackTrace();
            emailModel.addLog("Errore nella comunicazione con il client");
        }
    }

    private void stopServer() {
        try {
            if (threadPool != null) {
                threadPool.shutdown();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            emailModel.addLog("Server arrestato correttamente");
        } catch (IOException e) {
            emailModel.addLog("Errore durante l'arresto del server");
        }
    }

    private Set<String> loadRegisteredUsers(String filePath) {
        Set<String> users = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                users.add(line.trim().toLowerCase()); // Converti in minuscolo per evitare problemi di case-sensitive
            }
        } catch (IOException e) {
            emailModel.addLog("Errore durante la lettura degli utenti registrati: " + e.getMessage());
        }
        return users;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
