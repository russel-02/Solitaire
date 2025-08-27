package com.solitaire.process;

import com.solitaire.model.Card;
import java.util.List;

public class WastePile extends Pile {

    @Override
    public boolean canAddCard(Card card) {
        // waste can always accept drawn cards
        return true;
    }

    public void addCards(List<Card> cards) {
        for (Card c : cards) {
            c.setFaceUp(true);   // ensure all cards are face up
            this.cards.add(c);
        }
    }

    @Override
    public String toString() {
        return "Waste: " + cards;
    }
}
