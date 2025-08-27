package com.solitaire.game;

import com.solitaire.model.Card;
import com.solitaire.model.Dealer;
import com.solitaire.model.*;
import com.solitaire.process.*;
import java.util.*;

public class SolitaireGame {
    private int stockCycles = 0;
    private int stockCyclesLimit = 10;
    private StockPile stock;
    private WastePile waste;
    private List<FoundationPile> foundations;
    private List<TableauPile> tableau;

    // last tableau->tableau move info
    private int lastTableauFrom = -1;
    private int lastTableauTo = -1;
    private String lastTableauSequenceSig = null;

    // visited tableau-to-tableau moves in current cycle
    private Set<String> visitedMoves = new HashSet<>();
    private final Dealer dealer;

    public SolitaireGame(List<Card> deck, int shuffleCount) {
        dealer = new Dealer();

        System.out.println("Before shuffle:");
        dealer.displayDeck(deck);

        for (int i = 0; i < shuffleCount; i++) {
            dealer.shuffleDeck(deck);
        }

        System.out.println("After shuffle:");
        dealer.displayDeck(deck);
        
        for (Card card : deck) {
        	card.setFaceUp(false); 
        }

        // deal tableau (standard 7 piles, 1-7 cards)
        tableau = new ArrayList<>();
        int index = 0;
        for (int i = 1; i <= 7; i++) {
            List<Card> pileCards = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                pileCards.add(deck.get(index++));
            }
            tableau.add(new TableauPile(pileCards));
        }

        // stock = rest of the deck
        List<Card> stockCards = new ArrayList<>(deck.subList(index, deck.size()));
        Collections.reverse(stockCards);
        stock = new StockPile(stockCards);
        waste = new WastePile();

        // 4 foundations
        foundations = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            foundations.add(new FoundationPile(suit));
        }
    }

    public void playGame() {
        boolean moved;

        do {
            moved = false;

            // win check
            if (isGameWon()) {
                System.out.println("\n=== 🎉 YOU WON! All 52 cards are in the foundations. ===");
                printGameState();
                return;
            }

            // try moves in priority order
            if (tryWasteToFoundation()) { resetMoveTracking(); moved = true; continue; }
            if (tryWasteToTableau())    { resetMoveTracking(); moved = true; continue; }
            if (tryTableauToFoundation()) { resetMoveTracking(); moved = true; continue; }
            if (tryTableauToTableau())  { moved = true; continue; }
            if (tryStockOrRecycle())    { resetMoveTracking(); moved = true; continue; }

        } while (moved);

        System.out.println("\n=== GAME OVER (no more valid moves) ===");
        printGameState();
    }

    // ---------------- Move Helpers ----------------

    private boolean tryWasteToFoundation() {
        Card wasteTop = waste.peekTopCard();
        if (wasteTop == null) return false;

        for (FoundationPile foundation : foundations) {
            if (foundation.canAddCard(wasteTop)) {
                foundation.addCard(waste.removeTopCard());
                System.out.println("Moved " + wasteTop + " from Waste → Foundation");
                printGameState();
                return true;
            }
        }
        return false;
    }

    private boolean tryWasteToTableau() {
        Card wasteTop = waste.peekTopCard();
        if (wasteTop == null) return false;

        for (TableauPile tab : tableau) {
            if (tab.canAddCard(wasteTop)) {
                tab.addCard(waste.removeTopCard());
                System.out.println("Moved " + wasteTop + " from Waste → Tableau");
                printGameState();
                return true;
            }
        }
        return false;
    }

    private boolean tryTableauToFoundation() {
        for (TableauPile tab : tableau) {
            Card top = tab.peekTopCard();
            if (top == null) continue;

            for (FoundationPile foundation : foundations) {
                if (foundation.canAddCard(top)) {
                    foundation.addCard(tab.removeTopCard());
                    System.out.println("Moved " + top + " from Tableau → Foundation");

                    if (!tab.isEmpty()) {
                        Card newTop = tab.peekTopCard();
                        if (!newTop.isFaceUp()) newTop.setFaceUp(true);
                    }

                    printGameState();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryTableauToTableau() {
        for (int i = 0; i < tableau.size(); i++) {
            TableauPile from = tableau.get(i);
            List<Card> fromCards = from.getCards();

            for (int idx = 0; idx < fromCards.size(); idx++) {
                Card candidate = fromCards.get(idx);
                if (!candidate.isFaceUp()) continue;

                // only move if it reveals a hidden card
                boolean revealsNewCard = (idx > 0 && !fromCards.get(idx - 1).isFaceUp());
                if (!revealsNewCard) continue;

                List<Card> tentativeSeq = new ArrayList<>(fromCards.subList(idx, fromCards.size()));
                String seqSig = sequenceSignature(tentativeSeq);

                for (int j = 0; j < tableau.size(); j++) {
                    if (i == j) continue;
                    TableauPile to = tableau.get(j);

                    if (!to.canAddCard(candidate)) continue;

                    String moveKey = i + "->" + j + ":" + seqSig;
                    if (visitedMoves.contains(moveKey)) continue;

                    // prevent immediate reverse
                    if (lastTableauFrom == j && lastTableauTo == i && seqSig.equals(lastTableauSequenceSig)) {
                        continue;
                    }
                    // prevent re-moving same sequence into same target
                    if (lastTableauTo == j && seqSig.equals(lastTableauSequenceSig)) {
                        continue;
                    }

                    // special rule: only allow one King in first empty tableau
                    if (to.isEmpty() && candidate.getRank() == Rank.KING) {
                        if (!isFirstEmptyTableau(to)) continue;
                    }

                    // perform move
                    List<Card> sequence = from.extractSequenceFrom(candidate);
                    to.addSequence(sequence);

                    System.out.println("Moved sequence " + sequence +
                            " from Tableau " + (i + 1) + " → Tableau " + (j + 1));

                    if (!from.isEmpty()) {
                        Card newTop = from.peekTopCard();
                        if (!newTop.isFaceUp()) newTop.setFaceUp(true);
                    }

                    lastTableauFrom = i;
                    lastTableauTo = j;
                    lastTableauSequenceSig = seqSig;

                    visitedMoves.add(moveKey);

                    printGameState();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryStockOrRecycle() {
        if (!stock.isEmpty()) {
            List<Card> drawn = stock.drawCards(3);
            for (Card c : drawn) c.setFaceUp(true);
            waste.addCards(drawn);
            System.out.println("Drew " + drawn.size() + " cards from Stock → Waste");
            

            printGameState();
            resetMoveTracking();
            return true;
        } else if (!waste.isEmpty()) {
            stock.recycleFromWaste(waste);
            stockCycles++;

            // 🚨 detect infinite loop
            if (stockCycles > stockCyclesLimit) {
                System.out.println("\n=== GAME OVER (stock/waste loop detected) ===");
                printGameState();
                System.exit(0);  // hard stop game
            }

            System.out.println("Recycled Waste → Stock");
            printGameState();
            resetMoveTracking();
            return true;
        }
        return false;
    }

    // ---------------- Helpers ----------------

    private void resetMoveTracking() {
        lastTableauFrom = -1;
        lastTableauTo = -1;
        lastTableauSequenceSig = null;
        visitedMoves.clear(); // cleared only on non-tableau moves
    }

    private boolean isGameWon() {
        int total = 0;
        for (FoundationPile f : foundations) total += f.size();
        return total == 52;
    }

    private boolean isFirstEmptyTableau(TableauPile target) {
        for (TableauPile t : tableau) {
            if (t.isEmpty()) return t == target;
        }
        return false;
    }

    private String sequenceSignature(List<Card> seq) {
        StringBuilder sb = new StringBuilder();
        for (Card c : seq) {
            sb.append(c.getRank().name()).append("-").append(c.getSuit().name()).append(",");
        }
        return sb.toString();
    }

    private void printGameState() {
        System.out.println("\n=== Current Game State ===");

        System.out.print("Stock: ");
        System.out.println(stock.isEmpty() ? "[empty]" : "XX");

        System.out.print("Waste: ");
        System.out.println(waste.isEmpty() ? "[empty]" : waste.peekTopCard());

        for (int i = 0; i < foundations.size(); i++) {
            Card top = foundations.get(i).peekTopCard();
            System.out.println("Foundation " + (i + 1) + ": " + (top == null ? "[empty]" : top));
        }

        for (int i = 0; i < tableau.size(); i++) {
            System.out.print("Tableau " + (i + 1) + ": ");
            for (Card c : tableau.get(i).getCards()) {
                System.out.print(c.isFaceUp() ? c + " " : "XX ");
            }
            System.out.println();
        }
    }
    
    
    
}
