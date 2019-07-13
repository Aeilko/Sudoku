package sudoku.solver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import java.util.function.Function;

import sudoku.Sudoku;
import sudoku.Coordinate;

/**
 * A solver for Sudoku puzzles
 * @author Aeilko Bos
 */
public class SudokuSolver {
	// The Sudoku which is being solved
	private Sudoku sudoku;
	// The Sudoku like it was at the start
	private Sudoku startSudoku;
	// Wheter or not the Sudoku is solved
	private boolean solved;
	
	// Possibilities per field
	// fieldPossible uses the Coordinate(row, col)
	private TreeMap<Coordinate, HashSet<Integer>> fieldPossible;
	
	// Keep track of how long the solver takes.
	private long startTime;
	private long stopTime;


	
	// Constructor
	/**
	 * Creates a new SudokuSolves based on a Sudoku object
	 * @param s The Sudoku to be solved.
	 */
	public SudokuSolver(Sudoku s){
		this.sudoku = (Sudoku) s.clone();
		this.startSudoku = s;
		this.solved = false;
		
		this.fieldPossible = new TreeMap<>();
		
		this.startTime = -1;
		this.stopTime = -1;
		
		this.checkSolved();
	}
	
	/**
	 * Creates a new SudokuSolver based on an double array representation of the Sudoku ([col][row])
	 * @param s The dubble short array.
	 * @require s.length == 9
	 * @require for(short i = 0; i < 9; i++){ s[i].length == 9
	 * @require for(short i = 0; i < 9; i++){ for(short j = 0; j < 9; j++){ s[i][j] >= 0 && s[i][j] <= 9 }}
	 */
	public SudokuSolver(short[][] s){
		this(new Sudoku(s));
	}
	


	// Commands
	/**
	 * Attempts to solve the Sudoku
	 * @return Wheter or not the Soduku is solved.
	 */
	public boolean solve(){
		this.prepareSolve();

		this.startTime = System.nanoTime();
		// Attempt the solving tricks at most 81 times.
		for(int i = 0; i < 81 && !this.solved; i++){
			System.out.println("New orbit");
			// Save the current state of the Sudoku so we can check wheter anything changes.
			Sudoku oldSudoku = (Sudoku) this.sudoku.clone();

			// Fill every field which only has one possibility
			this.singlePossibility();

			// Check if there is a value which is only possible on one field in a group.
			this.attemptOnAllGroups(e -> this.singles(e));
			
			// Only attempt these methods when nothing has changed yet, since these are expensive
			if(this.sudoku.equals(oldSudoku)){
				this.attemptOnAllGroups(e -> this.twins(e));

				if(this.sudoku.equals(oldSudoku)){
					this.attemptOnAllGroups(e -> this.slings(e));
				}
			}

			this.checkSolved();
		}

		this.stopTime = System.nanoTime();;
		return this.isSolved();
	}



	// Queries
	/**
	 * @return Wheter or not the Sudoku is solved
	 */
	public boolean isSolved(){
		return this.solved;
	}



	// Helper methods
	/**
	 * Prepares the class to solve the Sudoku.
	 * Generates the list of possibilities for every field
	 */
	private void prepareSolve(){
		// A list which includes all posibilities.
		HashSet<Integer> emptyList = new HashSet<>();
		for(int i = 1; i <= 9; i++){
			emptyList.add(i);
		}
		
		// Set all possibility lists with all the values
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				this.fieldPossible.put(new Coordinate(i, j), new HashSet<>(emptyList));
			}
		}
		
		// Check all fields, remove possibilities
		for(short row = 0; row < 9; row++){
			for(short col = 0; col < 9; col++){
				short val = this.sudoku.getVal(row, col);
				if(val != 0){
					// If the field is not empty we set the value in the solver, which removes the possibilities of related fields.
					this.setValue(row, col, val);
				}
			}
		}
	}

	/**
	 * Updates the value of a field for the solver, which also updates the possibilities of related fields.
	 * @param row The row of the field
	 * @param col The column of the field
	 * @param val The new value of the field
	 * @require col >= 0 && col <= 8
	 * @require row >= 0 && row <= 8
	 * @require val >= 1 && val <= 9
	 */
	private void setValue(short row, short col, short val){
		this.fieldPossible.put(new Coordinate(row, col), new HashSet<>());
		
		// Remove possibility for every field in this row and col
		for(int i = 0; i < 9; i++){
			//System.out.println("Possible pre (" + row + ", " + col + "): " + this.fieldPossible.get(new Coordinate(row, i)));
			this.fieldPossible.get(new Coordinate(row, i)).remove((int) val);
			this.fieldPossible.get(new Coordinate(i, col)).remove((int) val);
			//System.out.println("Possible post (" + row + ", " + col + "): " + this.fieldPossible.get(new Coordinate(row, i)));

		}
		
		// Remove possibility for every field in this block
		int block = this.coordsToBlock(row, col);
		int colStart = (block%3)*3;
		int rowStart = (block/3)*3;
		//System.out.println("(" + row + ", " + col + "), block: " + block + ", (" + rowStart + ", " + colStart + ")");
		for(int r = rowStart; r < rowStart+3; r++){
			for(int c = colStart; c < colStart+3; c++){
				this.fieldPossible.get(new Coordinate(r, c)).remove((int) val);
			}
		}

		// Set the value to the Sudoku object
		this.sudoku.setVal(row, col, val);
	}
	
	/**
	 * Controleert of de sudoku is opgelost
	 */
	private void checkSolved(){
		boolean result = true;
		for(short row = 0; row < 9 && result; row++){
			for(short col = 0; col < 9 && result; col++){
				if(this.sudoku.getVal(row, col) == 0){
					result = false;
				}
			}
		}
		this.solved = result;
	}

	/**
	 * Translates coordinates to the block ID, starting from the top left with 0, left to right, top to bottom
	 * @param row The row of the field
	 * @param col The column of the field
	 * @return The ID of the block the field is in.
	 * @require col >= 0 && col <= 8
	 * @require row >= 0 && row <= 8
	 */
	private int coordsToBlock(int row, int col){
		return (3*(row/3))+(col/3);
	}
	
	/**
	 * Returns the time the solver needed to solve the Sudoku, or -1 if the solver isn't done solving yet.
	 * @return The time in seconds.
	 */
	public double getTimeNeeded(){
		if(startTime == -1 || stopTime == -1){
			return -1;
		}
		else{
			return (stopTime-startTime)/((double) 1000000000);
		}
	}
	


	// Solve methods
	/**
	 * Checks if there are any fields which have only one possibility, so we can set the value.
	 */
	private void singlePossibility(){
		for(short row = 0; row < 9; row++){
			for(short col = 0; col < 9; col++){
				HashSet<Integer> tmp = this.fieldPossible.get(new Coordinate(row, col));
				if(tmp.size() == 1){
					Object[] t = tmp.toArray();
					int v = (int) t[0];
					short val = (short) v;
					this.setValue(row, col, val);
				}
			}
		}
	}

	/**
	 * Applies the given solver method to all possible groups in the Sudoku
	 * @param solver The solver which will be applied to field group
	 */
	private void attemptOnAllGroups(Function<HashMap<Integer, HashSet<Integer>>, HashMap<Integer, HashSet<Integer>>> solver){
		HashMap<Integer, HashSet<Integer>> possibilities;

		// Perform the solver on all rows
		for(int i = 0; i < 9; i++){
			possibilities = this.getRowPossibilities(i);
			possibilities = solver.apply(possibilities);
			this.setRowPossibilities(i, possibilities);
		}

		// Perform the solver on all cols
		for(int i = 0; i < 9; i++) {
			possibilities = this.getColPossibilities(i);
			possibilities = solver.apply(possibilities);
			this.setColPossibilities(i, possibilities);
		}

		// Perform the solver on all blocks
		for(int i = 0; i < 9; i++){
			possibilities = this.getBlockPossibilities(i);
			possibilities = solver.apply(possibilities);
			this.setBlockPossibilities(i, possibilities);
		}
	}

	/**
	 * Check whether a value is only possible on one place in the field group
	 */
	private HashMap<Integer, HashSet<Integer>> singles(HashMap<Integer, HashSet<Integer>> poss) {
		// Count the occurrences of each value
		int[] occurrences = new int[10];
		for (int field : poss.keySet()) {
			HashSet<Integer> p = poss.get(field);
			for (int num : p) {
				occurrences[num]++;
			}
		}

		// Check if there are any values which only occur once.
		for (int val = 1; val <= 9; val++) {
			if (occurrences[val] == 1) {
				// This value only occurs once, so we have to find in which field it occurs and remove the rest of the possibilities.
				for (int field : poss.keySet()) {
					if (poss.get(field).contains(val)) {
						HashSet<Integer> tmp = new HashSet<>();
						tmp.add(val);
						poss.put(field, tmp);
					}
				}
			}
		}

		return poss;
	}

	/**
	 * Checks wheter there are any combination of 2 values which can only occur in the same 2 fields.
	 * If so we can remove all other possibilities in these 2 fields.
	 */
	private HashMap<Integer, HashSet<Integer>> twins(HashMap<Integer, HashSet<Integer>> poss) {
		// Count the occurrences of each value
		int[] occurrences = new int[10];
		for (int field : poss.keySet()) {
			HashSet<Integer> p = poss.get(field);
			for (int num : p) {
				occurrences[num]++;
			}
		}

		// Find the values with two occurrences
		HashSet<Integer> twices = new HashSet<Integer>();
		for(int val = 1; val <= 9; val++){
			if(occurrences[val] == 2)
				twices.add(val);
		}

		// Collect the fields in which they occur
		HashMap<Integer, ArrayList<Integer>> fields = new HashMap<>();
		for(int field: poss.keySet()){
			for(int val: twices){
				if(poss.get(field).contains(val)){
					if(fields.containsKey(val)){
						fields.get(val).add(field);
					}
					else{
						ArrayList<Integer> tmp = new ArrayList<>();
						tmp.add(field);
						fields.put(val, tmp);
					}
				}
			}
		}

		// Check if any of the twices have the same fields as possibilities
		for(int val: fields.keySet()){
			for(int twinVal: fields.keySet()){
				if(val != twinVal){
					if(fields.get(val).equals(fields.get(twinVal))){
						// They match, remove all other possibilities from these fields.
						HashSet<Integer> tmp = new HashSet<>();
						tmp.add(val);
						tmp.add(twinVal);
						HashSet<Integer> tmp2 = new HashSet<>();
						tmp2.add(val);
						tmp2.add(twinVal);

						poss.put(fields.get(val).get(0), tmp);
						poss.put(fields.get(val).get(1), tmp2);
					}
				}
			}
		}

		return poss;
	}

	/***
	 * Checks if it can find a sling of fields which have to contain a certain series of numbers.
	 * Example: 4 fields, possibilities (8,3),(3,6),(6,8),(9,3). The first 3 columns must contain 3,6 and 8.
	 * 			So now we can remove 3 as a possibility from the 4th field, and therefore it has to be 9.
	 */
	private HashMap<Integer, HashSet<Integer>> slings(HashMap<Integer, HashSet<Integer>> poss){
		System.out.println("Sling start");
		ArrayList<Integer> emptyFields = new ArrayList<>();
		// Find the fields which are still empty
		for(int k: poss.keySet()){
			if(poss.get(k).size() != 0)
				emptyFields.add(k);
		}

		// No we have to find x amount of fields, which can only contain x amount of values.
		// Than we can remove these x values from all the other cells.
		for(int i = 2; i < emptyFields.size(); i++) {
			// Check each combination of fields.
			HashMap<HashSet<Integer>, HashSet<Integer>> slingFields = this.slingsRecursive(i, emptyFields, new HashSet<Integer>(), new HashSet<Integer>(), poss);

			// Remove the possibilities of sling fields.
			for (HashSet<Integer> indices : slingFields.keySet()) {
				HashSet<Integer> possValues = slingFields.get(indices);
				if (possValues.size() == i) {
					for (int x = 0; x < emptyFields.size(); x++) {
						if (!indices.contains(emptyFields.get(x))) {
							HashSet<Integer> tmp = poss.get(emptyFields.get(x));
							tmp.removeAll(possValues);
							poss.put(emptyFields.get(x), tmp);
						}
					}
				}
			}
		}
		
		return poss;
	}

	private HashMap<HashSet<Integer>, HashSet<Integer>> slingsRecursive(int remainingI, ArrayList<Integer> remainingEmptyFields, HashSet<Integer> curIndices, HashSet<Integer> curPoss, HashMap<Integer, HashSet<Integer>> poss){
		HashMap<HashSet<Integer>, HashSet<Integer>> result = new HashMap<>();

		// Copy remaining fields so we can safely remove values from it while looping over the original.
		ArrayList<Integer> copyREF = new ArrayList<>(remainingEmptyFields);

		for(int j = 0; j <= remainingEmptyFields.size()-remainingI; j++){
			// Copy the "current" fields
			HashSet<Integer> copyCI = new HashSet<>(curIndices);
			HashSet<Integer> copyCP = new HashSet<>(curPoss);
			// Add the values of this field to the "current" fields
			copyCI.add(remainingEmptyFields.get(j));
			copyCP.addAll(poss.get(remainingEmptyFields.get(j)));

			if(remainingI == 1){
				// This was the last field, craete result and return it.
				result.put(copyCI, copyCP);
			}
			else{
				// Prepare the remaining values and call the recursive method.
				copyREF.remove(0);
				result.putAll(this.slingsRecursive(remainingI-1, copyREF, copyCI, copyCP, poss));
			}
		}

		return result;
	}


	/**
	 * Returns the possibilities of all values in the given column
	 */
	private HashMap<Integer, HashSet<Integer>> getColPossibilities(int col){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		for(int row = 0; row < 9; row++)
			result.put(row, this.fieldPossible.get(new Coordinate(row, col)));

		return result;
	}

	/**
	 * Saves the given values to the correct fields of the given column
	 */
	private void setColPossibilities(int col, HashMap<Integer, HashSet<Integer>> poss){
		for(int row = 0; row < 9; row++)
			this.fieldPossible.put(new Coordinate(row, col), poss.get(row));
	}

	/**
	 * Returns the possibilities of all values in the given row
	 */
	private HashMap<Integer, HashSet<Integer>> getRowPossibilities(int row){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		for(int col = 0; col < 9; col++)
			result.put(col, this.fieldPossible.get(new Coordinate(row, col)));

		return result;
	}

	/**
	 * Saves the given values to the correct fields of the given row
	 */
	private void setRowPossibilities(int row, HashMap<Integer, HashSet<Integer>> poss){
		for(int col = 0; col < 9; col++)
			this.fieldPossible.put(new Coordinate(row, col), poss.get(col));
	}

	/**
	 * Returns the possibilities of all values in the given block
	 */
	private HashMap<Integer, HashSet<Integer>> getBlockPossibilities(int block){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		int i = 0;
		for(int row = (block/3)*3; row < ((block/3)+1)*3; row++){
			for(int col = (block%3)*3; col < ((block%3)+1)*3; col++){
				result.put(i, this.fieldPossible.get(new Coordinate(row, col)));
				i++;
			}
		}

		return result;
	}

	/**
	 * Saves the given values to the correct fields of the given block
	 */
	private void setBlockPossibilities(int block, HashMap<Integer, HashSet<Integer>> poss){
		int i = 0;
		for(int row = (block/3)*3; row < ((block/3)+1)*3; row++){
			for(int col = (block%3)*3; col < ((block%3)+1)*3; col++) {
				this.fieldPossible.put(new Coordinate(row, col), poss.get(i));
				i++;
			}
		}
	}



	// Overrides
	@Override
	public String toString(){
		String result = "";
		String[] start = this.startSudoku.toString().split("\\r?\\n");
		String[] finished = this.sudoku.toString().split("\\r?\\n");
		for(int i = 0; i < start.length; i++){
			result = result + start[i] + "   " + finished[i] + "\n";
		}
		return result;
	}



	// Main method, for testing purposes
	public static void main(String[] args){
		// Some example sudoku's, 1 = simple, 2 = easy, 3 = middle, 4 = hard, 5 = a 6star puzzle from a book
		short[][] sudoku1 = new short[][]{{0,0,0, 8,0,3, 0,9,0},{7,0,0, 0,0,2, 0,0,0},{0,0,1, 4,0,0, 2,0,0},{0,5,0, 0,2,0, 8,0,0},{1,0,0, 6,0,5, 0,0,0},{0,0,8, 0,0,0, 9,0,7},{0,0,3, 0,0,4, 0,1,0},{5,6,0, 7,0,9, 0,0,3},{0,0,0, 0,0,0, 7,0,0}};
		short[][] sudoku2 = new short[][]{{0,0,0, 0,0,0, 0,0,1},{0,0,0, 5,0,6, 4,2,0},{0,4,0, 8,0,0, 7,3,0},{8,0,9, 0,0,0, 0,1,0},{0,0,0, 0,0,0, 0,0,0},{0,0,7, 6,0,5, 3,0,8},{7,0,0, 0,0,0, 0,0,0},{0,0,5, 3,0,0, 9,0,0},{0,0,6, 0,9,0, 0,4,3}};
		short[][] sudoku3 = new short[][]{{0,0,0, 0,0,0, 0,0,0},{5,0,4, 7,0,8, 1,0,9},{0,0,0, 5,4,0, 0,0,7},{0,0,9, 0,0,0, 0,0,0},{8,0,0, 2,0,4, 7,5,0},{0,0,0, 0,5,7, 0,0,8},{0,0,7, 1,0,0, 9,0,0},{0,3,6, 9,0,0, 0,0,0},{0,0,0, 0,0,0, 3,0,0}};
		short[][] sudoku4 = new short[][]{{0,4,0, 8,0,0, 0,0,0},{0,0,0, 1,5,0, 0,0,0},{5,0,0, 0,9,7, 0,0,0},{0,0,0, 0,7,0, 8,0,3},{0,0,0, 4,0,8, 6,0,0},{7,0,8, 9,3,0, 0,0,0},{9,0,0, 0,0,0, 2,0,1},{0,7,0, 0,0,0, 0,6,4},{0,0,0, 3,4,0, 0,9,0}};
		short[][] sudoku5 = new short[][]{{0,0,7, 4,0,0, 0,1,0},{0,0,0, 0,8,6, 0,2,0},{3,8,0, 0,0,2, 0,6,0},{0,0,0, 0,5,9, 0,4,0},{1,4,0, 0,0,0, 0,8,6},{0,7,0, 6,4,0, 0,0,0},{0,2,0, 8,0,0, 0,5,1},{0,5,0, 2,7,0, 0,0,0},{0,1,0, 0,0,5, 4,0,0}};

		TreeMap<String, Sudoku> sudokus = new TreeMap<>();
		sudokus.put("1 - Simple", new Sudoku(sudoku1));
		sudokus.put("2 - Easy", new Sudoku(sudoku2));
		sudokus.put("3 - Middle", new Sudoku(sudoku3));
		sudokus.put("4 - Hard", new Sudoku(sudoku4));
		sudokus.put("5 - 6 Stars", new Sudoku(sudoku5));

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