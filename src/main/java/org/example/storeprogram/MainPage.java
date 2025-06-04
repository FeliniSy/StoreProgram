package org.example.storeprogram;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MainPage {
    private static TextArea globalResultArea;
    public static double totalCost;

    //Product List
    private static List<org.example.storeprogram.CartItems> cartItems = new ArrayList<>();

    public static BorderPane mainPage() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(20));

        HBox topPanel = topPanelSection();

        VBox resultPanel = createResultPanel();
        VBox controlPanel = createControlPanel();

        HBox contentArea = new HBox(20);
        contentArea.getChildren().addAll(controlPanel, resultPanel);
        contentArea.setAlignment(Pos.CENTER);

        pane.setTop(topPanel);
        pane.setCenter(contentArea);
        pane.setFocusTraversable(true);
        return pane;
    }

    private static HBox topPanelSection(){
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(0,0,20,0));
        topPanel.setAlignment(Pos.CENTER_RIGHT);

        Button logout = new Button("Log out");
        logout.setPrefWidth(100);
        logout.getStyleClass().add("button");
        logout.setOnAction(e -> {
            org.example.storeprogram.HelloApplication.mainLayout.setCenter(org.example.storeprogram.LoginClass.loginForm());
        });

        topPanel.getChildren().addAll(logout);
        return topPanel;
    }

    private static VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(300);

        Label title = new Label("Add Product");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("title");

        TextField barcodeField = new TextField();
        barcodeField.setPromptText("Enter Barcode");
        barcodeField.getStyleClass().add("text-field");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter Amount");
        amountField.getStyleClass().add("text-field");

        TextField saleField = new TextField();
        saleField.setPromptText("Sale Percentage (optional)");
        saleField.getStyleClass().add("text-field");

        Button addButton = new Button("ADD");
        addButton.setPrefWidth(100);
        addButton.getStyleClass().add("button");

        Button deleteButton = new Button("DELETE");
        deleteButton.setPrefWidth(100);
        deleteButton.getStyleClass().add("button");

        Button transactionButton = new Button("TRANSACTION");
        transactionButton.setPrefWidth(100);
        transactionButton.getStyleClass().add("button");

        addButton.setOnAction(e -> {
            addProduct(barcodeField, amountField, saleField, globalResultArea);
        });

        deleteButton.setOnAction(e -> {
            clearLastEntry(globalResultArea);
        });

        transactionButton.setOnAction(e -> {
            showTransactionDialog();
        });

        panel.getChildren().addAll(
                title,
                new Separator(),
                barcodeField,
                amountField,
                saleField,
                addButton,
                deleteButton,
                transactionButton
        );
        panel.getStyleClass().add("control-panel");

        return panel;
    }

    private static VBox createResultPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(400);

        Label title = new Label("Shopping Cart");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("title");

        globalResultArea = new TextArea();
        globalResultArea.setEditable(false);
        globalResultArea.setPrefRowCount(30);
        globalResultArea.setPromptText("პროდუქტები დაემატება აქ...");
        globalResultArea.setPrefHeight(2000);
        globalResultArea.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(title, new Separator(), globalResultArea);
        panel.getStyleClass().add("result-panel");
        globalResultArea.getStyleClass().add("shopping-cart");

        return panel;
    }

    private static void addProduct(TextField barcodeField, TextField amountField, TextField saleField, TextArea resultArea) {
        String barcode = barcodeField.getText().trim();
        String amountText = amountField.getText().trim();
        String saleText = saleField.getText().trim();

        if (barcode.isEmpty() || amountText.isEmpty()) {
            showAlert("Error", "გთხოვთ შეიყვანოთ კოდი ან რაოდენობა.");
            return;
        }

        int amount = Integer.parseInt(amountText);

        try (Connection conn = org.example.storeprogram.forSQL.getConnection();
             Statement stmt = conn.createStatement()) {

            // find the product with barcode
            String query = "SELECT * FROM product WHERE barcode = '" + barcode + "'";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String productName = rs.getString("productName");
                int sellPrice = rs.getInt("sellprice");
                int availableAmount = rs.getInt("amount");

                // Check if enough stock is available
                if (availableAmount < amount) {
                    showAlert("Insufficient Stock", "მხოლოდ " + availableAmount + " არის მარაგში დარჩენილი " + productName);
                    return;
                }

                double finalPrice = sellPrice;
                String priceInfo = "";

                // Saled product
                if (!saleText.isEmpty()) {
                    try {
                        int salePercentage = Integer.parseInt(saleText);
                        finalPrice = sellPrice * (1 - salePercentage / 100.0);
                        priceInfo = " (Sale: " + salePercentage + "% off)";
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Sale", "Please enter a valid sale percentage.");
                        return;
                    }
                }

                totalCost = finalPrice * amount;
                int id = rs.getInt("productID");

                // Add product to the result area
                resultArea.appendText("Product: " + productName
                        + " | Barcode: " + barcode
                        + " | Quantity: " + amount
                        + " | Unit Price: $" + String.format("%.2f", finalPrice)
                        + priceInfo
                        + " | Total: $" + String.format("%.2f", totalCost) + "\n");

                // Add item to cart list
                cartItems.add(new org.example.storeprogram.CartItems(id,barcode, productName, amount,finalPrice));

                // Clear input fields after addition
                barcodeField.clear();
                amountField.clear();
                saleField.clear();

            } else {
                showAlert("Product Not Found", "ამ შტრიხკოდით პროდუქტი ვერ მოიძებნა: " + barcode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while accessing the database: " + e.getMessage());
        }
    }

    private static void clearLastEntry(TextArea resultArea) {
        String text = resultArea.getText();
        if (text.isEmpty()) {
            showAlert("Info", "გრაფა ცარიელია");
            return;
        }

        // Remove the last item
        if (!cartItems.isEmpty()) {
            cartItems.remove(cartItems.size() - 1);
        }

        int lastNewline = text.lastIndexOf('\n');
        if (lastNewline > 0) {
            resultArea.setText(text.substring(0, lastNewline));
        } else {
            resultArea.clear();
        }
    }

    private static void showTransactionDialog(){
        if (globalResultArea.getText().isEmpty()) {
            showAlert("Empty Cart", "გთხოვთ დაამატოთ პროდუქცია სანამ გადაიხდით.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("გადახდის მეთოდი");
        dialog.setResizable(false);

        VBox dialogPane = new VBox(20);
        dialogPane.setPadding(new Insets(30));
        dialogPane.setAlignment(Pos.CENTER);

        Label title = new Label("აირჩიეთ გადახდის მეთოდი");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("title");

        Button cashButton = new Button("Cash Payment");
        cashButton.setPrefWidth(120);
        cashButton.getStyleClass().add("button");
        Button cardButton = new Button("Card Payment");
        cardButton.setPrefWidth(120);
        cardButton.getStyleClass().add("button");


        cashButton.setOnAction(e -> {
            Stage cashier = new Stage();
            cashier.initModality(Modality.APPLICATION_MODAL);
            cashier.setTitle("Cashier");

            VBox cashierPane = new VBox(15);
            cashierPane.setPadding(new Insets(20));
            cashierPane.setAlignment(Pos.CENTER);

            String paidcash = String.format("%.2f", totalCost);
            Label cost = new Label("Total Cost: $" + paidcash);
            cost.getStyleClass().add("title");

            TextField customercash = new TextField();
            customercash.setPromptText("მომხმარებლის თანხა");
            customercash.setPrefWidth(200);
            customercash.getStyleClass().add("text-field");

            Button completePaymentButton = new Button("Complete Payment");
            completePaymentButton.setPrefWidth(120);
            completePaymentButton.getStyleClass().add("button");


            completePaymentButton.setOnAction(ee -> {
                try {
                    double customerCashAmount = Double.parseDouble(customercash.getText());

                    if (customerCashAmount >= totalCost) {
                        if (updateDatabaseQuantities()) {
                            double changeTogive = customerCashAmount - totalCost;
                            showAlert("Payment Successful", "გადახდილია!\nDatabase updated." + (changeTogive > 0 ? "\nChange: $" + changeTogive : "") );
                            globalResultArea.clear();
                            cartItems.clear();
                            cashier.close();
                            dialog.close();
                        } else {
                            showAlert("Payment Failed", "Failed to update database. Please try again.");
                        }
                    } else {
                        showAlert("Insufficient Amount", "თანხა არასაკმარისია");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter a valid number");
                }
            });

            cashierPane.getChildren().addAll(cost, customercash, completePaymentButton);

            Scene cashierScene = new Scene(cashierPane, 300, 250);
            cashier.setScene(cashierScene);
            cashier.showAndWait();
        });

        cardButton.setOnAction(e -> {
            if (updateDatabaseQuantities()) {
                showAlert("Payment Successful", "გადახდილია!\nDatabase updated.");
                globalResultArea.clear(); // Clear cart after payment
                cartItems.clear(); // Clear cart items list
            } else {
                showAlert("Payment Failed", "Failed to update database. Please try again.");
            }
            dialog.close();
        });

        dialogPane.getChildren().addAll(title, cashButton, cardButton);

        Scene scene = new Scene(dialogPane, 300, 200);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static boolean updateDatabaseQuantities() {
        if (cartItems.isEmpty()) {
            return true; // არაფერი განახლდა
        }

        try (Connection conn = org.example.storeprogram.forSQL.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            String updateQuery = "UPDATE product SET amount = amount - ? WHERE barcode = ?";
            String insertIntoSold = "INSERT INTO sold(useri, productID, amount, soldPrice) VALUES(?, ?, ?, ?)";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                 PreparedStatement soldStmt = conn.prepareStatement(insertIntoSold)) {

                for (org.example.storeprogram.CartItems item : cartItems) {
                    // First check if we have enough stock
                    String checkQuery = "SELECT amount FROM product WHERE barcode = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setString(1, item.barcode);
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next()) {
                            int currentAmount = rs.getInt("amount");

                            // Check if we have sufficient stock before updating
                            if (currentAmount < item.quantity) {
                                conn.rollback();
                                System.err.println("Insufficient stock for product: " + item.productName +
                                        ". Available: " + currentAmount + ", Required: " + item.quantity);
                                return false;
                            }
                        } else {
                            conn.rollback();
                            System.err.println("Product not found with barcode: " + item.barcode);
                            return false;
                        }
                    }

                    // Update product quantity
                    updateStmt.setInt(1, item.quantity);
                    updateStmt.setString(2, item.barcode);
                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected == 0) {
                        conn.rollback();
                        System.err.println("Failed to update product with barcode: " + item.barcode);
                        return false;
                    }

                    // Insert into sold table
                    soldStmt.setInt(1, org.example.storeprogram.HelloApplication.userId);
                    soldStmt.setInt(2, item.ID);
                    soldStmt.setInt(3, item.quantity);
                    soldStmt.setDouble(4, item.price);

                    int soldRowsAffected = soldStmt.executeUpdate();

                    if (soldRowsAffected == 0) {
                        conn.rollback();
                        System.err.println("Failed to insert sold record for product: " + item.barcode);
                        return false;
                    }

                    System.out.println("Successfully sold: " + item.productName + " (Quantity: " + item.quantity + ")");
                }

                // წარმატებით დასრულდა ტრანზაქცია
                conn.commit();
                System.out.println("Transaction completed successfully!");
                return true;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}