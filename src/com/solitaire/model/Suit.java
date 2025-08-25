package com.solitaire.model;


public enum Suit {
    DIAMOND("D", 4),
    HEART("H", 3),
    SPADES("S", 2),
    CLUBS("C", 1);

    private final String symbol;
    private final int priority;

    Suit(String symbol, int priority) {
        this.symbol = symbol;
        this.priority = priority;
    }

    public String getSymbol() { return symbol; }
    public int getPriority() { return priority; }

    @Override
    public String toString() {
        return symbol;
    }
}
