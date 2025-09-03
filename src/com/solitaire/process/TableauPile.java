package com.solitaire.process;

import com.solitaire.model.*;
import java.util.*;

public class TableauPile extends Pile {
    
    public TableauPile(List<Card> cards) {
        this.cards.addAll(cards);
        if (!this.cards.isEmpty()) {
            this.cards.get(this.cards.size() - 1).setFaceUp(true); 
        }
    }

    @Override
    public boolean canAddCard(Card card) {
        if (isEmpty()) {
            return card.getRank() == Rank.KING; 
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

        flipLastFaceUp();

        return sequence;
    }
    
    @Override
    public Card removeTopCard() {
        if (cards.isEmpty()) return null;
        Card removed = cards.remove(cards.size() - 1);
        flipLastFaceUp(); 
        return removed;
    }

    public void addSequence(List<Card> sequence) {
        if (sequence == null || sequence.isEmpty()) return;
        cards.addAll(sequence);
    }

    private void flipLastFaceUp() {
        if (!cards.isEmpty()) {
            cards.get(cards.size() - 1).setFaceUp(true);
        }
    }

    @Override
    public String toString() {
        return "Tableau: " + getCards();
    }
}
