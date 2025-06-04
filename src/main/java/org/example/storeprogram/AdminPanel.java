package org.example.storeprogram;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminPanel {
    private static BorderPane mainPane;

    public static void openAdminPanel() {
        mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        mainPane.getStyleClass().add("admin-panel");

        // log out section
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(10));
        topPanel.setAlignment(Pos.CENTER);

        Button addSectionBtn = new Button("Add Product Section");
        Button viewProductsBtn = new Button("View Products");
        Button backBtn = new Button("Back to Main");

        addSectionBtn.setOnAction(e -> {
            mainPane.setCenter(addProductSection());
        });

        viewProductsBtn.setOnAction(e -> {
            mainPane.setCenter(viewProductSection());
        });

        backBtn.setOnAction(e -> {
            mainPane.setCenter(createWelcomeSection());
        });

        topPanel.getChildren().addAll(addSectionBtn, viewProductsBtn, backBtn);
        mainPane.setTop(topPanel);

        // Set initial welcome section
        mainPane.setCenter(createWelcomeSection());

        Stage stage = new Stage();
        Scene scene = new Scene(mainPane, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Admin Panel");
        stage.show();
    }

    private static VBox createWelcomeSection() {
        VBox welcomePane = new VBox(20);
        welcomePane.setAlignment(Pos.CENTER);
        welcomePane.setPadding(new Insets(50));

        Label welcomeLabel = new Label("Welcome to Admin Panel");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label instructionLabel = new Label("Select an option from the top menu to get started");
        instructionLabel.setStyle("-fx-font-size: 14px;");

        welcomePane.getChildren().addAll(welcomeLabel, instructionLabel);
        return welcomePane;
    }

    private static ScrollPane viewProductSection() {
        ScrollPane pane = new ScrollPane();
        pane.setFitToWidth(true);
        pane.setPadding(new Insets(20));

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("View Products");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setPrefHeight(300);

        Button viewAllBtn = new Button("View All Products");
        viewAllBtn.getStyleClass().add("button");
        viewAllBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        viewAllBtn.setOnAction(e -> {
            try (Connection con = forSQL.getConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM product")) {

                textArea.clear();

                // Get column count to display all columns
                int columnCount = rs.getMetaData().getColumnCount();

                // Add header with column names
                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    header.append(rs.getMetaData().getColumnName(i));
                    if (i < columnCount) {
                        header.append(" | ");
                    }
                }
                textArea.appendText(header.toString() + "\n");
                textArea.appendText("-".repeat(header.length()) + "\n");

                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        row.append(value != null ? value : "NULL");
                        if (i < columnCount) {
                            row.append(" | ");
                        }
                    }
                    textArea.appendText(row.toString() + "\n");
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                showAlert("Error", "Failed to load products: " + exp.getMessage());
            }
        });


        Button viewAllsoldBtn = new Button("View All Sold Products");
        viewAllsoldBtn.getStyleClass().add("button");
        viewAllsoldBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        viewAllsoldBtn.setOnAction(e -> {
            try (Connection con = forSQL.getConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM sold")) {

                textArea.clear();

                // Get column count to display all columns
                int columnCount = rs.getMetaData().getColumnCount();

                // Add header with column names
                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    header.append(rs.getMetaData().getColumnName(i));
                    if (i < columnCount) {
                        header.append(" | ");
                    }
                }
                textArea.appendText(header.toString() + "\n");
                textArea.appendText("-".repeat(header.length()) + "\n");

                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        row.append(value != null ? value : "NULL");
                        if (i < columnCount) {
                            row.append(" | ");
                        }
                    }
                    textArea.appendText(row.toString() + "\n");
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                showAlert("Error", "Failed to load products: " + exp.getMessage());
            }

        });

        Label selection = new Label("Search Products by: ");
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Search by ID", "Search by Name", "Search by Category","Sold Items");
        comboBox.setValue("Search by ID");


        TextField searchField = new TextField();
        searchField.setPromptText("Enter search term");

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("button");
        searchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        searchBtn.setOnAction(e -> {
            String selectedOption = comboBox.getValue();

            String query = "";
            if (selectedOption.equals("Search by ID")) {
                int input = Integer.parseInt(searchField.getText());
                query = "SELECT * FROM product WHERE productID = '" + input + "'";
            } else if (selectedOption.equals("Search by Name")) {
                String input = searchField.getText().trim();
                query = "SELECT * FROM product WHERE productName LIKE '%" + input + "%'";
            } else if (selectedOption.equals("Search by Category")) {
                int input = Integer.parseInt(searchField.getText());
                query = "SELECT * FROM product WHERE categoryID = '" + input + "'";
            } else if (selectedOption.equals("Sold Items")) {
                int input = Integer.parseInt(searchField.getText());
                query = "Select solddate,sold.amount, product.price,useri from sold inner join product on sold.productID = product.productID where sold.productID = '" + input + "'";
            }

            try (Connection con = forSQL.getConnection();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                textArea.clear();
                boolean found = false;

                // Get column count to display all columns
                int columnCount = rs.getMetaData().getColumnCount();

                // Add header with column names
                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    header.append(rs.getMetaData().getColumnName(i));
                    if (i < columnCount) {
                        header.append(" | ");
                    }
                }
                textArea.appendText(header.toString() + "\n");
                textArea.appendText("-".repeat(header.length()) + "\n");

                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        row.append(value != null ? value : "NULL");
                        if (i < columnCount) {
                            row.append(" | ");
                        }
                    }
                    textArea.appendText(row.toString() + "\n");
                    found = true;
                }

                if (!found) {
                    textArea.setText("No products found matching your search criteria.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Search failed: " + ex.getMessage());
            }
        });

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.getChildren().addAll(selection, comboBox, searchField, searchBtn);

        mainContainer.getChildren().addAll(title, new Separator(), viewAllBtn, viewAllsoldBtn, new Separator(),
                searchBox, textArea);

        pane.setContent(mainContainer);
        return pane;
    }

    private static ScrollPane addProductSection() {
        // ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(20));

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // Title
        Label titleLabel = new Label("Add New Product");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create form grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));

        // Create text fields with labels
        TextField nameField = new TextField();
        nameField.setPromptText("Enter product name");
        nameField.setPrefWidth(200);

        TextField amountField = new TextField();
        amountField.setPromptText("Enter quantity");
        amountField.setPrefWidth(200);

        TextField priceField = new TextField();
        priceField.setPromptText("Enter purchase price");
        priceField.setPrefWidth(200);

        TextField amountSizeField = new TextField();
        amountSizeField.setPromptText("Enter amount size ID (optional)");
        amountSizeField.setPrefWidth(200);

        TextField categoryField = new TextField();
        categoryField.setPromptText("Enter category ID (optional)");
        categoryField.setPrefWidth(200);

        TextField deriverDateField = new TextField();
        deriverDateField.setPromptText("Enter delivery date (YYYY-MM-DD, optional)");
        deriverDateField.setPrefWidth(200);

        TextField fromwhoField = new TextField();
        fromwhoField.setPromptText("Enter supplier (optional)");
        fromwhoField.setPrefWidth(200);

        TextField branchIDField = new TextField();
        branchIDField.setPromptText("Enter branch ID (optional)");
        branchIDField.setPrefWidth(200);

        TextField sellPriceField = new TextField();
        sellPriceField.setPromptText("Enter selling price");
        sellPriceField.setPrefWidth(200);


        TextField barCodeField = new TextField();
        barCodeField.setPromptText("Enter barcode");
        barCodeField.setPrefWidth(200);

        // Add labels and fields to grid
        formGrid.add(new Label("Product Name:"), 0, 0);
        formGrid.add(nameField, 1, 0);

        formGrid.add(new Label("Quantity:"), 0, 1);
        formGrid.add(amountField, 1, 1);

        formGrid.add(new Label("Purchase Price:"), 0, 2);
        formGrid.add(priceField, 1, 2);

        formGrid.add(new Label("Amount Size ID:"), 0, 3);
        formGrid.add(amountSizeField, 1, 3);

        formGrid.add(new Label("Category ID:"), 0, 4);
        formGrid.add(categoryField, 1, 4);

        formGrid.add(new Label("Delivery Date:"), 0, 5);
        formGrid.add(deriverDateField, 1, 5);

        formGrid.add(new Label("Supplier:"), 0, 6);
        formGrid.add(fromwhoField, 1, 6);

        formGrid.add(new Label("Branch ID:"), 0, 7);
        formGrid.add(branchIDField, 1, 7);

        formGrid.add(new Label("Selling Price:"), 0, 8);
        formGrid.add(sellPriceField, 1, 8);

        formGrid.add(new Label("Barcode:"), 0, 9);
        formGrid.add(barCodeField, 1, 9);

        // Create buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        Button addProductButton = new Button("Add Product");
        addProductButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addProductButton.setPrefWidth(120);

        Button clearButton = new Button("Clear Fields");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        clearButton.setPrefWidth(120);

        buttonBox.getChildren().addAll(addProductButton, clearButton);

        // Add action for clear button
        clearButton.setOnAction(e -> {
            nameField.clear();
            amountField.clear();
            priceField.clear();
            amountSizeField.clear();
            categoryField.clear();
            deriverDateField.clear();
            fromwhoField.clear();
            branchIDField.clear();
            sellPriceField.clear();
            barCodeField.clear();
        });

        String query = "INSERT INTO product(productName,amount,price,amountsizeID,categoryID,deriverDate,fromwho,branchID,sellprice,barcode) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?)";

        addProductButton.setOnAction(e -> {
            try {
                // Validate required fields
                if (nameField.getText().trim().isEmpty() ||
                        amountField.getText().trim().isEmpty() ||
                        priceField.getText().trim().isEmpty() ||
                        sellPriceField.getText().trim().isEmpty() ||
                        barCodeField.getText().trim().isEmpty()) {

                    showAlert("Input Error", "Please fill in all required fields (Name, Amount, Price, Sell Price, Barcode)");
                    return;
                }

                // Parse input fields
                String name = nameField.getText().trim();
                int amount = Integer.parseInt(amountField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                double sellPrice = Double.parseDouble(sellPriceField.getText().trim());
                String barCode = barCodeField.getText().trim();

                // Optional fields - handle empty values
                Integer amountSize = amountSizeField.getText().trim().isEmpty() ? null : Integer.parseInt(amountSizeField.getText().trim());
                Integer category = categoryField.getText().trim().isEmpty() ? null : Integer.parseInt(categoryField.getText().trim());
                Integer branchID = branchIDField.getText().trim().isEmpty() ? null : Integer.parseInt(branchIDField.getText().trim());
                java.sql.Date deriverDate = deriverDateField.getText().trim().isEmpty() ? null : java.sql.Date.valueOf(deriverDateField.getText().trim());
                String fromwho = fromwhoField.getText().trim().isEmpty() ? null : fromwhoField.getText().trim();

                try (Connection con = forSQL.getConnection();
                     PreparedStatement stmt = con.prepareStatement(query)) {

                    // Bind parameters to the query
                    stmt.setString(1, name);
                    stmt.setInt(2, amount);
                    stmt.setDouble(3, price);

                    if (amountSize != null) {
                        stmt.setInt(4, amountSize);
                    } else {
                        stmt.setNull(4, java.sql.Types.INTEGER);
                    }

                    if (category != null) {
                        stmt.setInt(5, category);
                    } else {
                        stmt.setNull(5, java.sql.Types.INTEGER);
                    }

                    if (deriverDate != null) {
                        stmt.setDate(6, deriverDate);
                    } else {
                        stmt.setNull(6, java.sql.Types.DATE);
                    }

                    if (fromwho != null) {
                        stmt.setString(7, fromwho);
                    } else {
                        stmt.setNull(7, java.sql.Types.VARCHAR);
                    }

                    if (branchID != null) {
                        stmt.setInt(8, branchID);
                    } else {
                        stmt.setNull(8, java.sql.Types.INTEGER);
                    }

                    stmt.setDouble(9, sellPrice);
                    stmt.setString(10, barCode);

                    // Execute the query
                    int rowsInserted = stmt.executeUpdate();

                    if (rowsInserted > 0) {
                        showAlert("Success", "Product '" + name + "' added successfully!");
                        // Clear fields after successful addition
                        clearButton.fire();
                    } else {
                        showAlert("Error", "Failed to add the product. Please try again.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Database Error", "An error occurred while adding the product: " + ex.getMessage());
                }

            } catch (NumberFormatException exc) {
                showAlert("Input Error", "Please ensure all numeric fields contain valid numbers.");
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "An unexpected error occurred: " + ex.getMessage());
            }
        });
        nameField.getStyleClass().add("text-field");
        amountField.getStyleClass().add("text-field");
        priceField.getStyleClass().add("text-field");
        amountSizeField.getStyleClass().add("text-field");
        categoryField.getStyleClass().add("text-field");
        deriverDateField.getStyleClass().add("text-field");
        fromwhoField.getStyleClass().add("text-field");
        branchIDField.getStyleClass().add("text-field");
        sellPriceField.getStyleClass().add("text-field");
        barCodeField.getStyleClass().add("text-field");

        // Add all components to main container
        mainContainer.getChildren().addAll(titleLabel, new Separator(), formGrid, buttonBox);

        scrollPane.setContent(mainContainer);
        return scrollPane;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}