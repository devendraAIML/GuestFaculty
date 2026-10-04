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

public class FacultyController {

    @FXML private GridPane sessionPane;
    @FXML private TableView<Map<String, String>> historyTable;
    @FXML private TableView<Map<String, String>> feedbackTable;
    @FXML private VBox summaryBox;

    @FXML private TextField topicField;
    @FXML private ComboBox<String> departmentBox;
    @FXML private ComboBox<String> yearBox;
    @FXML private ComboBox<String> sectionBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;

    private ObservableList<Map<String, String>> sessionList = FXCollections.observableArrayList();
    private ObservableList<Map<String, String>> feedbackList = FXCollections.observableArrayList();

    // ✅ SAFE DB CONNECTION (FINAL FIX)
    private Connection getConnection() {
        int retries = 3;

        while (retries-- > 0) {
            try {
                DriverManager.setLoginTimeout(5); // 🔥 prevents hanging
                return DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe","system","Devendra");
            } catch (Exception e) {
                if (retries == 0) {
                    showAlert(AlertType.ERROR, "Database Error",
                            "Unable to connect to database.\nTry again in few seconds.");
                }
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }

    @FXML
    public void initialize() {

        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);

        departmentBox.getItems().addAll("CSE","CSE(AIML)","CSE(DS)","CSE(CS)","CSE&BS","ECE","EEE","MECH","CIVIL");
        yearBox.getItems().addAll("I-I","I-II","II-I","II-II","III-I","III-II","IV-I","IV-II");
        sectionBox.getItems().addAll("A","B","C","D","E","F");

        historyTable.setItems(sessionList);
        feedbackTable.setItems(feedbackList);
    }

    @FXML private void showStartSession() {
        sessionPane.setVisible(true);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);
    }

    @FXML private void showHistory() {
        sessionPane.setVisible(false);
        historyTable.setVisible(true);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(false);
        loadHistory();
    }

    @FXML private void showFeedback() {
        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(true);
        summaryBox.setVisible(false);
        loadFeedback();
    }

    @FXML private void showSummary() {
        sessionPane.setVisible(false);
        historyTable.setVisible(false);
        feedbackTable.setVisible(false);
        summaryBox.setVisible(true);
        loadSummary();
    }

    @FXML private void saveSession(){

        if(topicField.getText().isEmpty() || departmentBox.getValue()==null || yearBox.getValue()==null ||
           sectionBox.getValue()==null || datePicker.getValue()==null || startTimeField.getText().isEmpty() || endTimeField.getText().isEmpty()){
            showAlert(AlertType.WARNING,"Missing Data","Please fill all fields.");
            return;
        }

        try(Connection con = getConnection()){

            if(con == null) return;

            try(PreparedStatement ps = con.prepareStatement(
                "INSERT INTO FacultyTable " +
                "SELECT name, ?, ?, ?, ?, ?, ?, ? FROM faculty_login WHERE username=?"
            )) {

                ps.setString(1, topicField.getText());
                ps.setString(2, departmentBox.getValue());
                ps.setString(3, yearBox.getValue());
                ps.setString(4, sectionBox.getValue());
                ps.setDate(5, java.sql.Date.valueOf(datePicker.getValue()));
                ps.setString(6, startTimeField.getText());
                ps.setString(7, endTimeField.getText());
                ps.setString(8, LoginController.loggedInFaculty);

                ps.executeUpdate();
            }

            showAlert(AlertType.INFORMATION,"Success","Session saved");
            clearFields();

        }catch(Exception e){
            showAlert(AlertType.ERROR,"Error","Failed to save session");
        }
    }

    private void loadHistory(){

        sessionList.clear();

        try(Connection con = getConnection()){

            if(con == null) return;

            try(PreparedStatement ps = con.prepareStatement(
                "SELECT SessionTopic, Department, Year, Section, SessionDate, StartTime, EndTime FROM FacultyTable WHERE FacultyName=?"
            )) {

                ps.setString(1, getFacultyName(LoginController.loggedInFaculty));

                try(ResultSet rs = ps.executeQuery()) {

                    while(rs.next()){
                        Map<String,String> row = new LinkedHashMap<>();

                        row.put("Topic", rs.getString("SessionTopic"));
                        row.put("Department", rs.getString("Department"));
                        row.put("Year", rs.getString("Year"));
                        row.put("Section", rs.getString("Section"));
                        row.put("Date", rs.getDate("SessionDate").toString());
                        row.put("Duration", rs.getString("StartTime")+" - "+rs.getString("EndTime"));

                        sessionList.add(row);
                    }
                }
            }

            updateTable(historyTable, sessionList);

        }catch(Exception e){
            showAlert(AlertType.ERROR,"Error","Failed to load history");
        }
    }

    private void loadFeedback(){

        feedbackList.clear();

        try(Connection con = getConnection()){

            if(con == null) return;

            try(PreparedStatement ps = con.prepareStatement(
                "SELECT SessionTopic, Rating, Feedback FROM StudentFeedback WHERE FacultyName=?"
            )) {

                ps.setString(1, getFacultyName(LoginController.loggedInFaculty));

                try(ResultSet rs = ps.executeQuery()) {

                    while(rs.next()){
                        Map<String,String> row = new LinkedHashMap<>();

                        row.put("Topic", rs.getString("SessionTopic"));
                        row.put("Rating", rs.getString("Rating"));
                        row.put("Feedback", rs.getString("Feedback"));

                        feedbackList.add(row);
                    }
                }
            }

            updateTable(feedbackTable, feedbackList);

        }catch(Exception e){
            showAlert(AlertType.ERROR,"Error","Failed to load feedback");
        }
    }

    private void loadSummary(){

        try(Connection con = getConnection()){

            if(con == null) return;

            String facultyName = getFacultyName(LoginController.loggedInFaculty);

            int totalSessions = 0;

            try(PreparedStatement ps1 = con.prepareStatement(
                "SELECT COUNT(*) FROM FacultyTable WHERE FacultyName=?"
            )) {
                ps1.setString(1, facultyName);
                try(ResultSet rs1 = ps1.executeQuery()){
                    if(rs1.next()) totalSessions = rs1.getInt(1);
                }
            }

            List<String> depts = new ArrayList<>();

            try(PreparedStatement ps2 = con.prepareStatement(
                "SELECT DISTINCT Department FROM FacultyTable WHERE FacultyName=?"
            )) {
                ps2.setString(1, facultyName);
                try(ResultSet rs2 = ps2.executeQuery()){
                    while(rs2.next()){
                        depts.add(rs2.getString("Department"));
                    }
                }
            }

            summaryBox.getChildren().clear();

            summaryBox.getChildren().addAll(
                new Label("Faculty: " + facultyName),
                new Label("Total Sessions: " + totalSessions),
                new Label("Departments Covered: " + String.join(", ", depts))
            );

        }catch(Exception e){
            showAlert(AlertType.ERROR,"Error","Failed to load summary");
        }
    }

    private String getFacultyName(String username){
        try(Connection con = getConnection()){

            if(con == null) return username;

            try(PreparedStatement ps = con.prepareStatement(
                "SELECT name FROM faculty_login WHERE username=?"
            )) {
                ps.setString(1, username);
                try(ResultSet rs = ps.executeQuery()){
                    if(rs.next()) return rs.getString("name");
                }
            }

        }catch(Exception e){
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