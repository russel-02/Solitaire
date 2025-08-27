package com.solitaire.model;

import java.util.*;




public class Dealer {


    public void displayDeck(List<Card> deck) {
        for (Card card : deck) {
        	card.setFaceUp(true);
            System.out.print(card + " ");
        }
        System.out.println();
    }
    

 
    public void shuffleDeck(List<Card> deck) {
        List<Card> firstHalf = new ArrayList<>(deck.subList(0, deck.size() / 2));
        List<Card> secondHalf = new ArrayList<>(deck.subList(deck.size() / 2, deck.size()));

        deck.clear();
        for (int i = 0; i < firstHalf.size(); i++) {
            deck.add(firstHalf.get(i));
            deck.add(secondHalf.get(i));
        }
    }
    
}
