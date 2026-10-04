import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.sql.*;
import java.util.*;

public class AdminController {

    @FXML private AnchorPane mainContent;
    @FXML private GridPane requestPane;
    @FXML private VBox viewDetailsBox;
    @FXML private VBox feedbackButtonsBox;
    @FXML private VBox summaryButtonsBox;

    @FXML private TableView<Map<String,String>> sessionDetailsTable;
    @FXML private TableView<Map<String,String>> ratingTable;
    @FXML private TableView<Map<String,String>> feedbackTable;
    @FXML private TableView<Map<String,String>> summaryTable;

    @FXML private ComboBox<String> userTypeBox;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField contactField;

    private final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private final String DB_USER = "system";
    private final String DB_PASS = "Devendra";

    private List<Request> requests = new ArrayList<>();
    private int currentIndex = 0;

    // ================= DB =================
    private Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            showAlert("DB Error", "Connection Failed");
            return null;
        }
    }

    @FXML
    public void initialize() {
        userTypeBox.getItems().addAll("Faculty", "Student");
    }

    // ================= REQUESTS =================
    @FXML
    private void showRequests() {
        hideAll();
        requestPane.setVisible(true);
        loadRequests();
        currentIndex = 0;
        loadNextRequest();
    }

    private void loadRequests() {
        requests.clear();

        try (Connection con = getConnection()) {
            if (con == null) return;

            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM student_login_requests");

            while (rs.next()) {
                requests.add(new Request("Student",
                        rs.getInt("request_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("registernumber")));
            }

            rs = con.createStatement().executeQuery("SELECT * FROM faculty_login_requests");

            while (rs.next()) {
                requests.add(new Request("Faculty",
                        rs.getInt("request_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("gmail")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNextRequest() {
        if (currentIndex >= requests.size()) {

            // ✅ CLEAR UI when no requests
            userTypeBox.setValue(null);
            usernameField.clear();
            passwordField.clear();
            contactField.clear();

            showAlert("Done", "No more requests");
            return;
        }

        Request r = requests.get(currentIndex);

        userTypeBox.setValue(r.type);
        usernameField.setText(r.username);
        passwordField.setText(r.password);
        contactField.setText(r.contact);
    }

    @FXML
    private void acceptRequest() {

        if (currentIndex >= requests.size()) return;

        Request r = requests.get(currentIndex);

        try (Connection con = getConnection()) {
            if (con == null) return;

            if (r.type.equals("Student")) {

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO student_login (name, registernumber, branch, section, username, password) " +
                        "SELECT name, registernumber, branch, section, username, password " +
                        "FROM student_login_requests WHERE request_id=?"
                );
                ps.setInt(1, r.id);
                ps.executeUpdate();

                ps = con.prepareStatement("DELETE FROM student_login_requests WHERE request_id=?");
                ps.setInt(1, r.id);
                ps.executeUpdate();

            } else {

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO faculty_login (name, username, gmail, password) " +
                        "SELECT name, username, gmail, password " +
                        "FROM faculty_login_requests WHERE request_id=?"
                );
                ps.setInt(1, r.id);
                ps.executeUpdate();

                ps = con.prepareStatement("DELETE FROM faculty_login_requests WHERE request_id=?");
                ps.setInt(1, r.id);
                ps.executeUpdate();
            }

            showAlert("Success", "Account Created");

            // ✅ remove processed request
            requests.remove(currentIndex);
            loadNextRequest();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void rejectRequest() {

        if (currentIndex >= requests.size()) return;

        Request r = requests.get(currentIndex);

        try (Connection con = getConnection()) {
            if (con == null) return;

            String table = r.type.equals("Student")
                    ? "student_login_requests"
                    : "faculty_login_requests";

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM " + table + " WHERE request_id=?");
            ps.setInt(1, r.id);
            ps.executeUpdate();

            showAlert("Rejected", "Deleted");

            // ✅ remove processed request
            requests.remove(currentIndex);
            loadNextRequest();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SESSION =================
    @FXML
    private void showSession() {
        hideAll();
        viewDetailsBox.setVisible(true);
    }

    @FXML
    private void showSessionDetails() {

        sessionDetailsTable.setVisible(true);

        ObservableList<Map<String,String>> data = FXCollections.observableArrayList();

        try (Connection con = getConnection()) {
            if (con == null) return;

            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT FacultyName, SessionTopic, SessionDate, StartTime, EndTime FROM FacultyTable");

            while (rs.next()) {
                Map<String,String> row = new LinkedHashMap<>();

                row.put("Faculty Name", rs.getString("FacultyName"));
                row.put("Session Name", rs.getString("SessionTopic"));
                row.put("Session Duration", rs.getString("StartTime")+"-"+rs.getString("EndTime"));
                row.put("Session Date", rs.getDate("SessionDate").toString());

                data.add(row);
            }

            updateTable(sessionDetailsTable, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FEEDBACK =================
    @FXML
    private void showFeedback() {
        hideAll();
        feedbackButtonsBox.setVisible(true);
    }

    @FXML
    private void showRatingTable() {

        ratingTable.setVisible(true);
        feedbackTable.setVisible(false);

        ObservableList<Map<String,String>> data = FXCollections.observableArrayList();

        try (Connection con = getConnection()) {
            if (con == null) return;

            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT FacultyName, SessionTopic, ROUND(AVG(Rating),2) AS AvgRating " +
                    "FROM StudentFeedback GROUP BY FacultyName, SessionTopic");

            while (rs.next()) {
                Map<String,String> row = new LinkedHashMap<>();

                row.put("Faculty", rs.getString("FacultyName"));
                row.put("Session", rs.getString("SessionTopic"));
                row.put("Rating", rs.getString("AvgRating"));

                data.add(row);
            }

            updateTable(ratingTable, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showFeedbackTable() {

        feedbackTable.setVisible(true);
        ratingTable.setVisible(false);

        ObservableList<Map<String,String>> data = FXCollections.observableArrayList();

        try (Connection con = getConnection()) {
            if (con == null) return;

            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT FacultyName, SessionTopic, Feedback FROM StudentFeedback");

            while (rs.next()) {
                Map<String,String> row = new LinkedHashMap<>();

                row.put("Faculty", rs.getString("FacultyName"));
                row.put("Session", rs.getString("SessionTopic"));
                row.put("Feedback", rs.getString("Feedback"));

                data.add(row);
            }

            updateTable(feedbackTable, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SUMMARY =================
    @FXML
    private void showSummary() {
        hideAll();
        summaryButtonsBox.setVisible(true);
    }

    @FXML
    private void generateSummary() {

        summaryTable.setVisible(true);

        ObservableList<Map<String,String>> data = FXCollections.observableArrayList();

        try (Connection con = getConnection()) {
            if (con == null) return;

            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT FacultyName, SessionTopic, SessionDate, StartTime, EndTime FROM FacultyTable");

            while (rs.next()) {
                Map<String,String> row = new LinkedHashMap<>();

                row.put("Faculty", rs.getString("FacultyName"));
                row.put("Session", rs.getString("SessionTopic"));
                row.put("Duration", rs.getString("StartTime")+"-"+rs.getString("EndTime"));
                row.put("Date", rs.getDate("SessionDate").toString());

                data.add(row);
            }

            updateTable(summaryTable, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UTIL =================
    private void hideAll() {
        requestPane.setVisible(false);
        viewDetailsBox.setVisible(false);
        feedbackButtonsBox.setVisible(false);
        summaryButtonsBox.setVisible(false);

        sessionDetailsTable.setVisible(false);
        ratingTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryTable.setVisible(false);
    }

    private void updateTable(TableView<Map<String,String>> table, ObservableList<Map<String,String>> data){

        table.getColumns().clear();

        if(data.isEmpty()) return;

        for(String key : data.get(0).keySet()){

            TableColumn<Map<String,String>, String> col = new TableColumn<>(key);

            col.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(d.getValue().get(key)));

            table.getColumns().add(col);
        }

        table.setItems(data);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= MODEL =================
    static class Request {
        String type;
        int id;
        String username;
        String password;
        String contact;

        Request(String type, int id, String username, String password, String contact) {
            this.type = type;
            this.id = id;
            this.username = username;
            this.password = password;
            this.contact = contact;
        }
    }
}