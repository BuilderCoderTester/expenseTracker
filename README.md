# Expense Tracker

A desktop expense tracking application built with JavaFX and Maven. The app lets users record expenses, organize them by category, review spending totals, compare spending against a monthly budget, visualize category totals, and export expense data to CSV.

## What It Does

- Adds expenses with a description, amount, category, and date.
- Shows all saved expenses in a table.
- Supports searching by description or category.
- Supports filtering by category.
- Allows editing an expense by double-clicking a table row or using the context menu.
- Allows deleting the selected expense.
- Tracks total spending and total number of expense entries.
- Tracks progress against a monthly budget.
- Displays spending by category in a pie chart.
- Saves expense data locally so it is available when the app is reopened.
- Exports expenses to `expenses_export.csv`.

## How It Works

The main application is implemented in `src/main/java/com/javafx/javafx/Launcher.java`.

When the app starts, `Launcher` loads saved data from `expenses.dat`, builds the JavaFX interface in code, and then updates the table, budget label, statistics cards, and pie chart.

The application keeps expenses in an `ObservableList<Expense>`. Because the list is observable, JavaFX controls such as `TableView` can update when the list changes.

Each expense contains:

- `description`
- `amount`
- `category`
- `date`

The app validates user input before adding a new expense:

- Description must not be empty.
- Amount must be a positive number.
- Category must be selected.
- Date must be selected.

After adding, editing, or deleting an expense, the app:

1. Updates the in-memory expense list.
2. Saves the list and monthly budget to `expenses.dat`.
3. Recalculates total spending and item count.
4. Refreshes the budget status.
5. Rebuilds the pie chart from category totals.

## Data Storage

Expense data is stored locally in `expenses.dat` using Java object serialization.

The monthly budget is saved in the same file after the expense list. This lets the app restore both the saved expenses and the budget value the next time it opens.

CSV export writes a separate file:

```text
expenses_export.csv
```

The exported CSV contains:

```text
Description,Amount,Category,Date
```

## Project Structure

```text
src/main/java/com/javafx/javafx/
  Launcher.java          Main Expense Tracker application
  Expense.java           Expense model class
  HelloApplication.java  Starter/template JavaFX app
  HelloController.java   Starter/template FXML controller

src/main/resources/com/javafx/javafx/
  hello-view.fxml        Starter/template FXML view

pom.xml                 Maven project configuration
expenses.dat            Local serialized expense data
expenses_export.csv     CSV export output
target/                 Build output
```

The active expense tracker entry point is `Launcher.java`. The `HelloApplication`, `HelloController`, and `hello-view.fxml` files are from the default JavaFX starter template and are not the main expense tracker UI.

## Requirements

- Java 17 or newer
- Maven, or the included Maven wrapper
- JavaFX dependencies are managed through Maven

## Run The App

On Windows:

```powershell
.\mvnw.cmd clean javafx:run
```

On macOS or Linux:

```bash
./mvnw clean javafx:run
```

The Maven JavaFX plugin is configured to launch:

```text
com.javafx.javafx/com.javafx.javafx.Launcher
```

## Build And Package

The project uses:

- `maven-compiler-plugin` for Java 17 compilation.
- `javafx-maven-plugin` for running and creating a Java runtime image.
- `jpackage-maven-plugin` for creating a packaged desktop installer.

The packaged Windows installer output is configured for:

```text
target/installer/ExpenseTracker-1.0.0.exe
```

## Main Technologies

- Java 17
- JavaFX 21.0.6
- Maven
- Java object serialization
- CSV export with `PrintWriter`
