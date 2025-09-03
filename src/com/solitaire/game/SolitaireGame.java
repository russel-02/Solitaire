package com.solitaire.game;

import com.solitaire.model.*;
import com.solitaire.process.*;
import java.util.*;

public class SolitaireGame {
	// --- Game configuration ---
	private boolean progressMade = true;

	// --- Game state ---
	private final StockPile stock;
	private final WastePile waste;
	private static int DRAWN_CARDS;
	private static int deckSize;
	private final List<FoundationPile> foundations;
	private final List<TableauPile> tableau;
	private final Set<String> seenStates = new HashSet<>();

	// --- Move tracking (to avoid infinite loops) ---
	private int lastTableauFrom = -1;
	private int lastTableauTo = -1;
	private String lastTableauSeqSig = null;
	private final Set<String> visitedMoves = new HashSet<>();

	private final Dealer dealer;

	// ================= Constructor =================
	public SolitaireGame(List<Card> deck, int shuffleCount, int shuffleType, int DrawnCardsInput) {
		dealer = new Dealer();
		DRAWN_CARDS = DrawnCardsInput;
		deckSize = deck.size();

		System.out.println("\nBefore shuffle:");
		dealer.displayDeck(deck);
		System.out.println();

		shuffleDeck(deck, shuffleCount, shuffleType);
		System.out.println();

		System.out.println("After shuffle:");
		dealer.displayDeck(deck);
		System.out.println();

		deck.forEach(cardFace -> cardFace.setFaceUp(false));

		tableau = new ArrayList<>();
		int index = 0;
		for (int pileSize = 1; pileSize <= 7; pileSize++) {
			List<Card> pileCards = new ArrayList<>();
			for (int j = 0; j < pileSize; j++) {
				pileCards.add(deck.get(index++));
			}
			tableau.add(new TableauPile(pileCards));
		}

		List<Card> stockCards = new ArrayList<>(deck.subList(index, deck.size()));
		stock = new StockPile(stockCards);
		waste = new WastePile();

		// 4 empty foundations
		foundations = new ArrayList<>();
		for (Suit suit : Suit.values()) {
			foundations.add(new FoundationPile(suit));
		}
	}

	private void shuffleDeck(List<Card> deck, int shuffleCount, int shuffleType) {
		switch (shuffleType) {
		case 1:
			for (int i = 0; i < shuffleCount; i++) {
				Collections.shuffle(deck);
			}
			System.out.println("Shuffle used: Random Shuffling");
			break;
		case 2:
			shuffleCount %= 8; 
			for (int i = 0; i < shuffleCount; i++) {
				dealer.shuffleDeck(deck);
			}
			System.out.println("Shuffle used: Faro Shuffling");
			break;
		default:
			System.out.println("No shuffle type applied.");
		}
	}

	// ================= Main Game Loop =================
	public void playGame() {
		printGameState();
		
		do {
			progressMade = false;
			
			if (isGameWon()) {
				System.out.println("\n=== YOU WON! All 52 cards are in the foundations. ===");
				return;
			}
					

			if (tryWasteToFoundation()) {
				resetTracking();
				progressMade = true;
				continue;
			}
			if (tryWasteToTableau()) {
				resetTracking();
				progressMade = true;
				continue;
			}
			if (tryTableauToFoundation()) {
				resetTracking();
				progressMade = true;
				continue;
			}
			if (tryTableauToTableau()) {
				resetTracking();
				progressMade = true;
				continue;
			}
			if (tryStockOrRecycle()) {
				resetTracking();
				progressMade = true;
				continue;
			}

		} while (progressMade);
		System.out.println("\n=== GAME OVER (no more valid moves) ===");
	}

	// ================= Move Handlers =================
	private boolean tryWasteToFoundation() {
		Card top = waste.peekTopCard();
		if (top == null) 
			return false;
		

		for (FoundationPile foundationPile : foundations) {
			if (foundationPile.canAddCard(top)) {
				foundationPile.addCard(waste.removeTopCard());
				System.out.println("Moved " + top + " from Waste -> Foundation");
				printGameState();
				return true;
			}
		}
		return false;
	}

	private boolean tryWasteToTableau() {
		Card top = waste.peekTopCard();
		if (top == null) 
			return false;
		
		for (TableauPile tableauPile : tableau) {
			if (tableauPile.canAddCard(top)) {
				tableauPile.addCard(waste.removeTopCard());
				System.out.println("Moved " + top + " from Waste -> Tableau");
				printGameState();
				return true;
			}
		}
		return false;
	}

	private boolean tryTableauToFoundation() {
		for (TableauPile tableauPile : tableau) {
			Card top = tableauPile.peekTopCard();
			if (top == null)
				continue;
			
			for (FoundationPile fondationPile : foundations) {
				if (fondationPile.canAddCard(top)) {
					fondationPile.addCard(tableauPile.removeTopCard());
					System.out.println("Moved " + top + " from Tableau -> Foundation");
					
					if (!tableauPile.isEmpty() && !tableauPile.peekTopCard().isFaceUp()) {
						tableauPile.peekTopCard().setFaceUp(true);
					}
					printGameState();
					return true;
				}
			}
		}
		return false;
	}

	private boolean tryTableauToTableau() {
		return attemptTableauMove(true) || attemptTableauMove(false);
	}

	private boolean tryStockOrRecycle() {
		if (!stock.isEmpty()) {
			List<Card> drawn = stock.drawCards(DRAWN_CARDS);
			drawn.forEach(cardFace -> cardFace.setFaceUp(true));
			waste.addCards(drawn);

			System.out.println("Drew " + drawn.size() + " cards from Stock -> Waste");
			printGameState();
			return true;
		}

		if (!waste.isEmpty()) {
		    stock.recycleFromWaste(waste);
		    System.out.println("Recycled Waste -> Stock");

		    String sig = gameStateSignature();
		    if (!seenStates.add(sig)) {
		    	printGameState();
		        return false;
		    }
		    printGameState();
		    return true;
		}
		return false;
	}

	// ================= Tableau Move Logic =================
	/**
	 * Attempt to move a sequence of cards between tableau piles.
	 *
	 * @param requireReveal true if only moves that reveal a hidden card should be considered,
	 *                      false if only moves that do NOT reveal anything new should be considered.
	 * @return true if a valid move was made, false otherwise.
	 */
	private boolean attemptTableauMove(boolean requireReveal) {
	    // Try each tableau pile as the "source" pile
	    for (int sourceIndex = 0; sourceIndex < tableau.size(); sourceIndex++) {
	        TableauPile sourcePile = tableau.get(sourceIndex);
	        List<Card> sourceCards = sourcePile.getCards();

	        // Try each card in the source pile as a potential start of a movable sequence
	        for (int cardIndex = 0; cardIndex < sourceCards.size(); cardIndex++) {
	            Card candidate = sourceCards.get(cardIndex);

	            if (!candidate.isFaceUp())
	                continue;

	            // Check visibility conditions
	            boolean allFaceUp = sourceCards.stream().allMatch(Card::isFaceUp);
	            boolean revealsNew = (cardIndex > 0 && !sourceCards.get(cardIndex - 1).isFaceUp());

	            // If we're only looking for "revealing" moves, skip if it doesn't reveal
	            if (requireReveal && !revealsNew)
	                continue;

	            // If we're only looking for non-revealing moves, skip if it WOULD reveal
	            // or if not all cards in the pile are already visible
	            if (!requireReveal && (revealsNew || !allFaceUp))
	                continue;

	            // If nothing is revealed and this is not the very top card, skip it
	            if (!revealsNew && cardIndex > 0)
	                continue;

	            // Extract the sequence starting from this candidate card to the bottom
	            List<Card> sequence = new ArrayList<>(sourceCards.subList(cardIndex, sourceCards.size()));
	            String sequenceSignature = sequenceSignature(sequence);

	            for (int targetIndex = 0; targetIndex < tableau.size(); targetIndex++) {
	                if (sourceIndex == targetIndex)
	                    continue; // Can't move onto itself

	                TableauPile targetPile = tableau.get(targetIndex);

	                if (!targetPile.canAddCard(candidate))
	                    continue;

	                String moveKey = sourceIndex + "->" + targetIndex + ":" + sequenceSignature;

	                if (visitedMoves.contains(moveKey))
	                    continue;

	                if (isReverseOrRepeat(sourceIndex, targetIndex, sequenceSignature))
	                    continue;

	                if (targetPile.isEmpty() && candidate.getRank() == Rank.KING && !revealsNew)
	                    continue;

	                List<Card> movingSequence = sourcePile.extractSequenceFrom(candidate);
	                targetPile.addSequence(movingSequence);

	                System.out.println(
	                    "Moved sequence " + movingSequence +
	                    " from Tableau " + (sourceIndex + 1) +
	                    " -> Tableau " + (targetIndex + 1)
	                );

	                if (!sourcePile.isEmpty() && !sourcePile.peekTopCard().isFaceUp()) {
	                    sourcePile.peekTopCard().setFaceUp(true);
	                }

	                updateMoveTracking(sourceIndex, targetIndex, sequenceSignature, moveKey);

	                printGameState();

	                return true; 
	            }
	        }
	    }
	    return false;
	}

	private boolean isReverseOrRepeat(int sourceIndex, int targetIndex, String sequenceSignature) {
		return (lastTableauFrom == targetIndex && lastTableauTo == sourceIndex && sequenceSignature.equals(lastTableauSeqSig))
				|| (lastTableauTo == targetIndex && sequenceSignature.equals(lastTableauSeqSig));
	}

	private void updateMoveTracking(int sourceIndex, int targetIndex, String sequenceSignature, String moveKey) {
		lastTableauFrom = sourceIndex;
		lastTableauTo = targetIndex;
		lastTableauSeqSig = sequenceSignature;
		visitedMoves.add(moveKey);
	}

	// ================= Utility Methods =================
	private void resetTracking() {
		lastTableauFrom = -1;
		lastTableauTo = -1;
		lastTableauSeqSig = null;
		visitedMoves.clear();
	}

	// Create like barcode to check if same pattern has been used
	private String sequenceSignature(List<Card> seq) {
		StringBuilder sb = new StringBuilder();
		for (Card card : seq) {
			sb.append(card.getRank().name()).append("-").append(card.getSuit().name()).append(",");
		}
		return sb.toString();
	}
	
	//Represents entire game state to detect repeated states to avoid looping
	private String gameStateSignature() {
	    StringBuilder sb = new StringBuilder();

	    for (FoundationPile foundationPile : foundations) {
	        Card top = foundationPile.peekTopCard();
	        sb.append(top == null ? "[]" : top.toString()).append("|");
	    }

	    for (TableauPile tableauPile : tableau) {
	        for (Card card : tableauPile.getCards()) {
	            sb.append(card.isFaceUp() ? card.toString() : "XX").append(",");
	        }
	        sb.append("|");
	    }
	    
	    sb.append("W:[");
	    for (Card card : waste.getCards()) {
	        sb.append(card.toString()).append(",");
	    }
	    sb.append("]");

	    sb.append("S:[");
	    for (Card card : stock.getCards()) {
	        sb.append(card.toString()).append(",");
	    }
	    sb.append("]");
	    return sb.toString();
	}
	
	private boolean isGameWon() {
		return foundations.stream().mapToInt(FoundationPile::size).sum() == deckSize;
	}


	private void printGameState() {
		System.out.println("\n=== Game State ===");

		System.out.println("Stock: " + (stock.isEmpty() ? "[empty]" : "XX"));
		System.out.println("Waste: " + (waste.isEmpty() ? "[empty]" : waste.peekTopCard()));
		System.out.println();

		for (int i = 0; i < foundations.size(); i++) {
			Card top = foundations.get(i).peekTopCard();
			System.out.println("Foundation " + (i + 1) + ": " + (top == null ? "[empty]" : top));
		}
		System.out.println();

		for (int i = 0; i < tableau.size(); i++) {
			System.out.print("Tableau " + (i + 1) + ": ");
			for (Card cardFace : tableau.get(i).getCards()) {
				System.out.print(cardFace.isFaceUp() ? cardFace + " " : "XX ");
			}
			System.out.println();
		}
		System.out.println();
	}
}
