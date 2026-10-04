import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class GuestFacultyController {

    // PANES
    @FXML private GridPane facultyPane;
    @FXML private GridPane sessionPane;
    @FXML private TableView<Map<String, String>> historyTable;
    @FXML private TableView<Map<String, String>> feedbackTable;
    @FXML private VBox summaryBox;

    // FACULTY DETAILS
    @FXML private TextField facultyNameField;
    @FXML private TextField facultyAffiliationField;
    @FXML private ComboBox<String> designationBox;

    // SESSION FIELDS
    @FXML private TextField topicField;
    @FXML private ComboBox<String> departmentBox;
    @FXML private ComboBox<String> yearBox;
    @FXML private ComboBox<String> sectionBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;

    private ObservableList<Map<String, String>> sessionList = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> feedbackList = FXCollections.observableArrayList();

    // ✅ SAFE DB CONNECTION (FIXED: avoids ORA-12519)
    private Connection getConnection() {
        try {
            return DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe","system","Devendra");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Database Error", "Unable to connect to database");
            return null;
        }
    }

    @FXML
    public void initialize() {

        facultyPane.setVisible(false);
        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);

        designationBox.getItems().addAll("Professor","Associate Professor","Assistant Professor","Guest Faculty");

        departmentBox.getItems().addAll("CSE","CSE(AIML)","CSE(DS)","CSE(CS)","CSE&BS","ECE","EEE","MECH","CIVIL");
        yearBox.getItems().addAll("I-I","I-II","II-I","II-II","III-I","III-II","IV-I","IV-II");
        sectionBox.getItems().addAll("A","B","C","D","E","F");

        historyTable.setItems(sessionList);
        feedbackTable.setItems(feedbackList);
    }

    // SHOW FACULTY DETAILS
    @FXML private void showFacultyDetails() {
        facultyPane.setVisible(true);
        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);
    }

    // NAVIGATION
    @FXML private void showStartSession() {
        facultyPane.setVisible(false);
        sessionPane.setVisible(true);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);
    }

    @FXML private void showHistory() {
        facultyPane.setVisible(false);
        sessionPane.setVisible(false);
        historyTable.setVisible(true);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);
        loadHistory();
    }

    @FXML private void showFeedback() {
        facultyPane.setVisible(false);
        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(true);
        summaryBox.setVisible(false);
        loadFeedback();
    }

    @FXML private void showSummary() {
        facultyPane.setVisible(false);
        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(true);
        loadSummary();
    }

    // SAVE FACULTY DETAILS
    @FXML private void saveFacultyDetails() {

        if(facultyNameField.getText().isEmpty() || facultyAffiliationField.getText().isEmpty() || designationBox.getValue()==null){
            showAlert(AlertType.WARNING,"Missing Data","Fill all faculty details");
            return;
        }

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                "INSERT INTO guest_faculty VALUES (?, ?, ?)")) {

            if(con == null) return;

            ps.setString(1, facultyNameField.getText());
            ps.setString(2, facultyAffiliationField.getText());
            ps.setString(3, designationBox.getValue());

            ps.executeUpdate();

            showAlert(AlertType.INFORMATION,"Success","Faculty details saved");

            facultyNameField.clear();
            facultyAffiliationField.clear();
            designationBox.setValue(null);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // SAVE SESSION
    @FXML private void saveSession(){

        if(topicField.getText().isEmpty() || departmentBox.getValue()==null || yearBox.getValue()==null ||
           sectionBox.getValue()==null || datePicker.getValue()==null || startTimeField.getText().isEmpty() || endTimeField.getText().isEmpty()){
            showAlert(AlertType.WARNING,"Missing Data","Please fill all fields.");
            return;
        }

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                "INSERT INTO FacultyTable VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            if(con == null) return;

            String facultyName = getFacultyName(LoginController.loggedInFaculty);

            ps.setString(1, facultyName);
            ps.setString(2, topicField.getText());
            ps.setString(3, departmentBox.getValue());
            ps.setString(4, yearBox.getValue());
            ps.setString(5, sectionBox.getValue());
            ps.setDate(6, java.sql.Date.valueOf(datePicker.getValue()));
            ps.setString(7, startTimeField.getText());
            ps.setString(8, endTimeField.getText());

            ps.executeUpdate();

            showAlert(AlertType.INFORMATION,"Success","Session saved");
            clearFields();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadHistory() {

        sessionList.clear();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM FacultyTable WHERE FacultyName = ?")) {

            if(con == null) return;

            String facultyName = getFacultyName(LoginController.loggedInFaculty);
            ps.setString(1, facultyName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();

                row.put("Faculty", rs.getString("FacultyName"));
                row.put("Topic", rs.getString("SessionTopic"));
                row.put("Department", rs.getString("Department"));
                row.put("Year", rs.getString("Year"));
                row.put("Section", rs.getString("Section"));
                row.put("Date", rs.getDate("SessionDate").toString());
                row.put("Duration", rs.getString("StartTime") + " - " + rs.getString("EndTime"));

                sessionList.add(row);
            }

            updateTable(historyTable, sessionList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFeedback(){

        feedbackList.clear();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT SessionTopic, Rating, Feedback FROM StudentFeedback WHERE FacultyName=?")) {

            if(con == null) return;

            String facultyName = getFacultyName(LoginController.loggedInFaculty);
            ps.setString(1, facultyName);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                Map<String,String> row = new LinkedHashMap<>();

                row.put("Topic", rs.getString("SessionTopic"));
                row.put("Rating", rs.getString("Rating"));
                row.put("Feedback", rs.getString("Feedback"));

                feedbackList.add(row);
            }

            updateTable(feedbackTable, feedbackList);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadSummary(){

        try (Connection con = getConnection()) {

            if(con == null) return;

            String facultyName = getFacultyName(LoginController.loggedInFaculty);

            PreparedStatement ps1 = con.prepareStatement(
                "SELECT COUNT(*) FROM FacultyTable WHERE FacultyName=?"
            );
            ps1.setString(1, facultyName);
            ResultSet rs1 = ps1.executeQuery();
            rs1.next();
            int totalSessions = rs1.getInt(1);

            PreparedStatement ps2 = con.prepareStatement(
                "SELECT DISTINCT Department FROM FacultyTable WHERE FacultyName=?"
            );
            ps2.setString(1, facultyName);
            ResultSet rs2 = ps2.executeQuery();

            List<String> depts = new ArrayList<>();
            while(rs2.next()){
                depts.add(rs2.getString("Department"));
            }

            summaryBox.getChildren().clear();

            summaryBox.getChildren().addAll(
                new Label("Faculty: " + facultyName),
                new Label("Total Sessions: " + totalSessions),
                new Label("Departments Covered: " + (depts.isEmpty() ? "None" : String.join(", ", depts)))
            );

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getFacultyName(String username){
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                "SELECT name FROM faculty_login WHERE username=?")) {

            if(con == null) return username;

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getString("name");

        } catch(Exception e){
            e.printStackTrace();
        }
        return username;
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
    }

    @FXML private void logout(){
        try{
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) sessionPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void clearFields(){
        topicField.clear();
        departmentBox.setValue(null);
        yearBox.setValue(null);
        sectionBox.setValue(null);
        datePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
    }

    private void showAlert(AlertType type,String title,String message){
        Alert alert=new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}