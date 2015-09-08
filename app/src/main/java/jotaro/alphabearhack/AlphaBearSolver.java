package jotaro.alphabearhack;

import java.util.ArrayList;
import java.util.Scanner;

// AlphaBearSolver contains a dictionary, and can take a string of letter then produce
//     a list of words that contains the input letters
public class AlphaBearSolver {
	private ArrayList<LetterInventory> dict;
	
	// Construct AlphaBearSolver with a dictionary
	public AlphaBearSolver(Scanner dictionary) {
		dict = new ArrayList<LetterInventory>();
		while(dictionary.hasNextLine()){
			String current = dictionary.nextLine();
			dict.add(new LetterInventory(current));
		}
	}

	// Takes a string of letter and produce a list of words that made from the input letters
	// For example, input:"der", ouput:"red,..."
	public ArrayList<String> getWords(String input) {
		int max = 0;
		ArrayList<String> results = new ArrayList<String>();
		LetterInventory in = new LetterInventory(input);
		for(LetterInventory li:dict){			
			if(in.subtract(li) != null && li.getOriginal().length() >= max){
				max = li.getOriginal().length();
				results.add(li.getOriginal());
			}
		}
		java.util.Collections.sort(results, new StringLengthComparator(""));
		return results;
	}
}
