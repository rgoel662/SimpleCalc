import java.util.List;		// used by expression evaluator

/**
 *	<Description goes here>
 *
 *	@author	
 *	@since	
 */
public class SimpleCalc {
	
	private ExprUtils utils;	// expression utilities
	
	private ArrayStack<Double> valueStack;		// value stack
	private ArrayStack<String> operatorStack;	// operator stack

	// constructor	
	public SimpleCalc() {
		utils = new ExprUtils();
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
			} else if (!expr.equals("q")){
				double val = evaluateExpression(utils.tokenizeExpression(expr));
				System.out.println(val);
			}
		}while (!expr.equals("q"));
	}
	
	/**	Print help */
	public void printHelp() {
		System.out.println("Help:");
		System.out.println("  h - this message\n  q - quit\n");
		System.out.println("Expressions can contain:");
		System.out.println("  integers or decimal numbers");
		System.out.println("  arithmetic operators +, -, *, /, %, ^");
		System.out.println("  parentheses '(' and ')'");
	}
	
	/**
	 *	Evaluate expression and return the value
	 *	@param tokens	a List of String tokens making up an arithmetic expression
	 *	@return			a double value of the evaluated expression
	 */
	public double evaluateExpression(List<String> tokens) {
		double value = 0;
		for (int i = 0; i < tokens.size(); i ++){
			String t = tokens.get(i);
			if (t.charAt(0) >= '0' && t.charAt(0) <= '9'){
				valueStack.push(Double.parseDouble(t));
			}
			if (isOperator(t)){
				if (operatorStack.isEmpty()){
					operatorStack.push(t);
					continue;
				} else if (hasPrecedence(t, operatorStack.peek())){
					if (t.equals("(")){
						operatorStack.push(t);
					} else if (t.equals(")")){
						while(!operatorStack.peek().equals("(")){
							valueStack.push(executeOperation(valueStack.pop(), valueStack.pop(), operatorStack.pop()));
						}
						operatorStack.pop();
					} else {
						while (!operatorStack.isEmpty() && hasPrecedence(t, operatorStack.peek()) && !operatorStack.peek().equals("("))
							valueStack.push(executeOperation(valueStack.pop(), valueStack.pop(), operatorStack.pop()));
						operatorStack.push(t);
					}
					
				} else {
					operatorStack.push(t);
				}
			}
			if (i == tokens.size() -1){
				while(!operatorStack.isEmpty()){
					valueStack.push(executeOperation(valueStack.pop(), valueStack.pop(), operatorStack.pop()));
				}
			}
		}
		value = valueStack.peek();
		return value;
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
	 
}
