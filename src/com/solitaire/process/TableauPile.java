package com.solitaire.process;

import com.solitaire.model.*;
import java.util.*;

public class TableauPile extends Pile {
    
    public TableauPile(List<Card> cards) {
        this.cards.addAll(cards);
        if (!this.cards.isEmpty()) {
            this.cards.get(this.cards.size() - 1).setFaceUp(true); // last card face up
        }
    }

    @Override
    public boolean canAddCard(Card card) {
        if (isEmpty()) {
            return card.getRank() == Rank.KING; // only King can start empty pile
        }
        Card top = peekTopCard();
        return top != null &&
                top.isFaceUp() &&
                top.isRed() != card.isRed() &&
                top.getRankValue() == card.getRankValue() + 1;
    }
    
    /**
     * Returns the top face-up sequence starting at given card.
     * If the card isn’t found or is face-down, returns empty list.
     */
    public List<Card> extractSequenceFrom(Card start) {
        int index = cards.indexOf(start);
        if (index == -1 || !start.isFaceUp()) return new ArrayList<>();

        List<Card> sequence = new ArrayList<>(cards.subList(index, cards.size()));
        cards.subList(index, cards.size()).clear(); // remove from tableau

        // flip new last card if any remain
        flipLastFaceUp();

        return sequence;
    }
    
    /**
     * Remove and return the top card.
     */
    @Override
    public Card removeTopCard() {
        if (cards.isEmpty()) return null;
        Card removed = cards.remove(cards.size() - 1);
        flipLastFaceUp(); // reveal next card if any
        return removed;
    }

    /**
     * Add a sequence of cards to this tableau pile.
     */
    public void addSequence(List<Card> sequence) {
        if (sequence == null || sequence.isEmpty()) return;
        cards.addAll(sequence);
    }

    public Card peekTopCard() {
        return cards.isEmpty() ? null : cards.get(cards.size() - 1);
    }

    /**
     * Utility: flip the last card face-up if pile not empty.
     */
    private void flipLastFaceUp() {
        if (!cards.isEmpty()) {
            cards.get(cards.size() - 1).setFaceUp(true);
        }
    }
    
    public int size() {
        return cards.size();
    }

    /**
     * Rule: Prevent unnecessary tableau-to-tableau moves.
     * Only allow if:
     *  1. It exposes a new face-down card in the source pile, OR
     *  2. The destination pile was empty and this is the only valid King placement.
     */
    public boolean isNecessaryMove(List<Card> sequence, TableauPile source) {
        // Case 1: Exposes a new card in source
        boolean exposesCard = source.size() > sequence.size() &&
                              !source.peekTopCard().isFaceUp();
        
        // Case 2: Empty tableau and placing King
        boolean kingToEmpty = this.isEmpty() && sequence.get(0).getRank() == Rank.KING;
        
        return exposesCard || kingToEmpty;
    }

    @Override
    public String toString() {
        return "Tableau: " + getCards();
    }
}
