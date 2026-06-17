package com.javafx.javafx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.LocalDateStringConverter;
import javafx.scene.input.KeyCode;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Launcher extends Application {

    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private TableView<Expense> tableView;
    private Label totalLabel;
    private Label budgetLabel;
    private PieChart pieChart;
    private ComboBox<String> categoryCombo;
    private double monthlyBudget = 2000.0;

    private static final String DATA_FILE = "expenses.dat";
    private static final String[] CATEGORIES = {
            "Food & Dining", "Transportation", "Shopping", "Entertainment",
            "Bills & Utilities", "Healthcare", "Education", "Travel", "Other"
    };

    private static final Map<String, Color> CATEGORY_COLORS = Map.of(
            "Food & Dining", Color.web("#FF6B6B"),
            "Transportation", Color.web("#4ECDC4"),
            "Shopping", Color.web("#45B7D1"),
            "Entertainment", Color.web("#96CEB4"),
            "Bills & Utilities", Color.web("#FFEAA7"),
            "Healthcare", Color.web("#DDA0DD"),
            "Education", Color.web("#98D8C8"),
            "Travel", Color.web("#F7DC6F"),
            "Other", Color.web("#BB8FCE")
    );

    @Override
    public void start(Stage primaryStage) {
        loadData();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f6fa;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Center - Split pane with form and table
        SplitPane centerPane = new SplitPane();
        centerPane.setDividerPositions(0.35);

        // Left - Input Form & Stats
        VBox leftPanel = createLeftPanel();

        // Right - Table & Chart
        VBox rightPanel = createRightPanel();

        centerPane.getItems().addAll(leftPanel, rightPanel);
        root.setCenter(centerPane);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/style.css") != null ?
                getClass().getResource("/style.css").toExternalForm() : "");

        primaryStage.setTitle("Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        updateUI();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2c3e50; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 2);");

        Label title = new Label("💰 Expense Tracker");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Budget controls
        HBox budgetBox = new HBox(10);
        budgetBox.setAlignment(Pos.CENTER);

        budgetLabel = new Label();
        budgetLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        budgetLabel.setTextFill(Color.web("#ecf0f1"));

        Button setBudgetBtn = new Button("Set Budget");
        setBudgetBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;");
        setBudgetBtn.setOnAction(e -> showBudgetDialog());

        budgetBox.getChildren().addAll(budgetLabel, setBudgetBtn);

        header.getChildren().addAll(title, spacer, budgetBox);
        return header;
    }

    private VBox createLeftPanel() {
        VBox left = new VBox(20);
        left.setPadding(new Insets(20));
        left.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Input Form
        Label formTitle = new Label("Add New Expense");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        formTitle.setTextFill(Color.web("#2c3e50"));

        GridPane form = new GridPane();
        form.setVgap(12);
        form.setHgap(10);
        form.setAlignment(Pos.CENTER);

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        TextField descField = new TextField();
        descField.setPromptText("What did you spend on?");
        descField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        GridPane.setConstraints(descLabel, 0, 0);
        GridPane.setConstraints(descField, 1, 0);

        // Amount
        Label amountLabel = new Label("Amount ($):");
        amountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        TextField amountField = new TextField();
        amountField.setPromptText("0.00");
        amountField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");
        GridPane.setConstraints(amountLabel, 0, 1);
        GridPane.setConstraints(amountField, 1, 1);

        // Category
        Label catLabel = new Label("Category:");
        catLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        categoryCombo = new ComboBox<>(FXCollections.observableArrayList(CATEGORIES));
        categoryCombo.setPromptText("Select category");
        categoryCombo.setStyle("-fx-padding: 8; -fx-background-radius: 5;");
        categoryCombo.setPrefWidth(200);
        GridPane.setConstraints(catLabel, 0, 2);
        GridPane.setConstraints(categoryCombo, 1, 2);

        // Date
        Label dateLabel = new Label("Date:");
        dateLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-padding: 8;");
        datePicker.setPrefWidth(200);
        GridPane.setConstraints(dateLabel, 0, 3);
        GridPane.setConstraints(datePicker, 1, 3);

        // Buttons
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER);

        Button addBtn = new Button("➕ Add Expense");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            if (validateAndAdd(descField, amountField, categoryCombo, datePicker)) {
                clearForm(descField, amountField, categoryCombo, datePicker);
            }
        });

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 5; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> clearForm(descField, amountField, categoryCombo, datePicker));

        btnBox.getChildren().addAll(addBtn, clearBtn);
        GridPane.setConstraints(btnBox, 0, 4, 2, 1);

        form.getChildren().addAll(descLabel, descField, amountLabel, amountField, catLabel, categoryCombo, dateLabel, datePicker, btnBox);

        // Quick Stats Cards
        HBox statsBox = new HBox(10);
        statsBox.setAlignment(Pos.CENTER);

        VBox totalCard = createStatCard("Total Expenses", "0.00", "#e74c3c");
        totalCard.setId("totalCard");

        VBox countCard = createStatCard("Total Items", "0", "#3498db");
        countCard.setId("countCard");

        statsBox.getChildren().addAll(totalCard, countCard);

        // Pie Chart
        Label chartTitle = new Label("Spending by Category");
        chartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        chartTitle.setTextFill(Color.web("#2c3e50"));

        pieChart = new PieChart();
        pieChart.setPrefHeight(300);
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);

        left.getChildren().addAll(formTitle, form, statsBox, chartTitle, pieChart);
        return left;
    }

    private VBox createRightPanel() {
        VBox right = new VBox(15);
        right.setPadding(new Insets(20));
        right.setStyle("-fx-background-color: #f5f6fa;");

        // Search and Filter
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search expenses...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-padding: 10; -fx-background-radius: 20; -fx-border-color: #ddd; -fx-border-radius: 20;");

        ComboBox<String> filterCombo = new ComboBox<>(FXCollections.observableArrayList("All Categories"));
        filterCombo.getItems().addAll(CATEGORIES);
        filterCombo.setPromptText("Filter by Category");
        filterCombo.setStyle("-fx-padding: 8;");

        Button searchBtn = new Button("Search");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;");

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 5;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("🗑 Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;");
        deleteBtn.setOnAction(e -> deleteSelected());

        Button exportBtn = new Button("📥 Export CSV");
        exportBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5;");
        exportBtn.setOnAction(e -> exportToCSV());

        filterBox.getChildren().addAll(searchField, filterCombo, searchBtn, resetBtn, spacer, deleteBtn, exportBtn);

        // Table
        tableView = new TableView<>();
        tableView.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(120);
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                    setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(150);
        catCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    Color color = CATEGORY_COLORS.getOrDefault(item, Color.GRAY);
                    Label dot = new Label("●");
                    dot.setTextFill(color);
                    dot.setStyle("-fx-font-size: 16;");
                    setGraphic(dot);
                    setStyle("-fx-alignment: CENTER_LEFT;");
                }
            }
        });

        TableColumn<Expense, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });

        tableView.getColumns().addAll(descCol, amountCol, catCol, dateCol);
        tableView.setItems(expenses);

        // Search functionality
        searchBtn.setOnAction(e -> {
            String search = searchField.getText().toLowerCase();
            String filter = filterCombo.getValue();

            ObservableList<Expense> filtered = expenses.stream()
                    .filter(exp -> {
                        boolean matchesSearch = search.isEmpty() ||
                                exp.getDescription().toLowerCase().contains(search) ||
                                exp.getCategory().toLowerCase().contains(search);
                        boolean matchesCategory = filter == null || filter.equals("All Categories") ||
                                exp.getCategory().equals(filter);
                        return matchesSearch && matchesCategory;
                    })
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            tableView.setItems(filtered);
        });

        resetBtn.setOnAction(e -> {
            searchField.clear();
            filterCombo.setValue("All Categories");
            tableView.setItems(expenses);
        });

        // Context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> editSelected());
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteSelected());
        contextMenu.getItems().addAll(editItem, deleteItem);
        tableView.setContextMenu(contextMenu);

        // Double click to edit
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editSelected();
            }
        });

        right.getChildren().addAll(filterBox, tableView);
        return right;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        card.setPrefWidth(150);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", 12));
        titleLabel.setTextFill(Color.WHITE);

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.WHITE);
        valueLabel.setId("valueLabel");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private boolean validateAndAdd(TextField desc, TextField amount, ComboBox<String> cat, DatePicker date) {
        String description = desc.getText().trim();
        String amountText = amount.getText().trim();
        String category = cat.getValue();
        LocalDate expenseDate = date.getValue();

        if (description.isEmpty()) {
            showAlert("Error", "Please enter a description");
            return false;
        }

        double amountValue;
        try {
            amountValue = Double.parseDouble(amountText);
            if (amountValue <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid positive amount");
            return false;
        }

        if (category == null) {
            showAlert("Error", "Please select a category");
            return false;
        }

        if (expenseDate == null) {
            showAlert("Error", "Please select a date");
            return false;
        }

        Expense expense = new Expense(description, amountValue, category, expenseDate);
        expenses.add(expense);
        saveData();
        updateUI();
        return true;
    }

    private void clearForm(TextField desc, TextField amount, ComboBox<String> cat, DatePicker date) {
        desc.clear();
        amount.clear();
        cat.setValue(null);
        date.setValue(LocalDate.now());
    }

    private void updateUI() {
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        int count = expenses.size();

        // Update stats
        totalLabel = (Label) ((VBox) ((HBox) ((VBox) ((SplitPane) ((BorderPane)
                tableView.getScene().getRoot()).getCenter()).getItems().get(0))
                .getChildren().get(2)).getChildren().get(0)).getChildren().get(1);
        totalLabel.setText(String.format("$%.2f", total));

        Label countLabel = (Label) ((VBox) ((HBox) ((VBox) ((SplitPane) ((BorderPane)
                tableView.getScene().getRoot()).getCenter()).getItems().get(0))
                .getChildren().get(2)).getChildren().get(1)).getChildren().get(1);
        countLabel.setText(String.valueOf(count));

        // Update budget label
        double remaining = monthlyBudget - total;
        String budgetText = String.format("Budget: $%.2f / $%.2f (Remaining: $%.2f)",
                total, monthlyBudget, remaining);
        budgetLabel.setText(budgetText);
        budgetLabel.setTextFill(remaining < 0 ? Color.web("#e74c3c") : Color.web("#2ecc71"));

        // Update pie chart
        updatePieChart();
    }

    private void updatePieChart() {
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryTotals.forEach((cat, amount) -> {
            PieChart.Data data = new PieChart.Data(cat + " ($" + String.format("%.2f", amount) + ")", amount);
            pieData.add(data);
        });

        pieChart.setData(pieData);

        // Color the slices
        pieData.forEach(data -> {
            data.getNode().setStyle("-fx-pie-color: " + toHex(CATEGORY_COLORS.getOrDefault(
                    data.getName().split(" \\$")[0], Color.GRAY)) + ";");
        });
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private void deleteSelected() {
        Expense selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select an expense to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Expense");
        confirm.setContentText("Are you sure you want to delete this expense?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            expenses.remove(selected);
            saveData();
            updateUI();
        }
    }

    private void editSelected() {
        Expense selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle("Edit Expense");
        dialog.setHeaderText("Edit Expense Details");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField descField = new TextField(selected.getDescription());
        TextField amountField = new TextField(String.valueOf(selected.getAmount()));
        ComboBox<String> catCombo = new ComboBox<>(FXCollections.observableArrayList(CATEGORIES));
        catCombo.setValue(selected.getCategory());
        DatePicker datePicker = new DatePicker(selected.getDate());

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(catCombo, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    return new Expense(
                            descField.getText(),
                            Double.parseDouble(amountField.getText()),
                            catCombo.getValue(),
                            datePicker.getValue()
                    );
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newExpense -> {
            int index = expenses.indexOf(selected);
            expenses.set(index, newExpense);
            saveData();
            updateUI();
        });
    }

    private void showBudgetDialog() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(monthlyBudget));
        dialog.setTitle("Set Monthly Budget");
        dialog.setHeaderText("Monthly Budget");
        dialog.setContentText("Enter budget amount:");

        dialog.showAndWait().ifPresent(result -> {
            try {
                monthlyBudget = Double.parseDouble(result);
                updateUI();
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount");
            }
        });
    }

    private void exportToCSV() {
        try {
            File file = new File("expenses_export.csv");
            PrintWriter writer = new PrintWriter(file);
            writer.println("Description,Amount,Category,Date");

            for (Expense e : expenses) {
                writer.printf("\"%s\",%.2f,\"%s\",%s%n",
                        e.getDescription(), e.getAmount(), e.getCategory(), e.getDate());
            }

            writer.close();
            showAlert("Success", "Exported to " + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert("Error", "Failed to export: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(title.equals("Error") ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(expenses));
            oos.writeDouble(monthlyBudget);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Expense> loaded = (List<Expense>) ois.readObject();
            expenses.setAll(loaded);
            monthlyBudget = ois.readDouble();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    public static class Expense implements Serializable {
        private String description;
        private double amount;
        private String category;
        private LocalDate date;

        public Expense(String description, double amount, String category, LocalDate date) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}