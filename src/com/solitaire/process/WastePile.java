package com.solitaire.process;

import com.solitaire.model.Card;
import java.util.List;

public class WastePile extends Pile {

    @Override
    public boolean canAddCard(Card card) {
       
        return true;
    }

    public void addCards(List<Card> cards) {
        for (Card wasteCardFace : cards) {
        	wasteCardFace.setFaceUp(true);
            this.cards.add(wasteCardFace);
        }
    }

    @Override
    public String toString() {
        return "Waste: " + cards;
    }
}
