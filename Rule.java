import java.util.ArrayList;


public class Rule {
	public String consequent;
	public ArrayList<String> literals = new ArrayList<String>();
	
	public String toString(){
		StringBuffer ret = new StringBuffer(literals.get(0));
		for (int i = 1; i < literals.size(); i++) ret.append(" ^ " + literals.get(i));
		ret.append(" > " + consequent);
		return ret.toString();
	}
}
