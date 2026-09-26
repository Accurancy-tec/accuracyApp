package com.example.accurancymobileapp.model;

public class ActiveSpinner {

    private String symbol;
    private String name;

    public ActiveSpinner(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return symbol + " - " + name;
    }
}