package com.solitaire.process;

import java.util.*;
import com.solitaire.model.Card;

public abstract class Pile {
    protected List<Card> cards = new ArrayList<>();

    public abstract boolean canAddCard(Card card);

    public void addCard(Card card) { 
        cards.add(card); 
    }

    /**
     * Removes and returns the top card. 
     * Subclasses may override to add extra behavior (e.g. Tableau flips next card).
     */
    public Card removeTopCard() { 
        return cards.isEmpty() ? null : cards.remove(cards.size()-1); 
    }

    public Card peekTopCard() { 
        return cards.isEmpty() ? null : cards.get(cards.size()-1); 
    }

    // ---- Helpers ----
    public int size() { return cards.size(); }

    public boolean isEmpty() { return cards.isEmpty(); }

    /** Returns a copy to keep encapsulation. */
    public List<Card> getCards() { return new ArrayList<>(cards); }

    public void clear() { cards.clear(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card).append(" ");
        }
        return sb.toString().trim();
    }
}
