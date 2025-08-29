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

		System.out.println("\nBefore shuffle:");
		dealer.displayDeck(deck);
		System.out.println();

		// Shuffle deck based on type
		shuffleDeck(deck, shuffleCount, shuffleType);

		System.out.println("After shuffle:");
		dealer.displayDeck(deck);
		System.out.println();

		// reset all cards to facedown
		deck.forEach(cardFace -> cardFace.setFaceUp(false));

		// deal tableau (7 piles, 1 to 7 cards each)
		tableau = new ArrayList<>();
		int index = 0;
		for (int pileSize = 1; pileSize <= 7; pileSize++) {
			List<Card> pileCards = new ArrayList<>();
			for (int j = 0; j < pileSize; j++) {
				pileCards.add(deck.get(index++));
			}
			tableau.add(new TableauPile(pileCards));
		}

		// remaining deck goes into stock
		List<Card> stockCards = new ArrayList<>(deck.subList(index, deck.size()));
		Collections.reverse(stockCards);
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
			System.out.println("No shuffle applied.");
		}
	}

	// ================= Main Game Loop =================
	public void playGame() {
		do {
			progressMade = false;
			
			

			if (isGameWon()) {
				System.out.println("\n=== YOU WON! All 52 cards are in the foundations. ===");
				return;
			}

			// Try moves in priority order
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
				System.out.println("Moved " + top + " from Waste → Foundation");
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
				System.out.println("Moved " + top + " from Waste → Tableau");
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
					System.out.println("Moved " + top + " from Tableau → Foundation");

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

			System.out.println("Drew " + drawn.size() + " cards from Stock → Waste");
			printGameState();
			return true;
		}

		if (!waste.isEmpty()) {
			
		    stock.recycleFromWaste(waste);
		    System.out.println("Recycled Waste → Stock");

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
	private boolean attemptTableauMove(boolean requireReveal) {
		for (int i = 0; i < tableau.size(); i++) {
			TableauPile from = tableau.get(i);
			List<Card> cards = from.getCards();

			for (int idx = 0; idx < cards.size(); idx++) {
				Card candidate = cards.get(idx);
				if (!candidate.isFaceUp())
					continue;

				boolean allFaceUp = cards.stream().allMatch(Card::isFaceUp);
				boolean revealsNew = (idx > 0 && !cards.get(idx - 1).isFaceUp());

				if (requireReveal && !revealsNew)
					continue;
				if (!requireReveal && (revealsNew || !allFaceUp))
					continue;
				if (!revealsNew && idx > 0)
					continue;

				List<Card> seq = new ArrayList<>(cards.subList(idx, cards.size()));
				String seqSig = sequenceSignature(seq);

				for (int j = 0; j < tableau.size(); j++) {
					if (i == j)
						continue;
					TableauPile to = tableau.get(j);

					if (!to.canAddCard(candidate))
						continue;

					String moveKey = i + "->" + j + ":" + seqSig;
					if (visitedMoves.contains(moveKey))
						continue;

					if (isReverseOrRepeat(i, j, seqSig))
						continue;
					if (to.isEmpty() && candidate.getRank() == Rank.KING && !revealsNew)
						continue;

					List<Card> movingSeq = from.extractSequenceFrom(candidate);
					to.addSequence(movingSeq);

					System.out.println(
							"Moved sequence " + movingSeq + " from Tableau " + (i + 1) + " → Tableau " + (j + 1));

					if (!from.isEmpty() && !from.peekTopCard().isFaceUp()) {
						from.peekTopCard().setFaceUp(true);
					}

					updateMoveTracking(i, j, seqSig, moveKey);
					printGameState();
					return true;
				}
			}
		}
		return false;
	}

	private boolean isReverseOrRepeat(int from, int to, String seqSig) {
		return (lastTableauFrom == to && lastTableauTo == from && seqSig.equals(lastTableauSeqSig))
				|| (lastTableauTo == to && seqSig.equals(lastTableauSeqSig));
	}

	private void updateMoveTracking(int from, int to, String seqSig, String moveKey) {
		lastTableauFrom = from;
		lastTableauTo = to;
		lastTableauSeqSig = seqSig;
		visitedMoves.add(moveKey);
	}

	// ================= Utility Methods =================
	private void resetTracking() {
		lastTableauFrom = -1;
		lastTableauTo = -1;
		lastTableauSeqSig = null;
		visitedMoves.clear();
	}

	private boolean isGameWon() {
		return foundations.stream().mapToInt(FoundationPile::size).sum() == 52;
	}

	private String sequenceSignature(List<Card> seq) {
		StringBuilder sb = new StringBuilder();
		for (Card c : seq) {
			sb.append(c.getRank().name()).append("-").append(c.getSuit().name()).append(",");
		}
		return sb.toString();
	}
	
	private String gameStateSignature() {
	    StringBuilder sb = new StringBuilder();

	    // Foundations (only top is needed, since they never shrink)
	    for (FoundationPile f : foundations) {
	        Card top = f.peekTopCard();
	        sb.append(top == null ? "[]" : top.toString()).append("|");
	    }

	    // Tableau
	    for (TableauPile t : tableau) {
	        for (Card c : t.getCards()) {
	            sb.append(c.isFaceUp() ? c.toString() : "XX").append(",");
	        }
	        sb.append("|");
	    }

	    // Waste (full order)
	    sb.append("W:[");
	    for (Card c : waste.getCards()) {
	        sb.append(c.toString()).append(",");
	    }
	    sb.append("]");

	    // Stock (full order)
	    sb.append("S:[");
	    for (Card c : stock.getCards()) {
	        sb.append(c.toString()).append(",");
	    }
	    sb.append("]");

	    return sb.toString();
	}



	private void printGameState() {
		System.out.println("\n=== Current Game State ===");

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
	}
}
