import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ServerEmailApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Modello
        ServerEmailModel serverEmailMod = new ServerEmailModel();

        // Start the server on port 12345 (or any available port)
        serverEmailMod.startServer(12345);

        // Controller
        ServerEmailController serverEmailCtrl = new ServerEmailController(serverEmailMod);

        // View
        ServerEmailView serverEmailView = new ServerEmailView(serverEmailCtrl);

        // Instaurazione relazione observer-observerable tra vista (Observer) e modello (Observable)
        serverEmailMod.addObserver(serverEmailView);

        // Layout principale
        BorderPane root = new BorderPane();
        root.setCenter(serverEmailView);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Gestione della chiusura della finestra
        primaryStage.setOnCloseRequest(event -> {
            // Aggiungi eventuale logica di chiusura qui
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}