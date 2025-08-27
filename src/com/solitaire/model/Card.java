package com.solitaire.model;

import java.util.Objects;



public class Card implements Comparable<Card> {
    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    public int getRankValue() { return rank.getValue(); }
    public int getSuitValue() { return suit.getValue(); }

    public boolean isRed() {
        return suit == Suit.HEART || suit == Suit.DIAMOND;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card other = (Card) obj;
        return suit == other.suit && rank == other.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }

    @Override
    public String toString() {
        return faceUp ? rank + "-" + suit : "XX";
    }

    @Override
    public int compareTo(Card other) {
        int cmp = Integer.compare(this.getRankValue(), other.getRankValue());
        if (cmp != 0) return cmp;
        return Integer.compare(this.getSuitValue(), other.getSuitValue());
    }

    public boolean isFaceUp() { return faceUp; }
    public void setFaceUp(boolean faceUp) { this.faceUp = faceUp; }
}
