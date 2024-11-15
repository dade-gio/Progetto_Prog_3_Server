import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private ServerEmailModel serverModel;

    public ClientHandler(Socket socket, ServerEmailModel model) {
        this.clientSocket = socket;
        this.serverModel = model;
    }

    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            while (true) {
                String command = in.readUTF();
                switch (command) {
                    case "SEND_EMAIL":
                        Email email = (Email) in.readObject();
                        boolean success = serverModel.getLogServer().inviaMail(email);
                        out.writeBoolean(success);
                        break;
                    case "GET_EMAIL":
                        String address = in.readUTF();
                        ArrayList<Email> emails = serverModel.getLogServer().getEmail(address);
                        out.writeObject(emails);
                        break;
                    case "DELETE_EMAIL":
                        String key = in.readUTF();
                        Email mailToDelete = (Email) in.readObject();
                        //serverModel.deleteMail(key, mailToDelete);
                        out.writeBoolean(true);
                        break;
                    default:
                        out.writeUTF("Unknown command");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}