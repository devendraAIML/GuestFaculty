import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdminAccountController {

    @FXML private TextField mail123, uername123;
    @FXML private PasswordField password123;
    @FXML private TextField passwordVisible;

    public void initialize() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 SAFE DB CONNECTION (ADDED)
    private Connection getConnection() {

        int retries = 3;

        while (retries-- > 0) {
            try {
                return DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe",
                    "system",
                    "Devendra"
                );

            } catch (Exception e) {

                if (e.getMessage() != null && e.getMessage().contains("ORA-12519")) {
                    System.out.println("⚠ DB Busy... retrying");

                    try { Thread.sleep(1000); } catch (Exception ignored) {}
                } else {
                    e.printStackTrace();
                    break;
                }
            }
        }

        showAlert("Error", "Database Busy. Try again!");
        return null;
    }

    // EMPTY METHODS
    @FXML private void mail1234() {}
    @FXML private void uername1234() {}
    @FXML private void password1234() {}

    @FXML
    private void togglePassword() {
        if (passwordVisible.isVisible()) {
            password123.setText(passwordVisible.getText());
            passwordVisible.setVisible(false);
            password123.setVisible(true);
        } else {
            passwordVisible.setText(password123.getText());
            passwordVisible.setVisible(true);
            password123.setVisible(false);
        }
    }

    @FXML
    private void submit1234() {

        Connection conn = getConnection();
        if (conn == null) return; // 🔥 PREVENT CRASH

        try (
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO admin_login (mail, username, password) VALUES (?, ?, ?)"
            )
        ) {

            ps.setString(1, mail123.getText());
            ps.setString(2, uername123.getText());
            ps.setString(3, password123.getText());

            ps.executeUpdate();

            showAlert("Success", "Admin Created!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to create admin");
        } finally {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void back1234() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) mail123.getScene().getWindow();
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