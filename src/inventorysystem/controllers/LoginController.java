/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    @FXML
    private void switchToSignUp(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/inventorysystem/views/signup.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("/inventorysystem/assets/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
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
