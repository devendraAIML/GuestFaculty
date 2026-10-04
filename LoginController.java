import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.sql.*;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class LoginController {

    public static String loggedInFaculty;

    @FXML private TextField user123;
    @FXML private PasswordField p123;

    @FXML private RadioButton s123, f123, a123, gf123;

    @FXML private Button l123, c123;
    @FXML private Hyperlink forgot123;

    public void initialize() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void user1234() {}
    @FXML private void p1234() {}
    @FXML private void s1234() {}
    @FXML private void f1234() {}
    @FXML private void a1234() {}
    @FXML private void gf1234() {}

    @FXML
    private void l1234() {
        String username = user123.getText();
        String password = p123.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Enter Username & Password");
            return;
        }

        try {
            if (s123.isSelected()) {

                if (checkStudent(username, password)) {
                    Parent root = FXMLLoader.load(getClass().getResource("StudentDashboard.fxml"));
                    Stage stage = (Stage) l123.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    showAlert("Error", "Invalid Student or Not Approved");
                }

            } else if (f123.isSelected()) {

                if (checkFaculty(username, password)) {
                    Parent root = FXMLLoader.load(getClass().getResource("FacultyDashboard.fxml"));
                    Stage stage = (Stage) l123.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    showAlert("Error", "Invalid Faculty or Not Approved");
                }

            } else if (gf123.isSelected()) {

                if (checkFaculty(username, password)) {
                    Parent root = FXMLLoader.load(getClass().getResource("GuestFacultyDashboard.fxml"));
                    Stage stage = (Stage) l123.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    showAlert("Error", "Invalid Guest Faculty or Not Approved");
                }

            } else if (a123.isSelected()) {

                if (checkAdmin(username, password)) {
                    Parent root = FXMLLoader.load(getClass().getResource("AdminDashBoard.fxml"));
                    Stage stage = (Stage) l123.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } else {
                    showAlert("Error", "Invalid Admin");
                }

            } else {
                showAlert("Error", "Select Role");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void c1234() {
        try {
            Parent root;

            if (s123.isSelected()) {
                root = FXMLLoader.load(getClass().getResource("CreateAccountStudent.fxml"));
            } 
            else if (f123.isSelected() || gf123.isSelected()) {
                root = FXMLLoader.load(getClass().getResource("CreateAccountFaculty.fxml"));
            } 
            else if (a123.isSelected()) {
                root = FXMLLoader.load(getClass().getResource("CreateAccountAdminPage.fxml"));
            } 
            else {
                showAlert("Error", "Select Role First");
                return;
            }

            Stage stage = (Stage) c123.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "FXML File Not Found!");
        }
    }

    @FXML
    private void forgot1234(ActionEvent event) {
        try {
            String selectedRole = null;

            if (s123.isSelected()) selectedRole = "Student";
            else if (f123.isSelected()) selectedRole = "Faculty";
            else if (gf123.isSelected()) selectedRole = "Faculty";
            else if (a123.isSelected()) selectedRole = "Admin";

            if (selectedRole == null) {
                showAlert("Error", "Please select role first");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ForgotPassword.fxml"));
            Parent root = loader.load();

            ForgotPasswordController controller = loader.getController();
            controller.setUserType(selectedRole);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ FIXED (AUTO CLOSE CONNECTION)
    private boolean checkStudent(String u, String p) {
        String sql = "SELECT * FROM student_login WHERE username=? AND password=?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe","system","Devendra");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u);
            ps.setString(2, p);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkFaculty(String u, String p) {
        String sql = "SELECT * FROM faculty_login WHERE username=? AND password=?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe","system","Devendra");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u);
            ps.setString(2, p);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                loggedInFaculty = rs.getString("username");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkAdmin(String u, String p) {
        String sql = "SELECT * FROM admin_login WHERE username=? AND password=?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe","system","Devendra");
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u);
            ps.setString(2, p);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}