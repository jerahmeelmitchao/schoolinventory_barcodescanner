package inventorysystem.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardController {

    @FXML
    private StackPane mainContent;

    @FXML
    private Button dashboardBtn;
    @FXML
    private Button itemsBtn;
    @FXML
    private Button categoriesBtn;
    @FXML
    private Button borrowersBtn;
    @FXML
    private Button inchargesBtn;
    @FXML
    private Button scannedItemsBtn;
    @FXML
    private Button logoutBtn;

    private Map<Button, String> buttonFxmlMap;

    public void initialize() {

        buttonFxmlMap = new HashMap<>();
        buttonFxmlMap.put(dashboardBtn, "dashboard2.fxml");
        buttonFxmlMap.put(itemsBtn, "items.fxml");
        buttonFxmlMap.put(categoriesBtn, "category.fxml");
        buttonFxmlMap.put(borrowersBtn, "BorrowerManagement.fxml");
        buttonFxmlMap.put(inchargesBtn, "InCharge.fxml");
        buttonFxmlMap.put(scannedItemsBtn, "scanned_items.fxml");

        // Navigation buttons
        buttonFxmlMap.keySet().forEach(btn -> btn.setOnAction(e -> loadView(btn)));

        // Logout button
        logoutBtn.setOnAction(e -> handleLogout());

        loadView(dashboardBtn); // default
    }

    private void loadView(Button clickedButton) {

        // Apply active style class
        buttonFxmlMap.keySet().forEach(btn -> {
            btn.getStyleClass().remove("active-sidebar-btn");
            if (btn == clickedButton) {
                btn.getStyleClass().add("active-sidebar-btn");
            }
        });

        // Load FXML
        String fxmlFile = buttonFxmlMap.get(clickedButton);
        URL fxmlUrl = getClass().getResource("/inventorysystem/views/" + fxmlFile);

        if (fxmlUrl == null) {
            System.err.println("❌ FXML file not found: " + fxmlFile);
            return;
        }

        try {
            Node view = FXMLLoader.load(fxmlUrl);
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("❌ Failed to load view: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void handleLogout() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Logout");

        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 20;");

        // Define the buttons FIRST
        ButtonType logoutType = new ButtonType("Logout", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(logoutType, cancelType);

        // Create custom UI content
        VBox box = new VBox(
                new javafx.scene.control.Label("Are you sure you want to logout?")
        );
        box.setSpacing(15);
        dialog.getDialogPane().setContent(box);

        // Style the actual buttons that appear
        Button logoutBtn = (Button) dialog.getDialogPane().lookupButton(logoutType);
        logoutBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white; -fx-padding:6 18; -fx-background-radius:6;");

        Button cancelBtn = (Button) dialog.getDialogPane().lookupButton(cancelType);
        cancelBtn.setStyle("-fx-background-color:#bdc3c7; -fx-text-fill:black; -fx-padding:6 18; -fx-background-radius:6;");

        // Process button result
        dialog.setResultConverter(result -> {
            if (result == logoutType) {
                goToLoginScreen();
            }
            return null;
        });

        dialog.showAndWait();  // Works correctly now
    }

    private void goToLoginScreen() {
        try {
            URL fxmlUrl = getClass().getResource("/inventorysystem/views/Login.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ Login.fxml NOT FOUND!");
                return;
            }

            Parent loginRoot = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) mainContent.getScene().getWindow();

            StackPane animationWrapper = new StackPane(loginRoot);

            Scene newScene = new Scene(animationWrapper, 800, 500);
            newScene.getStylesheets().add(
                    getClass().getResource("/inventorysystem/assets/styles.css").toExternalForm()
            );

            // ----------------------------------
            // FADE OUT the current dashboard
            // ----------------------------------
            Parent currentRoot = mainContent.getScene().getRoot();

            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            // ----------------------------------
            // FADE IN the login screen
            // ----------------------------------
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), loginRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            fadeOut.setOnFinished(e -> {
                stage.setScene(newScene);
                stage.centerOnScreen();
                fadeIn.play();
            });

            fadeOut.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
