import javafx.collections.ObservableList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ServerEmailController implements ActionListener {
    private final ServerEmailModel serverEmailMod;
    private final ObservableList<String> logList;

    public ServerEmailController(ServerEmailModel serverEmailMod, ObservableList<String> list) {
        this.serverEmailMod = serverEmailMod;
        this.logList = list;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        serverEmailMod.clearLog(logList);
    }

    public void createLog(String textLog) {
        try {
            serverEmailMod.addLog(textLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}