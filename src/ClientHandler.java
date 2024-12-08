import CommonResources.Email;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private final ServerEmailModel serverModel;
    private final String currentUserEmail;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String switchCase;

    public ClientHandler(ServerEmailModel model, String currentUserEmail, String switchCase, ObjectInputStream in, ObjectOutputStream out) {
        this.serverModel = model;
        this.currentUserEmail = currentUserEmail;
        this.switchCase = switchCase;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {

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

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
