package com.solitaire.process;

import com.solitaire.model.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockPile extends Pile {

    public StockPile(List<Card> cards) {
        if (cards != null) {
            this.cards.addAll(cards);
            
            for (Card c : this.cards) {
                c.setFaceUp(false);
            }
        }
    }

    @Override
    public boolean canAddCard(Card card) {
        return false; // no direct adding to stock
    }

    /**
     * Draw up to 'n' cards from stock (top = last element).
     * All cards are flipped face-up before being returned.
     */
    public List<Card> drawCards(int n) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < n && !cards.isEmpty(); i++) {
            Card c = cards.remove(cards.size() - 1);
            c.setFaceUp(true);
            drawn.add(c);
        }
        return drawn;
    }

    /** Alias so either draw(3) or drawCards(3) works. */
    public List<Card> draw(int count) {
        return drawCards(count);
    }

    /**
     * Recycle waste back into stock (flip all cards face down again).
     */
    public void recycleFromWaste(WastePile waste) {
        if (waste.isEmpty()) return;

        // Reverse order of waste → so the first drawn card goes back on top
        List<Card> wasteCards = new ArrayList<>(waste.getCards());
        Collections.reverse(wasteCards);

        this.cards.addAll(wasteCards); // back into stock
        waste.clear();

        // All cards should be face down again
        for (Card c : this.cards) {
            c.setFaceUp(false);
        }
    }


    /** Convenience method for debugging/UI */
    public Card peekTop() {
        return cards.isEmpty() ? null : cards.get(cards.size() - 1);
    }

    @Override
    public String toString() {
        return "Stock (" + size() + " cards)";
    }
}
