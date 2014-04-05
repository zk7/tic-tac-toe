//First Order Logic rules propositionalizer
//By Zain Shamsi
//-------------------------------------

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;


public class TicTacToe {

	public static void main(String[] args) {
		System.out.println("You have started Zain's Tic-Tac-Toe. Use help to get a command listing.");
		
		
		while (true){
			System.out.print(":>");
			String command = in.next();
			
			if (command.equals("load")) loadFOLFile(in.next());
			else if (command.equals("show")) BackChainer.showAll();
			else if (command.equals("clear")) BackChainer.clearAll();
			else if (command.equals("query")) BackChainer.query(in.next());			
			else if (command.equals("tracelast")) BackChainer.tracelast();
			else if (command.equals("tracetrue")) BackChainer.tracetrue();
			else if (command.equals("nextmove")) for (String p : nextMoves(in.next())) System.out.println(p);
			else if (command.equals("print")) printBoard();
			else if (command.equals("play")) play();
			else if (command.equals("help")){
				System.out.println("load <filename> - loads file with rules and board state\n" +
						"show - shows currently loaded rules\n" +
						"clear - clears loaded rules\n" +
						"query <symbol> - query a board place to see if true or false\n" +
						"nextmove <X/O> - queries all board places to see valid next moves\n" +
						"tracelast - traces last query result \n" +
						"print - print current board state \n" +
						"play - play a game! \n" +
						"help - this menu\n" +
						"quit - quit program\n");	
			}
			else if (command.equals("quit") || command.equals("exit")) return;
			else System.out.println("Invalid Command, try help for a listing");
			
		}		
	}

	public static void loadFOLFile(String filename){
		try {
			Scanner filereader = new Scanner(new File(filename));
			while (filereader.hasNextLine()){
				String line = filereader.nextLine();
				StringTokenizer st = new StringTokenizer(line);				
				if (st.countTokens() == 1){
					//fact
					String literal = st.nextToken();
					BackChainer.facts.add(proportionalize(literal));
				}
				else {
					String token = st.nextToken(" ^>");
					if (token.equals("domain")){
						//add domain for variable
						String var = st.nextToken();
						ArrayList<String> values = new ArrayList<String>();
						while (st.hasMoreTokens()){
							values.add(st.nextToken());
						}
						domains.put(var, values);
					}
					else {
						//we have a rule
						ArrayList<String> vars = new ArrayList<String>();
						if (token.equals("forall")){ //find out which vars we need
							token = st.nextToken(); //get first var							
							do {
								vars.add(token);
								token = st.nextToken(); //get additional vars
							}while (!token.equals(":"));
							token = st.nextToken(); //get first predicate
						}
						
						//now read the rule
						ArrayList<String> literals = new ArrayList<String>();
						literals.add(proportionalize(token));
						while (st.hasMoreTokens()) literals.add(proportionalize(st.nextToken()));
						
						createRules(vars, literals);
					}
				}
			}
			filereader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found! Did you enter it correctly?");
		} catch (Exception e) {
			System.out.println("Something is incorrect in the rules. Unable to load. Please check file.");
		}
		
	}

	public static String proportionalize(String literal){
		return literal.replaceAll("[(,]", "_").replace(")", "");
	}
	
	public static void createRules(List<String> vars, ArrayList<String> terms) throws Exception{
		if (vars.isEmpty()){
			//create the rule
			Rule r = new Rule();
			for (int i = 0; i < terms.size()-1; i++){
				r.literals.add(terms.get(i));
			}
			r.consequent = terms.get(terms.size()-1);
			BackChainer.rules.add(r);
			return;
		}
		for (String val : domains.get(vars.get(0))){
			ArrayList<String> newterms = new ArrayList<String>();
			for (String t : terms){
				newterms.add(t.replaceAll(vars.get(0), val));
			}
			createRules(vars.subList(1, vars.size()), newterms);
		}
	
	}
	
	public static ArrayList<String> nextMoves(String player){
		ArrayList<String> possibles = new ArrayList<String>();
		for (int i = 1; i <= 3; i++){
			for (int j = 1; j <= 3; j++){
				String query = "play_"+player+"_"+i+"_"+j;
				
				if (BackChainer.backwardChaining(query))
					possibles.add(query);
			}
		}
		return possibles;
	}
	
	public static void play(){
		System.out.println("Player will play as X");
		int result = 0;
		
		//clear board
		BackChainer.facts.clear();
		
		while (result == 0){
			
			//ask for user to enter move		
			boolean valid = false;
			int row = 0, col = 0;
			while (!valid){
				System.out.println("Enter move for X (row column): ");
				try {					
					row = in.nextInt();
					col = in.nextInt();				
				
					//check for valid move
					String queryX = "p_X_"+row+"_"+col;
					String queryO = "p_O_"+row+"_"+col;
					if (row < 1 || row > 3 || col < 1 || col > 3 ||
							BackChainer.backwardChaining(queryX) || BackChainer.backwardChaining(queryO))
						System.out.println("Invalid move! Try again");
					else valid = true;
				} catch (Exception e){
					System.out.println("Invalid input! Try again");
					in.nextLine();
				}
			}
			
			//add as fact to KB
			String fact = "p_X_"+row+"_"+col;
			BackChainer.facts.add(fact);
			printBoard();
			
			result = checkWinner();
			if (result != 0) break;
			
			//call nextmove to get AI move
			System.out.println("AI plays: ");
			ArrayList<String> moves = nextMoves("O");
			String AImove = moves.get(random.nextInt(moves.size())).replace("lay", "");
			
			//add as fact to KB
			BackChainer.facts.add(AImove);
			printBoard();
			
			result = checkWinner();
		}
		
		if (result == 1) System.out.println("You won!");
		if (result == 2) System.out.println("AI won!");
		if (result == 3) System.out.println("Stalemate!");
		System.out.println("Thanks for playing!");
	}
	
	public static int checkWinner(){
		//check rows
		for (int i = 1; i <= 3; i++){
			if (BackChainer.backwardChaining("p_X_"+i+"_1") && BackChainer.backwardChaining("p_X_"+i+"_2") && BackChainer.backwardChaining("p_X_"+i+"_3")) return 1;
			if (BackChainer.backwardChaining("p_O_"+i+"_1") && BackChainer.backwardChaining("p_O_"+i+"_2") && BackChainer.backwardChaining("p_O_"+i+"_3")) return 2;
		}
		
		//check columns
		for (int j = 1; j <= 3; j++){
			if (BackChainer.backwardChaining("p_X_1_"+j) && BackChainer.backwardChaining("p_X_2_"+j) && BackChainer.backwardChaining("p_X_3_"+j)) return 1;
			if (BackChainer.backwardChaining("p_O_1_"+j) && BackChainer.backwardChaining("p_O_2_"+j) && BackChainer.backwardChaining("p_O_3_"+j)) return 2;
		}
		
		//check diagonal left
		if (BackChainer.backwardChaining("p_X_1_1") && BackChainer.backwardChaining("p_X_2_2") && BackChainer.backwardChaining("p_X_3_3")) return 1;
		if (BackChainer.backwardChaining("p_O_1_1") && BackChainer.backwardChaining("p_O_2_2") && BackChainer.backwardChaining("p_O_3_3")) return 2;
		//check diagonal right
		if (BackChainer.backwardChaining("p_X_1_3") && BackChainer.backwardChaining("p_X_2_2") && BackChainer.backwardChaining("p_X_3_1")) return 1;
		if (BackChainer.backwardChaining("p_O_1_3") && BackChainer.backwardChaining("p_O_2_2") && BackChainer.backwardChaining("p_O_3_1")) return 2;
	
		//check stalemate
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				String queryX = "p_X_"+(i+1)+"_"+(j+1);
				String queryO = "p_O_"+(i+1)+"_"+(j+1);
				boolean x = BackChainer.backwardChaining(queryX);
				boolean o = BackChainer.backwardChaining(queryO);
				if (!x && !o) return 0; //there is an empty spot
			}
		}		
		return 3; //no empty spots
	}
	
	public static void printBoard(){
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				String queryX = "p_X_"+(i+1)+"_"+(j+1);
				String queryO = "p_O_"+(i+1)+"_"+(j+1);
				try{
					boolean x = BackChainer.backwardChaining(queryX);
					boolean o = BackChainer.backwardChaining(queryO);
					if (x) System.out.print("X");
					else if (o) System.out.print("O");
					else System.out.print(" ");
				} catch(Exception e){
					System.out.println("An error occured on board print");
					return;
				}
				if (j < 2) System.out.print("|");
			}
			System.out.println();
			if (i < 2) System.out.println("-----");
		}
	}	
	
	public static Scanner in = new Scanner(System.in);
	public static Random random = new Random();
	public static HashMap<String, ArrayList<String>> domains = new HashMap<String, ArrayList<String>>();
}
