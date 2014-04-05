//BackwardChaining Theorem prover for Propositional Logic
//By Zain Shamsi
//-------------------------------------

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;


public class BackChainer {

	public static void loadPropositionalFile(String filename){
		String token;
		try {
			Scanner filereader = new Scanner(new File(filename));
			while (filereader.hasNextLine()){
				token = filereader.nextLine();
				StringTokenizer st = new StringTokenizer(token);				
				if (st.countTokens() == 1){
					facts.add(st.nextToken());
				}
				else {
					Rule r = new Rule();
					boolean propsym = false;
					while (st.hasMoreTokens()){
						token  = st.nextToken();
						if (token.equals(">")) propsym = true;
						if (!token.equals("^") && !propsym)	r.literals.add(token);							
						if (!token.equals("^") && propsym) r.consequent = token;
					}
					rules.add(r);
				}
			}
			filereader.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found! Did you enter it correctly?");
		}
		
	}
	
	public static void showAll(){ 
		System.out.println("FACTS");
		for (String f: facts) System.out.println(f);	
		System.out.println("RULES");
		for (Rule r: rules) System.out.println(r);		
	}
	
	public static void clearAll(){
		facts.clear();
		rules.clear();
	}
	
	public static void query(String q) { 
		trace.clear();
		try {
			System.out.println(backwardChaining(q));
		}catch (StackOverflowError e){
			System.out.println("Query goes into infinite loop, try something else");
			trace.clear();
		}
	}
	
	public static boolean backwardChaining(String query) throws StackOverflowError{
		trace.add("QUERY >> " + query);
		
		//first check if query is a fact, then return true
		for (String fact: facts){
			if (query.equals(fact)){
				trace.add(fact + " is a fact");
				return true;
			}
		}
		
		//else go through rules and find all rules with consequent == query
		//then backward chain on their antecedents
		for (Rule r : rules){
			if (query.equals(r.consequent)){
				boolean ruleGood = true;
				trace.add("Using " + r);
				//check each literal in the rule
				for (String literal : r.literals){
					if (literal.contains("-")){
						//if negative literal, check negation-as-failure
						if (backwardChaining(literal.substring(1))){
							trace.add(literal + " = false");
							ruleGood = false;
						}
						else{ 
							trace.add(literal + " = true");
							trueonly.add(literal);
						}
					}
					else if (!backwardChaining(literal)){
						trace.add(literal + " = false");
						ruleGood = false;
					}
					else{
						trace.add(literal + " = true");
						trueonly.add(literal);
					}
				}
				//return true if rule satisfied, else look for other rules
				if (ruleGood) return ruleGood;
			}
		}
		
		//if no fact or rule, return false
		trace.add(query + " is not a fact and is not entailed");
		return false;
	}
	
	public static void tracelast() { 
		for (String s : trace) System.out.println(s);
	}	
	
	public static void tracetrue() {
		for (String s : trueonly) System.out.println(s);
	}
	
	public static ArrayList<String> trace = new ArrayList<String>();
	public static ArrayList<String> facts = new ArrayList<String>();
	public static ArrayList<Rule> rules = new ArrayList<Rule>();
	public static ArrayList<String> trueonly = new ArrayList<String>();
}
