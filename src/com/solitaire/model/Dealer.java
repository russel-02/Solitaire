package com.solitaire.model;


import java.util.*;

public class Dealer {
    private List<Card> deck;

    public Dealer(List<Card> deck) {
        this.deck = new ArrayList<>(deck);
    }

    public void displayDeck() {
        for (Card cardsInDeck : deck) {
            System.out.print(cardsInDeck + " ");
        }
        System.out.println();
    }

    public void shuffleDeck(int times) {
        for (int t = 0; t < times; t++) {
            List<Card> FirstHalf = new ArrayList<>(deck.subList(0, deck.size() / 2));
            List<Card> SecondHalf = new ArrayList<>(deck.subList(deck.size() / 2, deck.size()));
            List<Card> newDeck = new ArrayList<>();
            for (int i = 0; i < FirstHalf.size(); i++) {
                newDeck.add(FirstHalf.get(i));
                newDeck.add(SecondHalf.get(i));
            }
            deck = newDeck;
        }
    }


}

