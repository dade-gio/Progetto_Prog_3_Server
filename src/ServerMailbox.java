import java.io.*;
import java.util.*;

public class ServerMailbox {
    private String account;
    private List<Email> messages;
    private File mailboxFile;

    public ServerMailbox(String account) {
        this.account = account;
        this.messages = new ArrayList<>();
        this.mailboxFile = new File("mail_data/" + account + ".txt");
    }

    public void addMessage(Email email) {
        messages.add(email);
    }

    public void loadMessages() {
        if (mailboxFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mailboxFile))) {
                messages = (List<Email>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMessages() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mailboxFile))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
