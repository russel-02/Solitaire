package com.solitaire.model;



public enum Rank {
	ACE("A", 1),
	TWO("2", 2),
    THREE("3", 3),
    FOUR("4", 4),
    FIVE("5", 5),
    SIX("6", 6),
    SEVEN("7", 7),
    EIGHT("8", 8),
    NINE("9", 9),
    TEN("10", 10),
    JACK("J", 11),
    QUEEN("Q", 12),
	KING("K", 13),
	;

    private final String symbol;
    private final int value;

    Rank(String symbol, int value) {
        this.symbol = symbol;
        this.value = value;
    }

    public String getSymbol() { 
        return symbol; 
    }

    public int getValue() { 
        return value; 
    }
    
    public static Rank fromString(String str) {
        for (Rank r : values()) {
            if (r.symbol.equals(str)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Invalid rank: " + str);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
