package com.solitaire.process;

import com.solitaire.model.Card;
import com.solitaire.model.Rank;
import com.solitaire.model.Suit;

public class FoundationPile extends Pile {
    private final Suit suit;

    public FoundationPile(Suit suit) {
        this.suit = suit;
    }

    @Override
    public boolean canAddCard(Card card) {
        if (card.getSuit() != suit) return false;

        if (isEmpty()) {
            return card.getRank() == Rank.ACE;
        }

        Card top = peekTopCard();
        return card.getRankValue() == top.getRankValue() + 1;
    }

    @Override
    public String toString() {
        return "Foundation " + suit + ": " + (isEmpty() ? "[empty]" : peekTopCard());
    }
}
