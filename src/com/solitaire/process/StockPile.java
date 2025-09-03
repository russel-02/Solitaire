package com.solitaire.process;

import com.solitaire.model.Card;
import java.util.ArrayList;
import java.util.List;

public class StockPile extends Pile {

    public StockPile(List<Card> cards) {
        if (cards != null) {
            this.cards.addAll(cards);
            
            for (Card cardStock : this.cards) {
            	cardStock.setFaceUp(false);
            }
        }
    }

    @Override
    public boolean canAddCard(Card card) {
        return false; // no direct adding to stock
    }

    public List<Card> drawCards(int n) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < n && !cards.isEmpty(); i++) {
            Card cardStock = cards.remove(0);
            cardStock.setFaceUp(true);
            drawn.add(cardStock);
        }
        return drawn;
    }

    public void recycleFromWaste(WastePile waste) {
        if (waste.isEmpty()) return;
        List<Card> wasteCards = new ArrayList<>(waste.getCards());
        this.cards.addAll(wasteCards); 
        waste.clear();
        
        for (Card cardStock : this.cards) {
        	cardStock.setFaceUp(false);
        }
    }

    @Override
    public String toString() {
        return "Stock (" + size() + " cards)";
    }
}
