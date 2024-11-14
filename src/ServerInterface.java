import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerInterface extends Remote {
    int getInfoLetture(String address) throws RemoteException;
    void appendToLog(String testoLog) throws RemoteException;
    boolean inviaMail(Email mail) throws RemoteException;
    ArrayList<Email> getEmail(String address) throws RemoteException;
    void deleteEmail(String key, Email mail) throws RemoteException;
    void setReadMail(String address, Email mail) throws RemoteException;
}