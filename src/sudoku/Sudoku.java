package sudoku;

import java.util.TreeMap;

import sudoku.solver.SudokuSolver;

/**
 * Modelleert een sudoku veld
 * @author Aeilko Bos
 */
public class Sudoku implements Cloneable {
	// Een lege sudoku in een dubbele short array
	public static final short[][] emptySudoku = new short[][]{{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0}};
	
	// De sudoku in een dubbele short array volgens waardes[col][row], met het hokje als waarde of 0 voor leeg.
	private short[][] values;
	
	
	// Constructor
	/**
	 * Maakt een nieuwe sodoku aan met de gegeven waardes.
	 * @param cols Dubbele short array met de getallen die in de sudoku staan
	 * @require cols.length == 9
	 * @require for(int i = 0; i < cols.length; i++) cols[i].length == 9
	 * @require for(int i = 0; i < cols.length; i++){ for(int j = 0; j < cols[i].length; j++){ cols[i][j] >= 0 && cols[i][j] <= 9 }}
	 */
	public Sudoku(short[][] cols){
		this.values = cols;
	}
	
	/**
	 * Maakt een lege sudoku aan
	 */
	public Sudoku(){
		this(Sudoku.emptySudoku);
	}
	
	
	// Commands
	/**
	 * Past een waarde in een sudoku aan
	 * @param col De kolom van het hokje dat aangepast moet worden.
	 * @param row De rij van het hokje dat aangepast moet worden.
	 * @param val De nieuwe waarde die in het hokje moet komen te staan (0 = leeg).
	 * @require col >= 0 && col <= 8
	 * @require row >= 0 && col <= 8
	 * @require val >= 0 && val <= 9
	 */
	public void setVal(short col, short row, short val){
		this.values[col][row] = val;
	}
	
	/**
	 * Leegt de sudoku
	 */
	public void empty(){
		this.values = Sudoku.emptySudoku;
	}

	
	// Queries
	/**
	 * @param col De kolom van het hokje
	 * @param row De rij van het hokje
	 * @return De waarde van een hokje
	 */
	public short getVal(short col, short row){
		return this.values[col][row];
	}
	
	
	// Override's
	@Override
	public String toString(){
		String result = "+-----+-----+-----+\n";
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				if(j%3 == 0){
					result = result + "|" + (this.values[i][j] != 0 ? this.values[i][j] : " ");
				}
				else{
					result = result + " " + (this.values[i][j] != 0 ? this.values[i][j] : " ");
				}
				
			}
			result = result + "|\n";
			
			if((i+1)%3 == 0){
				result = result + "+-----+-----+-----+\n";
			}
		}
		
		return result;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Sudoku){
			Sudoku s = (Sudoku) o;
			boolean result = true;
			for(short i = 0; i < 9; i++){
				for(short j = 0; j < 9; j++){
					if(this.values[i][j] != s.getVal(i, j)){
						result = false;
					}
				}
			}
			return result;
		}
		else{
			return false;
		}
	}
	
	@Override
	public Object clone(){
		try {
			// We are required to call this, but this does not clone it correctly
			super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		short[][] newS = new short[9][9];
		for(short i = 0; i < 9; i++){
			for(short j = 0; j < 9; j++){
				newS[i][j] = this.getVal(i, j);
			}
		}
		return new Sudoku(newS);
	}
	
	
	// Main method, for testing purposes
	public static void main(String[] args){
		// Sudoku's laden, 1 = simpel, 2 = easy, 3 = middel, 4 = hard
		short[][] sudoku1 = new short[][]{{0,0,0, 8,0,3, 0,9,0},{7,0,0, 0,0,2, 0,0,0},{0,0,1, 4,0,0, 2,0,0},{0,5,0, 0,2,0, 8,0,0},{1,0,0, 6,0,5, 0,0,0},{0,0,8, 0,0,0, 9,0,7},{0,0,3, 0,0,4, 0,1,0},{5,6,0, 7,0,9, 0,0,3},{0,0,0, 0,0,0, 7,0,0}};
		short[][] sudoku2 = new short[][]{{0,0,0, 0,0,0, 0,0,1},{0,0,0, 5,0,6, 4,2,0},{0,4,0, 8,0,0, 7,3,0},{8,0,9, 0,0,0, 0,1,0},{0,0,0, 0,0,0, 0,0,0},{0,0,7, 6,0,5, 3,0,8},{7,0,0, 0,0,0, 0,0,0},{0,0,5, 3,0,0, 9,0,0},{0,0,6, 0,9,0, 0,4,3}};
		short[][] sudoku3 = new short[][]{{0,0,0, 0,0,0, 0,0,0},{5,0,4, 7,0,8, 1,0,9},{0,0,0, 5,4,0, 0,0,7},{0,0,9, 0,0,0, 0,0,0},{8,0,0, 2,0,4, 7,5,0},{0,0,0, 0,5,7, 0,0,8},{0,0,7, 1,0,0, 9,0,0},{0,3,6, 9,0,0, 0,0,0},{0,0,0, 0,0,0, 3,0,0}};
		short[][] sudoku4 = new short[][]{{0,4,0, 8,0,0, 0,0,0},{0,0,0, 1,5,0, 0,0,0},{5,0,0, 0,9,7, 0,0,0},{0,0,0, 0,7,0, 8,0,3},{0,0,0, 4,0,8, 6,0,0},{7,0,8, 9,3,0, 0,0,0},{9,0,0, 0,0,0, 2,0,1},{0,7,0, 0,0,0, 0,6,4},{0,0,0, 3,4,0, 0,9,0}};
		
		TreeMap<String, Sudoku> sudokus = new TreeMap<String, Sudoku>();
		sudokus.put("1 - Simple", new Sudoku(sudoku1));
		sudokus.put("2 - Easy", new Sudoku(sudoku2));
		sudokus.put("3 - Middle", new Sudoku(sudoku3));
		sudokus.put("4 - Hard", new Sudoku(sudoku4));
		
		for(String name: sudokus.keySet()){
			Sudoku s = sudokus.get(name);
			SudokuSolver ss = new SudokuSolver(s);
			System.out.println("Sudoku " + name);
			if(ss.solve()){
				System.out.println("Opgelost!");
			}
			else{
				System.out.println("Niet opgelost");				
			}
			System.out.println("Tijd: " + ss.getTimeNeeded() + " seconden");
			System.out.println(ss);
		}
	}
}