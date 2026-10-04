import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.sql.*;

public class ForgotPasswordController {

    @FXML private TextField gmail123;

    @FXML private PasswordField newpass123;
    @FXML private TextField newpassVisible;

    @FXML private PasswordField confirm123;
    @FXML private TextField confirmVisible;

    @FXML private Label errorLabel;

    @FXML private Button submit123, back123;

    // 🔥 ROLE FROM LOGIN
    private String userType;

    public void setUserType(String type) {
        this.userType = type;
    }

    // ✅ SAFE DB CONNECTION (FINAL)
    private Connection getConnection() {
        int retries = 3;

        while (retries-- > 0) {
            try {
                DriverManager.setLoginTimeout(5); // 🔥 prevents freeze
                return DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe",
                    "system",
                    "Devendra"
                );
            } catch (Exception e) {
                if (retries == 0) {
                    showAlert("Database Error", "Unable to connect. Try again later.");
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    @FXML
    public void initialize() {

        newpassVisible.setVisible(false);
        newpassVisible.setManaged(false);

        confirmVisible.setVisible(false);
        confirmVisible.setManaged(false);
    }

    // ===== EMPTY METHODS =====
    @FXML private void gmail1234() {}
    @FXML private void newpass1234() {}
    @FXML private void confirm1234() {}

    // ===== TOGGLE PASSWORD =====
    @FXML
    private void toggleNewPass() {
        if (newpassVisible.isVisible()) {
            newpass123.setText(newpassVisible.getText());
            newpassVisible.setVisible(false);
            newpassVisible.setManaged(false);

            newpass123.setVisible(true);
            newpass123.setManaged(true);
        } else {
            newpassVisible.setText(newpass123.getText());
            newpassVisible.setVisible(true);
            newpassVisible.setManaged(true);

            newpass123.setVisible(false);
            newpass123.setManaged(false);
        }
    }

    @FXML
    private void toggleConfirmPass() {
        if (confirmVisible.isVisible()) {
            confirm123.setText(confirmVisible.getText());
            confirmVisible.setVisible(false);
            confirmVisible.setManaged(false);

            confirm123.setVisible(true);
            confirm123.setManaged(true);
        } else {
            confirmVisible.setText(confirm123.getText());
            confirmVisible.setVisible(true);
            confirmVisible.setManaged(true);

            confirm123.setVisible(false);
            confirm123.setManaged(false);
        }
    }

    // ===== SUBMIT =====
    @FXML
    private void submit1234() {

        String userInput = gmail123.getText().trim();
        String pass = newpass123.isVisible() ? newpass123.getText() : newpassVisible.getText();
        String confirm = confirm123.isVisible() ? confirm123.getText() : confirmVisible.getText();

        errorLabel.setText("");

        if (userType == null) {
            errorLabel.setText("User type not set. Please go back and try again.");
            return;
        }

        if (userInput.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("All fields are required");
            return;
        }

        if (!pass.equals(confirm)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        try (Connection conn = getConnection()) {

            if (conn == null) return;

            int updated = 0;

            if (userType.equals("Student")) {

                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE student_login SET password=? WHERE registernumber=?"
                )) {
                    ps.setString(1, pass);
                    ps.setString(2, userInput);
                    updated = ps.executeUpdate();
                }

            } else if (userType.equals("Faculty")) {

                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE faculty_login SET password=? WHERE gmail=?"
                )) {
                    ps.setString(1, pass);
                    ps.setString(2, userInput);
                    updated = ps.executeUpdate();
                }

            } else if (userType.equals("Admin")) {

                try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE admin_login SET password=? WHERE mail=?"
                )) {
                    ps.setString(1, pass);
                    ps.setString(2, userInput);
                    updated = ps.executeUpdate();
                }
            }

            if (updated > 0) {
                showAlert("Success", "Password Updated Successfully");
                goBack();
            } else {
                errorLabel.setText("Account does not exist");
            }

        } catch (Exception e) {
            errorLabel.setText("Database Error");
        }
    }

    // ===== BACK =====
    @FXML
    private void back1234() {
        goBack();
    }

    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) back123.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== ALERT =====
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}