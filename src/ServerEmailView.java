import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Observable;
import java.util.Observer;

public class ServerEmailView extends VBox implements Observer {
    private ServerEmailController serverEmailCtrl;
    private TextArea logTxtArea = new TextArea();

    public ServerEmailView(ServerEmailController serverEmailCtrl) {
        this.serverEmailCtrl = serverEmailCtrl;
        setPadding(new Insets(10));
        setSpacing(10);

        getChildren().add(logAreaPanel("Inizializzazione Log - Server operativo"));
        getChildren().add(logOptions());
        serverEmailCtrl.createLog(logTxtArea.getText());
    }

    /**
     * Metodo che crea e restituisce pannello contenente il log
     */
    private VBox logAreaPanel(String text) {
        VBox mainPanel = new VBox();
        mainPanel.setSpacing(10);
        mainPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-padding: 10;");

        logTxtArea.setEditable(false);
        logTxtArea.setWrapText(true);
        logTxtArea.setText(text);
        logTxtArea.setFont(Font.font("Helvetica", 14));
        logTxtArea.setStyle("-fx-background-color: black; -fx-text-fill: green;");

        ScrollPane logScrollPane = new ScrollPane(logTxtArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setPrefHeight(300);

        Button cleanButton = new Button("Pulisci log");
        cleanButton.setFont(Font.font("Helvetica", 14));
        cleanButton.setOnAction(event -> {
            logTxtArea.clear();
            serverEmailCtrl.createLog(logTxtArea.getText());
        });

        mainPanel.getChildren().addAll(new Text("LOG"), logScrollPane, cleanButton);
        return mainPanel;
    }

    /**
     * Metodo che crea e restituisce pannello destro contenente opzioni colore della finestra di log
     */
    private VBox logOptions() {
        VBox logOptionsPanel = new VBox();
        logOptionsPanel.setSpacing(10);

        CheckBox colorCheckBoxBG = new CheckBox("Nero/Verde");
        colorCheckBoxBG.setSelected(true);
        CheckBox colorCheckBoxBW = new CheckBox("Nero/Bianco");
        CheckBox colorCheckBoxWB = new CheckBox("Bianco/Nero");

        colorCheckBoxBG.setOnAction(event -> {
            logTxtArea.setStyle("-fx-background-color: black; -fx-text-fill: green;");
        });
        colorCheckBoxBW.setOnAction(event -> {
            logTxtArea.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        });
        colorCheckBoxWB.setOnAction(event -> {
            logTxtArea.setStyle("-fx-background-color: white; -fx-text-fill: black;");
        });

        logOptionsPanel.getChildren().addAll(colorCheckBoxBG, colorCheckBoxBW, colorCheckBoxWB);
        return logOptionsPanel;
    }

    /**
     * Metodo update, viene richiamato in seguito ad una modifica dell'elemento osservato notificata attraverso il metodo notifyObservers
     */
    @Override
    public void update(Observable o, Object arg) { if (arg instanceof ServerEmailModel.Log) {
        logTxtArea.setText(((ServerEmailModel.Log) arg).getTestoLog());
    }
    }
}