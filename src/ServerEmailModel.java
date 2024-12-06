import CommonResources.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.io.*;

public class ServerEmailModel extends Observable {

    private ObservableList<String> logList;
    private ArrayList<String[]> emailData; // tutto il csv in una arraylist
    private int cntLette = 0;

    public ServerEmailModel(String csvFilePath) {

        emailData = new ArrayList<>();
        logList = FXCollections.observableArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("#");
                emailData.add(values);  // Add the loaded email to in-memory list
            }
        } catch (IOException e) {
            addLog("Errore durante l'aggiornamento del file CSV");
        }
    }

    public int getCntLette() {
        return cntLette;
    }
    public void setCntLette(int cntLette) {this.cntLette = cntLette;}

    /**
     * Metodo che imposta come letta una determinata mail all'interno del server
     * quando lato client si clicca su di essa.
     */
    public synchronized boolean setReadMail(Email mail) {
        boolean emailFound = false;

        // Trova la mail corrispondente
        for (String[] emailFields : emailData) {
            if (emailFields[0].equals(mail.getMittEmail()) &&               // Mittente
                    emailFields[1].equals(mail.getCcString()) &&               // Destinatari
                    emailFields[2].equals(mail.getArgEmail()) &&               // Argomento
                    emailFields[3].equals(mail.getDataSpedEmail()) &&          // Data
                    emailFields[4].equals(mail.getTestoEmail().replace("\n", "§"))) { // Testo
                // La mail è stata trovata, segna come letta
                emailFields[5] = "true";  // Imposta il flag "isRead" a true
                emailFound = true;
                break;
            }
        }

        if (emailFound) {
            // Aggiorna il file CSV dopo aver marcato la mail come letta
            updateCsvFile();
            addLog("commonResources.Email letta: " + mail.getMittEmail() + " - " + mail.getArgEmail());
            return true;
        } else {
            addLog("Errore: commonResources.Email non trovata per l'indirizzo: " + mail.getMittEmail());
            return false;
        }
    }


    /**
     * Metodo che aggiunge alla lista delle mail di un utente la nuova mail inviata
     * @syncronized per evitare che ci sia una lettura mentre avviene la scrittura
     * nel caso in cui non esistelle la casella mail a cui aggiungerla: messaggio di errore
     * @param mail mail da inviare
     */

    public synchronized boolean inviaMail(Email mail) throws Exception {

        mail.setTestoEmail(mail.getTestoEmail().replace("§", "\n"));
        String[] newEmail = {
                mail.getMittEmail(),
                mail.getCcString(), // Comma-separated list of recipients
                mail.getArgEmail(),
                mail.getDataSpedEmail(),
                mail.getTestoEmail().replace("\n", "§"),
                Boolean.toString(mail.getIsRead())       // Convert boolean isRead to a string ("true"/"false")
        };
        emailData.add(newEmail);
        updateCsvFile();
        addLog(mail.getMittEmail() + " ha inviato una nuova mail a: " + mail.getCcString());
        return true;
    }


    /**
     * Metodo che rimuove dalla lista di mail la
     * @param mailToDelete è la mail da eliminare
     * @param key l'indirizzo mail associato
     */
    public void deleteEmail(String key, Email mailToDelete) {
        Iterator<String[]> iterator = emailData.iterator();
        boolean found = false;

        while (iterator.hasNext()) {
            String[] emailFields = iterator.next();

            // Confronta tutti i campi univoci dell'email
            if (emailFields[0].equals(mailToDelete.getMittEmail()) &&               // Mittente
                    emailFields[1].equals(mailToDelete.getCcString()) &&               // Destinatari
                    emailFields[2].equals(mailToDelete.getArgEmail()) &&               // Argomento
                    emailFields[3].equals(mailToDelete.getDataSpedEmail()) &&          // Data
                    emailFields[4].equals(mailToDelete.getTestoEmail().replace("\n", "§"))) { // Testo
                iterator.remove();
                found = true;
                break;
            }
        }

        if (found) {
            updateCsvFile(); // Aggiorna il file CSV dopo aver eliminato l'email
            addLog("commonResources.Email rimossa correttamente: " + mailToDelete);
        } else {
            addLog("Errore: L'email specificata non è stata trovata");
        }
    }


    /**
     * Metodo che aggiorna il file CSV con il contenuto corrente di emailData.
     */
    private synchronized void updateCsvFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("src/Server/archives/email.csv"))) {
            for (String[] emailFields : emailData) {
                String line = String.join("#", emailFields);
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            addLog("Errore durante l'aggiornamento del file CSV: " + e.getMessage());
        }
    }


    public ArrayList<Email> getEmailDataForUser(String userEmail) {
        ArrayList<Email> userEmails = new ArrayList<>();
        for (String[] emailFields : emailData) {
            if (emailFields[1].contains(userEmail)) {
                Email email = new Email();
                email.setMittEmail(emailFields[0]);
                email.setDestsEmail(Arrays.asList(emailFields[1].split(",")));
                email.setArgEmail(emailFields[2]);
                email.setDataSpedEmail(emailFields[3]);
                email.setTestoEmail(emailFields[4].replace("§", "\n"));
                boolean isRead = emailFields.length > 5 && emailFields[5].equals("true");
                email.setRead(isRead);

                if (Objects.equals(emailFields[5], "false")) {
                    cntLette++;
                }

                userEmails.add(email);
            }
        }
        userEmails.sort((e1, e2) -> {
            if (e1.getDataSpedEmail() == null) return 1;
            if (e2.getDataSpedEmail() == null) return -1;
            return e2.getDataSpedEmail().compareTo(e1.getDataSpedEmail());
        });


        return userEmails;
    }



    /**
     * METODI LOG
     */


    public ObservableList<String> getLogList() {
        return logList;
    }
    public void clearLog(ObservableList<String> logList){
        logList.clear();
        setChanged();
        notifyObservers(this);
    }
    public void addLog(String log) {
        logList.add(log); // Aggiungi il log alla lista
        setChanged();     // Indica che il modello è stato modificato
        notifyObservers(logList); // Notifica gli osservatori, passando la lista aggiornata
    }
    public void setTestoLog(ObservableList<String> logList) {
        this.logList = logList;
    }
}
