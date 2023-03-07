import java.util.List;		// used by expression evaluator
import java.util.ArrayList;

/**
 *	Performs simple calculator functions
 *
 *	@author	Rishabh Goel
 *	@since	3/5/23
 */
public class SimpleCalc {
	
	private ExprUtils utils;	// expression utilities
	
	private ArrayStack<Double> valueStack;		// value stack
	private ArrayStack<String> operatorStack;	// operator stack
	private List<String> names;		// names of all variables
	private List<Double> vals;		// values of all variables

	// constructor	
	public SimpleCalc() {
		utils = new ExprUtils();
		names = new ArrayList<String>();
		vals = new ArrayList<Double>();
		names.add("e"); names.add("pi");
		vals.add(Math.E); vals.add(Math.PI);
	}
	
	public static void main(String[] args) {
		SimpleCalc sc = new SimpleCalc();
		sc.run();
	}
	
	public void run() {
		System.out.println("\nWelcome to SimpleCalc!!!\n");
		runCalc();
		System.out.println("\nThanks for using SimpleCalc! Goodbye.\n");
	}
	
	/**
	 *	Prompt the user for expressions, run the expression evaluator,
	 *	and display the answer.
	 */
	public void runCalc() {
		String expr = "";
		do{
			valueStack = new ArrayStack<Double>();
			operatorStack = new ArrayStack<String>();
			expr = Prompt.getString("");
			if (expr.equals("h")){
				printHelp();
			} else if (expr.equals("l")){
				printVarList();
			} else if (!expr.equals("q")){
				double val = evaluateExpression(utils.tokenizeExpression(expr));
				System.out.println(val);
			}
		}while (!expr.equals("q"));
	}
	
	/**	Print help */
	public void printHelp() {
		System.out.println("\nHelp:");
		System.out.println("  h - this message\n  q - quit\n");
		System.out.println("Expressions can contain:");
		System.out.println("  integers or decimal numbers");
		System.out.println("  arithmetic operators +, -, *, /, %, ^");
		System.out.println("  parentheses '(' and ')'\n");
	}
	
	public void printVarList(){
		System.out.println("\nVariables:");
		for ( int i = 0 ; i < vals.size(); i ++){
			System.out.printf("%4s%-20s=%15.2f\n", "", names.get(i), vals.get(i));
		}
		System.out.println();
	}
	
	/**
	 *	Evaluate expression and return the value
	 *	@param tokens	a List of String tokens making up an arithmetic expression
	 *	@return			a double value of the evaluated expression
	 */
	public double evaluateExpression(List<String> tokens) {

		//deal with variable assignment
		if (tokens.size() > 1){
			boolean isAssignment = false;
			for (int i = 0; i < tokens.size(); i++){
				if (tokens.get(i).equals("=") && i != 1)
					return 0.0;
				else if (tokens.get(i).equals("="))
					isAssignment = true;
			}
			if (isAssignment){
				for (int i = 0; i < tokens.get(0).length(); i++){
					if (!Character.isAlphabetic(tokens.get(0).charAt(i)))
						return 0.0;
				}
				if (isVariable(tokens.get(0))){
					int idx;
					boolean stop = false;
					for (idx = 0; idx < names.size() && !stop; idx++){
						if (names.get(idx).equals(tokens.get(0))){
							stop = true;
						}
					}
					idx--;
					vals.set(idx, evaluateExpression(tokens.subList(2, tokens.size())));
					System.out.print(tokens.get(0) + " = ");
					return vals.get(idx);
				} else {
					names.add(tokens.get(0));
					System.out.print(tokens.get(0) + " = ");
					vals.add(evaluateExpression(tokens.subList(2, tokens.size())));
					return vals.get(vals.size() - 1);
				}
			}
		}

		for (int i = 0; i < tokens.size(); i ++){
			String t = tokens.get(i);

			//always push operand
			if (Character.isDigit(t.charAt(0)) || isVariable(t)){
				if (isVariable(t)){
					int idx;
					boolean stop = false;
					for (idx = 0; idx < names.size() && !stop; idx++){
						if (names.get(idx).equals(t)){
							stop = true;
						}
					}
					idx--;
					valueStack.push(vals.get(idx));
				} else 
					valueStack.push(Double.parseDouble(t));
			} else if (!isOperator(t))
				valueStack.push(0.0);
			
			if (isOperator(t)){
				
				//always push first operator
				if (operatorStack.isEmpty()){
					operatorStack.push(t);
					continue;
				} else if (hasPrecedence(t, operatorStack.peek())){
					
					//always push left parenthesis
					if (t.equals("(")){
						operatorStack.push(t);
					} else if (t.equals(")")){ //evaluate all ops until left parenthesis
						while(!operatorStack.peek().equals("(")){
							valueStack.push(executeOperation(valueStack.pop(), valueStack.pop(), operatorStack.pop()));
						}
						operatorStack.pop();
					} else { //evaluate all ops until you reach an operator that has higher / same precedence
						while (!operatorStack.isEmpty() && hasPrecedence(t, operatorStack.peek()) && !operatorStack.peek().equals("("))
							valueStack.push(executeOperation(valueStack.pop(), valueStack.pop(), operatorStack.pop()));
						operatorStack.push(t);
					}
					
				} else { // if it has higher precedence, just push it
					operatorStack.push(t);
				}
			}
			
			// if at the end, evaluate all values/operators left
			if (i == tokens.size() -1){
				while(!operatorStack.isEmpty()){
					valueStack.push(executeOperation(valueStack.pop(), valueStack.pop(), operatorStack.pop()));
				}
			}
		}
		return valueStack.peek();
	}
	
	/**
	 *	Precedence of operators
	 *	@param op1	operator 1
	 *	@param op2	operator 2
	 *	@return		true if op2 has higher or same precedence as op1; false otherwise
	 *	Algorithm:
	 *		if op1 is exponent, then false
	 *		if op2 is either left or right parenthesis, then false
	 *		if op1 is multiplication or division or modulus and 
	 *				op2 is addition or subtraction, then false
	 *		otherwise true
	 */
	private boolean hasPrecedence(String op1, String op2) {
		if (op1.equals("^")) return false;
		if (op2.equals("(") || op2.equals(")")) return false;
		if ((op1.equals("*") || op1.equals("/") || op1.equals("%")) 
				&& (op2.equals("+") || op2.equals("-")))
			return false;
		return true;
	}
	
	/**	Determine if character is valid arithmetic operator including parentheses
	 *	@param str	the character to check
	 *	@return		true if the character is '+', '-', '*', '/', '^', '=','(', or ')'
	 */
	private boolean isOperator(String str){
		if (isBinaryOperator(str.charAt(0)) || str.charAt(0) == '(' || str.charAt(0) == ')')
			return true;
		return false;
	}
	
	/**	Determine if character is valid binary arithmetic operator excluding parentheses
	 *	@param c	the character to check
	 *	@return		true if the character is '+', '-', '*', '/', '^', or '='
	 */
	private boolean isBinaryOperator(char c) {
		switch (c) {
			case '+': case '-': case '*': case '/': 
			case '%': case '=': case '^':
				return true;
		}
		return false;
	}
	
	/**
	 * Executes one mathematical operation
	 * 
	 * @param op1 	The operand on the right side
	 * @param op2	The operand on the left side
	 * @param op	The operator
	 * @return		The completed mathematical operation (sum for "+", difference for "-", etc.)
	 */
	private double executeOperation(double op1, double op2, String op){
		if (op.equals("+"))
			return op2 + op1;
		else if (op.equals("-"))
			return op2 - op1;
		else if (op.equals("*"))
			return op2 * op1;
		else if (op.equals("/"))
			return op2 / op1;
		else if (op.equals("%"))
			return op2 % op1;
		else
			return Math.pow(op2, op1);
	}

	private boolean isVariable(String str){
		for (int i = 0; i < names.size(); i ++){
			if (str.equals(names.get(i)))
				return true;
		}
		return false;
	}
	 
}