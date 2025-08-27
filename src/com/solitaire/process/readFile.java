package com.solitaire.process;

import java.io.File;
import java.io.IOException;
import java.util.*;
import com.solitaire.model.Card;
import com.solitaire.model.Rank;
import com.solitaire.model.Suit;

public class readFile {
	private static int deckSize = 52;
    public static List<Card> readDeckFromFile(String filename) throws IOException {
        List<Card> deck = loadDeck(filename);
        validateDeck(deck);
        return deck;
    }

    private static List<Card> loadDeck(String filename) throws IOException {
        List<Card> deck = new ArrayList<>();
        List<String> invalidCards = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                for (String cardStr : line.split(",")) {
                    parseCard(cardStr.trim(), deck, invalidCards);
                }
            }
        }

        if (!invalidCards.isEmpty()) {
            throw new IllegalArgumentException("Invalid cards found: " + invalidCards);
        }

        return deck;
    }

    private static void parseCard(String cardStr, List<Card> deck, List<String> invalidCards) {
        String[] parts = cardStr.split("-");
        if (parts.length != 2) {
            invalidCards.add(cardStr);
            return;
        }

        String suitStr = parts[0].trim().toUpperCase();
        String rankStr = parts[1].trim().toUpperCase();

        try {
            Suit suit = Suit.fromString(suitStr);
            Rank rank = Rank.fromString(rankStr);
            deck.add(new Card(suit, rank));
        } catch (IllegalArgumentException e) {
            invalidCards.add(cardStr);
        }
    }

    private static void validateDeck(List<Card> deck) {
        Set<Card> seen = new HashSet<>();
        List<Card> duplicates = new ArrayList<>();

        for (Card card : deck) {
            if (!seen.add(card)) {
                duplicates.add(card);
            }
        }

        if (!duplicates.isEmpty() || deck.size() != deckSize) {
            if (!duplicates.isEmpty()) {
                duplicates.forEach(card -> card.setFaceUp(true));
                throw new IllegalArgumentException(
                    "Invalid file: must contain 52 unique cards. Duplicates: " + duplicates
                );
            }
            throw new IllegalArgumentException("Invalid file: must contain exactly 52 unique cards.");
        }
    }
}
