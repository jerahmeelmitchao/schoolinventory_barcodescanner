/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package inventorysystem.controllers;

import inventorysystem.dao.UserDAO;
import inventorysystem.models.User;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jaret
 */
public class SignUpController implements Initializable {

    @FXML
    private TextField signInUsername;
    @FXML
    private Button signInBtn;
    @FXML
    private PasswordField signInPassword;
    @FXML
    private Text logInBtn;
    @FXML
    private PasswordField signInConfirmPassword;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void handleSignUp() {
        String username = signInUsername.getText().trim();
        String password = signInPassword.getText().trim();
        String confirmPassword = signInConfirmPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        User newUser = new User(username, password);
        boolean success = UserDAO.createUser(newUser);

        if (success) {
            showAlert("Success", "Account created successfully! Please log in.");
            // 🔹 Auto-switch to login.fxml after successful sign-up
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) signInBtn.getScene().getWindow();
                Scene scene = new Scene(root, 1100, 700);
                scene.getStylesheets().add(getClass().getResource("/inventorysystem/assets/styles.css").toExternalForm());

                stage.setScene(scene);
                stage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            showAlert("Error", "Failed to create account. Username might already exist.");
        }
    }

    @FXML
    private void switchToLoginPage(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("/inventorysystem/assets/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.centerOnScreen();

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
