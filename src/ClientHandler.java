import CommonResources.Email;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private final ServerEmailModel serverModel;
    private final String currentUserEmail;
    private final Socket clientSocket;

    public ClientHandler(ServerEmailModel model, String currentUserEmail, Socket clientSocket) {
        this.serverModel = model;
        this.currentUserEmail = currentUserEmail;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (true) {
                String switchCase = in.readUTF(); // Legge un comando dal client

                // Se la connessione è chiusa, esci dal ciclo
                if (clientSocket.isClosed()) {
                    System.out.println("Connessione chiusa, interrompo il thread.");
                    break;
                }

                switch (switchCase) {
                    case "SYNC":
                        ArrayList<Email> userEmails = (ArrayList<Email>) serverModel.getEmailDataForUser(currentUserEmail);
                        out.writeObject(userEmails);
                        out.flush();
                        break;

                    case "SEND_EMAIL":
                        Email email = (Email) in.readObject();
                        serverModel.inviaMail(email);
                        out.writeUTF("OK");
                        out.flush();
                        break;

                    case "DELETE_EMAIL":
                        String mittente = in.readUTF(); // mittente in stringa
                        Email mailToDelete = (Email) in.readObject(); // struttura mail
                        serverModel.deleteEmail(mittente, mailToDelete);
                        out.writeUTF("OK");
                        out.flush();
                        break;

                    case "IS_READ":
                        Email email1 = (Email) in.readObject();
                        serverModel.setReadMail(email1);
                        break;

                    default:
                        out.writeUTF("Unknown command");
                        out.flush();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                // Assicurati che il socket venga chiuso correttamente
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
