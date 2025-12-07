package inventorysystem.controllers;

import inventorysystem.dao.UserDAO;
import inventorysystem.models.User;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author jaret
 */
public class LoginController implements Initializable {

    @FXML
    private PasswordField loginPassword;
    @FXML
    private Button loginBtn;
    @FXML
    private TextField loginUsername;
    @FXML
    private Text signInBtn;
    @FXML
    private AnchorPane rootPane;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Press ENTER in password field → login
        loginPassword.setOnAction(e -> login(new ActionEvent(loginBtn, null)));

        // Press ENTER in username field → focus password OR login if password filled
        loginUsername.setOnAction(e -> {
            if (!loginPassword.getText().isEmpty()) {
                login(new ActionEvent(loginBtn, null));
            } else {
                loginPassword.requestFocus();
            }
        });
    }

    @FXML
    private void login(ActionEvent event) {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        User user = UserDAO.getUser(username, password);

        if (user != null) {
            showAlert("Success", "Login successful! Welcome " + user.getUsername());
            // 🔹 Switch to dashboard here
            try {
                // Load dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/dashboard.fxml"));
                Parent dashboardRoot = loader.load();

                // Get current stage
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Create new scene with different size
                Scene dashboardScene = new Scene(dashboardRoot, 1200, 750);
                dashboardScene.getStylesheets().add(getClass().getResource("/inventorysystem/assets/styles.css").toExternalForm());

                // Set scene & allow resizing
                stage.setScene(dashboardScene);
                stage.setResizable(true);
                stage.centerOnScreen();
                stage.getIcons().add(
                        new Image(getClass().getResourceAsStream("/inventorysystem/assets/app_icon.png"))
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Access Denied", "Invalid username or password.");
            System.out.println("Invalid username or password.");
        }
    }

    @FXML
    private void switchToSignUp(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/signup.fxml"));
            Parent nextRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            StackPane wrapper = new StackPane(nextRoot);
            Scene newScene = new Scene(wrapper, 800, 500);
            newScene.getStylesheets().add(getClass().getResource("/inventorysystem/assets/styles.css").toExternalForm());

            Parent currentRoot = ((Node) event.getSource()).getScene().getRoot();

            // FADE OUT
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            // FADE IN
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), nextRoot);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
