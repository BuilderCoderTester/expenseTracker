package com.javafx.javafx;


import java.io.Serializable;
import java.time.LocalDate;

public class Expense implements Serializable {
    private String description;
    private double amount;
    private String category;
    private LocalDate date;

    public Expense(String description, double amount, String category, LocalDate date){
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }

    public double getAmount(){return amount;}
    public void setAmount(double amount){this.amount = amount;}

    public String getCategory(){return category;}
    public void setCategory(String category){
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
