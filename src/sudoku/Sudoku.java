package sudoku;

import java.util.TreeMap;

import sudoku.solver.SudokuSolver;

/**
 * A game of Sudoku
 * @author Aeilko Bos
 */
public class Sudoku implements Cloneable {
	// An empty sudoku field
	public static final short[][] emptySudoku = new short[][]{{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0},{0,0,0, 0,0,0, 0,0,0}};

	// The value of the fields in this sudoku, using 0 for an empty field.
	// The double array uses this structure: values[col][row]
	private short[][] values;
	


	// Constructor
	/**
	 * Creates a new Sudoku which contains the values of the given double arrays
	 * @param cols Short double array which contain the values of the Sudoku fields, use 0 for an empty field.
	 * @require cols.length == 9
	 * @require for(int i = 0; i < cols.length; i++) cols[i].length == 9
	 * @require for(int i = 0; i < cols.length; i++){ for(int j = 0; j < cols[i].length; j++){ cols[i][j] >= 0 && cols[i][j] <= 9 }}
	 */
	public Sudoku(short[][] cols){
		this.values = cols;
	}
	
	/**
	 * Creates an empty Sudoku
	 */
	public Sudoku(){
		this(Sudoku.emptySudoku);
	}
	


	// Commands
	/**
	 * Sets the value of a field in the Sudoku
	 * @param row The row of the field which is set
	 * @param col The column of the field which is set
	 * @param val The new value of the specified field, use 0 for empty
	 * @require col >= 0 && col <= 8
	 * @require row >= 0 && col <= 8
	 * @require val >= 0 && val <= 9
	 */
	public void setVal(short row, short col, short val){
		this.values[row][col] = val;
	}
	
	/**
	 * Empty this sudoku
	 */
	public void empty(){
		this.values = Sudoku.emptySudoku;
	}


	
	// Queries
	/**
	 * Returns the value of the given field
	 * @param row The row of the field
	 * @param col The column of the field
	 * @return The value of the requested field
	 */
	public short getVal(short row, short col){
		return this.values[row][col];
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
		catch (CloneNotSupportedException e) { }

		short[][] newS = new short[9][9];
		for(short i = 0; i < 9; i++){
			for(short j = 0; j < 9; j++){
				newS[i][j] = this.getVal(i, j);
			}
		}
		return new Sudoku(newS);
	}
}