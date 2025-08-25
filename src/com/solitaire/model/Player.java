package com.solitaire.model;

import java.util.LinkedList;
import java.util.Queue;


public class Player {
    private final Queue<Card> deck = new LinkedList<>();


    public void addCard(Card card) {
        deck.add(card);
    }

    public Card playCard() {
        return deck.poll();
    }

    public boolean hasCards() {
        return !deck.isEmpty();
    }

    public int getDeckSize() {
        return deck.size();
    }

    public void displayDeck() {
        for (Card cardsInDeck : deck) {
            System.out.print(cardsInDeck + " ");
        }
        System.out.println();
    }
}

