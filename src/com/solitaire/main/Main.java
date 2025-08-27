package com.solitaire.main;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.solitaire.model.Card;
import com.solitaire.model.Rank;
import com.solitaire.model.Suit;
import com.solitaire.process.readFile;
import com.solitaire.game.SolitaireGame;




public class Main {
	private static int shuffleCount;
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		
		System.out.println("Welcome to Solitaire");
		
	      while (true) {
	            System.out.print("Enter number of faro shuffles: ");
	            if (input.hasNextInt()) {
	            	shuffleCount = input.nextInt();
	                if (shuffleCount >= 0 && shuffleCount <= 100) {
	                	shuffleCount %= 8;
	                    break; 
	                } else {
	                    System.out.println("Please enter a number from 0 to 100.");
	                }
	            } else {
	                System.out.println("Invalid input. Please enter an integer.");
	                input.next(); 
	            }
	        }
	        input.nextLine();
	        
	        System.out.print("Input File path: ");
	        String Readfile = input.nextLine();
	        List<Card> deck = readFile.readDeckFromFile(Readfile);
	        input.close();


	   
	        Collections.reverse(deck);
	        
	        SolitaireGame game = new SolitaireGame(deck, shuffleCount);
	        game.playGame();

	}

}
