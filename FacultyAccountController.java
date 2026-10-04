import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FacultyAccountController {

    @FXML private TextField nameField, regNoField, usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;

    public void initialize() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void name12() {}
    @FXML private void reg123() {}
    @FXML private void user12() {}
    @FXML private void pass12() {}

    @FXML
    private void togglePassword() {
        if (passwordVisible.isVisible()) {
            passwordField.setText(passwordVisible.getText());
            passwordVisible.setVisible(false);
            passwordField.setVisible(true);
        } else {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordField.setVisible(false);
        }
    }

    @FXML
    private void req123() {
        try {
            DriverManager.setLoginTimeout(5);

            try (
                Connection conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe",
                    "system",
                    "Devendra"
                );
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO faculty_login_requests " +
                    "(request_id, name, username, gmail, password) " +
                    "VALUES (faculty_req_seq.NEXTVAL, ?, ?, ?, ?)"
                )
            ) {

                ps.setString(1, nameField.getText());
                ps.setString(2, usernameField.getText());
                ps.setString(3, regNoField.getText());
                ps.setString(4, passwordField.getText());

                ps.executeUpdate();

                showAlert("Success", "Faculty Request Sent!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void b123() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setContentText(m);
        a.showAndWait();
    }
}