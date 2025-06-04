package org.example.storeprogram;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginClass {
    public static GridPane loginForm() {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setVgap(10);
        pane.setHgap(10);


        // Username field
        TextField username = new TextField();
        username.setPromptText("Username");
        pane.add(username, 0, 0);
        username.getStyleClass().add("text-field");

        // Password field
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        pane.add(password, 0, 1);
        password.getStyleClass().add("text-field");


        // Login button
        Button loginButton = new Button("Login");
        pane.add(loginButton, 0, 3);
        loginButton.setDefaultButton(true);
        loginButton.getStyleClass().add("button");

        //go to admin panel
        KeyCombination adminPanel = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN,KeyCombination.SHIFT_DOWN,
                KeyCombination.ALT_DOWN);
        pane.setOnKeyPressed(e -> {
            if (adminPanel.match(e)){
                org.example.storeprogram.AdminPanel.openAdminPanel();
                e.consume();
            }
        });

        pane.setFocusTraversable(true);

        loginButton.setOnAction(e -> {
            String userInput = username.getText().trim();
            String passInput = password.getText().trim();

            if (userInput.isEmpty() || passInput.isEmpty()) {
                showError("Please fill in both username and password.");
                return;
            }

            try (Connection con = org.example.storeprogram.forSQL.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("SELECT userID,username,pasword FROM users WHERE username = ? AND pasword = ?")) {

                pstmt.setString(1, userInput);
                pstmt.setString(2, passInput);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // Successfully logged in
                        org.example.storeprogram.HelloApplication.userId = rs.getInt("userID");
                        org.example.storeprogram.HelloApplication.mainLayout.setTop(null);
                        org.example.storeprogram.HelloApplication.mainLayout.setCenter(MainPage.mainPage());

                    } else {
                        showError("Invalid username or password.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("An error occurred: " + ex.getMessage());
            }
        });


        return pane;
    }


    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
