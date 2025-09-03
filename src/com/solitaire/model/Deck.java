package com.solitaire.model;

import java.util.*;

public class Deck {
    private final List<Card> cards = new ArrayList<>(52);

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public int size() {
        return cards.size();
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    public void setCards(List<Card> newOrder) {
        cards.clear();
        cards.addAll(newOrder);
    }

    @Override
    public String toString() {
        return cards.toString();
    }
}
