import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Email implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String sender;
    private List<String> recipients;
    private String subject;
    private String body;
    private Date sentDate;

    public Email(String id, String sender, List<String> recipients, String subject, String body, Date sentDate) {
        this.id = id;
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.sentDate = sentDate;
    }

    // Getters per tutti i campi
    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Date getSentDate() {
        return sentDate;
    }
}
