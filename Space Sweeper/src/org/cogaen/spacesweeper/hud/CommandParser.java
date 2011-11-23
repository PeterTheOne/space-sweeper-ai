package org.cogaen.spacesweeper.hud;

public class CommandParser {

	public static enum Token {STRING, DOUBLE, INT, BOOLEAN, EOC};
	
	private int idx;
	private int iValue;
	private double dValue;
	private boolean bValue;
	private int ch = 0;
	private String command;
	private StringBuffer str = new StringBuffer();;
	private Token token;
	
	public void parse(String cmd) {
		this.command = cmd;
		this.idx = 0;
		nextChar();
		nextToken();
	}
	
	public Token getToken() {
		return this.token;
	}
	
	public String getString() {
		return this.str.toString();
	}
	
	public void nextToken() {
		this.str.setLength(0);
		
		while (this.ch == ' ') {
			nextChar();
		}
		
		if (this.ch == -1) {
			this.token = Token.EOC;
			return;
		}
		
		if (isDigit(this.ch)) {
			parseInt();
			
			if (this.ch == '.') {
				parseDouble();
				this.token = Token.DOUBLE;
				return;
			} else {
				this.token = Token.INT;
				return;
			}
		}

		this.str.append(this.ch);
		nextChar();
		while (this.ch != -1 && this.ch != ' ') {
			this.str.append(this.ch);
			nextChar();
		}
		this.token = Token.STRING;
 	}
	
	private void parseDouble() {
		this.dValue = this.iValue;
		while (isDigit(nextChar())) {
			this.dValue += (this.ch - '0') / 10.0;
		}
	}

	private int nextChar() {
		if (this.idx < this.command.length()) {
			this.ch = this.command.charAt(this.idx++);
		} else {
			this.ch = -1;
		}
		
		return this.ch;
	}
	
	private void parseInt() {
		this.iValue = this.ch - '0';
		while (isDigit(nextChar())) {
			this.iValue *= 10;
			this.iValue += this.ch - '0';
		}
	}

	public boolean isDigit(int ch) {
		return ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5' || ch == '6' || ch == '7' 
				|| ch == '8' || ch == '9'; 
	}
	
	public double getDoubleValue() {
		return this.dValue;
	}
	
	public boolean getBooleanValue() {
		return this.bValue;
	}
	
}
