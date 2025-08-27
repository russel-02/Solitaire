package com.solitaire.process;

import java.io.File;
import java.io.IOException;
import java.util.*;
import com.solitaire.model.Card;
import com.solitaire.model.Rank;
import com.solitaire.model.Suit;

public class readFile {

    public static List<Card> readDeckFromFile(String filename) {
        Scanner input = new Scanner(System.in);
        List<Card> deck = new ArrayList<>();
        boolean fileLoaded = false;

        while (!fileLoaded) {
            List<String> invalidCards = new ArrayList<>();

            try (Scanner fileScanner = new Scanner(new File(filename))) {
                deck.clear();

                
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine().trim();
                    if (line.isEmpty()) continue; 

                    StringTokenizer cardTokenizer = new StringTokenizer(line, ",");

                   
                    while (cardTokenizer.hasMoreTokens()) {
                        String cardStr = cardTokenizer.nextToken().trim();
                        StringTokenizer parts = new StringTokenizer(cardStr, "-");

                        if (parts.countTokens() == 2) {
                            String suitStr = parts.nextToken().trim().toUpperCase();
                            String rankStr = parts.nextToken().trim().toUpperCase();

                            try {
                                Suit suit = Suit.fromString(suitStr);
                                Rank rank = Rank.fromString(rankStr);
                                deck.add(new Card(suit, rank));
                            } catch (IllegalArgumentException e) {
                                invalidCards.add(cardStr);
                            }
                        } else {
                            invalidCards.add(cardStr);
                        }
                    }
                }

                
                if (!invalidCards.isEmpty()) {
                    System.out.println("Invalid cards found: " + invalidCards);
                    filename = promptFilePath(input);
                    continue;
                }

               
                Set<Card> seen = new HashSet<>();
                List<Card> duplicates = new ArrayList<>();
                for (Card card : deck) {
                    if (!seen.add(card)) {
                        duplicates.add(card);
                    }
                }

                if (!duplicates.isEmpty() || deck.size() != 52) {
                    System.out.println("Invalid file: must contain exactly 52 unique cards.");
                    if (!duplicates.isEmpty()) {
                    	duplicates.forEach(card  -> card.setFaceUp(true));
                        System.out.println("   Duplicates: " + duplicates);
                    }
                    filename = promptFilePath(input);
                    continue;
                }

                fileLoaded = true;

            } catch (IOException e) {
                System.out.println("File not found or cannot be read.");
                filename = promptFilePath(input);
            }
        }

        return deck;
    }

    private static String promptFilePath(Scanner input) {
        System.out.print("Input File path: ");
        return input.nextLine();
    }
}

