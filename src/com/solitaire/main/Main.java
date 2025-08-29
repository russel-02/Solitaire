package com.solitaire.main;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import com.solitaire.model.Card;
import com.solitaire.process.readFile;
import com.solitaire.game.SolitaireGame;

public class Main {
	private static int shuffleCount;
	private static int shuffleType;
	public static int DrawnCardsInput;
	private static List<Card> deck;
	private static boolean unreadable = true;

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		System.out.println("Welcome to Solitaire");

		while (true) {
			System.out.print("Choose between (1) Random shuffle or (2) Faro Shuffle: ");
			if (input.hasNextInt()) {
				shuffleType = input.nextInt();
				if (shuffleType == 1 || shuffleType == 2) {
					break;
				} else {
					System.out.println("Please enter a number between 1 or 2.");
				}
			} else {
				System.out.println("Invalid input. Please enter a number.");
				input.next();
			}
		}

		while (true) {
			System.out.print("Enter number of shuffles: ");
			if (input.hasNextInt()) {
				shuffleCount = input.nextInt();
				if (shuffleCount >= 0 && shuffleCount <= 100) {
					break;
				} else {
					System.out.println("Please enter a number from 0 to 100.");
				}
			} else {
				System.out.println("Invalid input. Please enter an integer.");
				input.next();
			}
		}
		
		while (true) {
			System.out.print("Choose between (1) or (3) Number of Drawn Cards from Stock Pile: ");
			if (input.hasNextInt()) {
				DrawnCardsInput = input.nextInt();
				if (DrawnCardsInput == 1 || DrawnCardsInput == 3) {
					break;
				} else {
					System.out.println("Please enter a number between 1 or 3.");
				}
			} else {
				System.out.println("Invalid input. Please enter a number.");
				input.next();
			}
		}
		input.nextLine();

		while (unreadable) {
			try {
				System.out.print("Enter deck file path: ");
				String path = input.nextLine();
				deck = readFile.readDeckFromFile(path);
				unreadable = false;
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}

		input.close();
		Collections.reverse(deck);
		SolitaireGame game = new SolitaireGame(deck, shuffleCount, shuffleType, DrawnCardsInput);
		game.playGame();

	}

}
