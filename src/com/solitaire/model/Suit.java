package com.solitaire.model;


public enum Suit {
    DIAMOND("D", 4),
    HEART("H", 3),
    SPADES("S", 2),
    CLUBS("C", 1);

    private final String symbol;
    private final int value;

    Suit(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
    }

    public String getSymbol() { return symbol; }
    public int getValue() { return value; }
    
    public static Suit fromString(String str) {
        for (Suit s : values()) {
            if (s.symbol.equalsIgnoreCase(str)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid suit: " + str);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
