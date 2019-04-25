package sudoku.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import sudoku.Sudoku;
import sudoku.Coordinate;

/**
 * Een klasse die probeert om een Sudoku op te lossen.
 * @author Aeilko Bos
 */
public class SudokuSolver {
	// De sudoku die opgelost moet worden
	private Sudoku sudoku;
	// De sudoku waar mee gestart is
	private Sudoku startSudoku;
	// Of de sudoku opgelost is
	private boolean solved;
	
	// Mogelijkheden
	// TODO: hokPossible is just wrong. Change it to a hashSet, and define which way around the coords are.
	// TODO: Currently the called (col, row) in the code, but actually are (row, col)
	private TreeMap<Coordinate, ArrayList<Integer>> hokPossible;
	private TreeMap<Integer, ArrayList<Integer>> colPossible;
	private TreeMap<Integer, ArrayList<Integer>> rowPossible;
	private TreeMap<Integer, ArrayList<Integer>> blokPossible;
	
	// Tijd bijhouden
	private long startTime;
	private long stopTime;
	
	
	// Constructor
	/**
	 * Maakt een nieuwe solver aan voor de gegeven Sudoku.
	 * @param s De sudoku die opgelost moet worden.
	 */
	public SudokuSolver(Sudoku s){
		this.sudoku = (Sudoku) s.clone();
		this.startSudoku = s;
		this.solved = false;
		
		this.hokPossible = new TreeMap<Coordinate, ArrayList<Integer>>();
		this.colPossible = new TreeMap<Integer, ArrayList<Integer>>();
		this.rowPossible = new TreeMap<Integer, ArrayList<Integer>>();
		this.blokPossible = new TreeMap<Integer, ArrayList<Integer>>();
		
		this.startTime = -1;
		this.stopTime = -1;
		
		this.checkSolved();
	}
	
	/**
	 * Maakt een nieuwe solver aan, gebaseerd op een dubbelle short array met de waardes van de sudoku (0 = leeg).
	 * @param s De dubbele short array.
	 * @require s.length == 9
	 * @require for(short i = 0; i < 9; i++){ s[i].length == 9
	 * @require for(short i = 0; i < 9; i++){ for(short j = 0; j < 9; j++){ s[i][j] >= 0 && s[i][j] <= 9 }}
	 */
	public SudokuSolver(short[][] s){
		this(new Sudoku(s));
	}
	
	
	// Commands
	/**
	 * Probeert de sudoku op te lossen
	 * @return Of de sudoku is opgelost
	 */
	public boolean solve(){
		this.prepareSolve();
		this.startTime = System.nanoTime();
		// Maximaal 81 keer proberen (aantal vakjes in een sudoku)
		for(int i = 0; i < 81 && !this.solved; i++){
			System.out.println("Round " + i);
			Sudoku oldSudoku = (Sudoku) this.sudoku.clone();
			this.enkeleMogelijkheid();
			this.checkCols();
			this.checkRows();
			this.checkBlok();
			
			// Deze methode's alleen uitvoeren als er niks veranderd is, aangezien deze vrij zwaar zijn.
			if(this.sudoku.equals(oldSudoku)){
				this.twinCols();
				this.twinRows();
				this.twinBlok();

				if(this.sudoku.equals(oldSudoku)){
					this.slings();
				}
			}

			this.checkSolved();
		}

		//System.out.println("Row 1 poss: " + this.getRowPossibilities(0));
		//System.out.println("Col 1 poss: " + this.getColPossibilities(0));
		//System.out.println("Block 3 poss: " + this.getBlockPossibilities(2));

		this.stopTime = System.nanoTime();;
		return this.isSolved();
	}
	
	/**
	 * Doet het voorbereidende werk voor het oplossen van de sudoku.
	 * Haalt alle vakjes op en genereert lijsten met mogelijke getallen.
	 */
	private void prepareSolve(){
		// Een lijst met alle mogelijkheden
		ArrayList<Integer> legeLijst = new ArrayList<Integer>();
		for(int i = 1; i <= 9; i++){
			legeLijst.add(i);
		}
		
		// Alle lijsten instellen met alle mogelijkheden
		for(int i = 0; i < 9; i++){
			this.colPossible.put(i, new ArrayList<Integer>(legeLijst));
			this.rowPossible.put(i, new ArrayList<Integer>(legeLijst));
			this.blokPossible.put(i, new ArrayList<Integer>(legeLijst));
			for(int j = 0; j < 9; j++){
				this.hokPossible.put(new Coordinate(i, j), new ArrayList<Integer>(legeLijst));
			}
		}
		
		// Alle hokjes ophalen
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				int val = (int) this.sudoku.getVal((short) i, (short) j);
				if(val != 0){
					// Hokje is niet leeg, waarde aanpassen
					this.setValue(i, j, val);
				}
			}
		}
	}
	
	/**
	 * Past de waarde van een hokje aan in de solver.
	 * @param col De kolom van het hokje.
	 * @param row De rij van het hokje.
	 * @param val De nieuwe waarde van het hokje.
	 * @require col >= 0 && col <= 8
	 * @require row >= 0 && row <= 8
	 * @require val >= 1 && val <= 9
	 */
	private void setValue(int col, int row, int val){
		// Algemene mogelijkheden veranderen.
		this.hokPossible.put(new Coordinate(col, row), new ArrayList<Integer>());
		this.colPossible.get(col).remove((Object) val);
		this.rowPossible.get(row).remove((Object) val);
		this.blokPossible.get(this.coordsToBlok(col, row)).remove((Object) val);
		
		// Mogelijkheid uit hele kolom en rij verwijderen.
		for(int i = 0; i < 9; i++){
			this.hokPossible.get(new Coordinate(col, i)).remove((Object) val);
			this.hokPossible.get(new Coordinate(i, row)).remove((Object) val);
		}
		
		// Mogelijkheid uit hele blok verwijderen.
		int blok = this.coordsToBlok(col, row);
		int colStart = (blok%3)*3;
		int rowStart = (blok/3)*3;
		for(int i = colStart; i < colStart+3; i++){
			for(int j = rowStart; j < rowStart+3; j++){
				this.hokPossible.get(new Coordinate(i, j)).remove((Object) val);
			}
		}

		// Waarde doorgeven
		this.sudoku.setVal((short) col, (short) row, (short) val);
	}
	
	/**
	 * Controleert of de sudoku is opgelost
	 */
	private void checkSolved(){
		boolean result = true;
		for(short i = 0; i < 9 && result; i++){
			for(short j = 0; j < 9 && result; j++){
				if(this.sudoku.getVal(i, j) == 0){
					result = false;
				}
			}
		}
		this.solved = result;
	}
	
	
	// Queries
	/**
	 * @return Of de sudoku is opgelost.
	 */
	public boolean isSolved(){
		return this.solved;
	}
	
	/**
	 * Vertaalt een kolom-rij combinatie naar een blok op het sudoku veld.
	 * @param col De kolom van het hokje.
	 * @param row De rij van het hokje.
	 * @return Het blok waar dit hokje in ligt.
	 * @require col >= 0 && col <= 8
	 * @require row >= 0 && row <= 8
	 */
	private int coordsToBlok(int col, int row){
		return (3*(row/3))+(col/3);
	}
	
	/**
	 * Geeft de tijd die gebruikt is om de sudoku op te lossen op -1 als er nog niet geprobeert is de sudoku op te lossen.
	 * @return De tijd in seconden.
	 */
	public double getTimeNeeded(){
		if(startTime == -1 || stopTime == -1){
			return -1;
		}
		else{
			return (stopTime-startTime)/((double) 1000000000);
		}
	}
	
	
	// Solve methode's
	/**
	 * Controleert of er ergens een hokje is met nog maar 1 mogelijkheid.
	 */
	private void enkeleMogelijkheid(){
		// Controleer alle hokjes.
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				ArrayList<Integer> temp = this.hokPossible.get(new Coordinate(i, j));
				if(temp.size() == 1){
					// Er is maar 1 mogelijkheid in dit hokje, waarde invullen.
					this.setValue(i, j, temp.get(0));
				}
			}
		}
	}

	private void slings(){
		HashMap<Integer, HashSet<Integer>> possibilities;

		// Perform slings with cols.
		System.out.println("Sling cols");
		for(int i = 0; i < 9; i++) {
			possibilities = this.getColPossibilities(i);
			possibilities = this.sling(possibilities);
			this.setColPossibilities(i, possibilities);
		}

		// Perform slings with rows.
		System.out.println("Sling rows");
		for(int i = 0; i < 9; i++){
			possibilities = this.getRowPossibilities(i);
			possibilities = this.sling(possibilities);
			this.setRowPossibilities(i, possibilities);
		}

		// Perform slings with blocks.
		System.out.println("Sling blocks");
		for(int i = 0; i < 9; i++){
			possibilities = this.getBlockPossibilities(i);
			possibilities = this.sling(possibilities);
			this.setBlockPossibilities(i, possibilities);
		}
	}

	private HashMap<Integer, HashSet<Integer>> getColPossibilities(int col){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		for(int row = 0; row < 9; row++){
			// TODO: Replace hokPossible with a HashSet
			HashSet<Integer> tmp = new HashSet();
			tmp.addAll((this.hokPossible.get(new Coordinate(row, col))));
			result.put(row, tmp);
		}

		return result;
	}

	private void setColPossibilities(int col, HashMap<Integer, HashSet<Integer>> poss){
		for(int row = 0; row < 9; row++){
			// TODO: replace hokPossible with a HashSet
			ArrayList<Integer> tmp = new ArrayList<>();
			tmp.addAll(poss.get(row));
			this.hokPossible.put(new Coordinate(row, col), tmp);
		}
	}

	private HashMap<Integer, HashSet<Integer>> getRowPossibilities(int row){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		for(int col = 0; col < 9; col++){
			// TODO: replace hokPossible with a HashSet
			HashSet<Integer> tmp = new HashSet<>();
			tmp.addAll(this.hokPossible.get(new Coordinate(row, col)));
			result.put(col, tmp);
		}

		return result;
	}

	private void setRowPossibilities(int row, HashMap<Integer, HashSet<Integer>> poss){
		for(int col = 0; col < 9; col++){
			// TODO: replace hokPossible with a HashSet
			ArrayList<Integer> tmp = new ArrayList<>();
			tmp.addAll(poss.get(col));
			this.hokPossible.put(new Coordinate(row, col), tmp);
		}
	}

	private HashMap<Integer, HashSet<Integer>> getBlockPossibilities(int block){
		HashMap<Integer, HashSet<Integer>> result = new HashMap<>();

		int i = 0;
		for(int row = (block/3)*3; row < ((block/3)+1)*3; row++){
			for(int col = (block%3)*3; col < ((block%3)+1)*3; col++) {
				// TODO: Replace hokPossible with a HashSet
				HashSet<Integer> tmp = new HashSet<>();
				tmp.addAll(this.hokPossible.get(new Coordinate(row, col)));
				result.put(i, tmp);
				i++;
			}
		}

		return result;
	}

	private void setBlockPossibilities(int block, HashMap<Integer, HashSet<Integer>> poss){
		int i = 0;
		for(int row = (block/3)*3; row < ((block/3)+1)*3; row++){
			for(int col = (block%3)*3; col < ((block%3)+1)*3; col++) {
				// TODO: Replace hokPossible with a HashSet
				ArrayList<Integer> tmp = new ArrayList<>();
				tmp.addAll(poss.get(i));
				this.hokPossible.put(new Coordinate(row, col), tmp);
				i++;
			}
		}
	}
	
	/**
	 * Controleert of een waarde maar op 1 plek in een kolom mogelijk is.
	 */
	private void checkCols(){
		// Voor elke kolom.
		for(int col = 0; col < 9; col++){
			// Mogelijkheden in een map met (getal), (rij waar getal mogelijk is).
			TreeMap<Integer, ArrayList<Integer>> mogelijk = new TreeMap<Integer, ArrayList<Integer>>();
			// Vullen met lege lijsten
			for(int i = 1; i < 10; i++){
				mogelijk.put(i, new ArrayList<Integer>());
			}
			
			// Mogelijkheden toevoegen aan de map.
			ArrayList<Integer> temp;
			for(int i = 0; i < 9; i++){
				// Mogelijke waardes van een specifiek hokje in deze kolom
				temp = this.hokPossible.get(new Coordinate(col, i));
				for(int val: temp){
					//System.out.println("Val: " + val);
					mogelijk.get((Object) val).add(i);
				}
			}
			
			// Controleren op waardes die maar op 1 plek mogelijk zijn.
			for(int i = 1; i < 10; i++){
				temp = mogelijk.get((Object) i);
				if(temp.size() == 1){
					// Slechts 1 mogelijkheid, lees de rij uit en vul de waarde in.
					int row = temp.get(0);
					this.setValue(col, row, i);
				}
			}
		}
	}
	
	/**
	 * Controleert of een waarde maar op 1 plek in een rij mogelijk is.
	 */
	private void checkRows(){
		// Voor elke rij.
		for(int row = 0; row < 9; row++){
			// Mogelijkheden in een map met (getal), (kolom waar getal mogelijk is).
			TreeMap<Integer, ArrayList<Integer>> mogelijk = new TreeMap<Integer, ArrayList<Integer>>();
			// Vullen met lege lijsten
			for(int i = 1; i < 10; i++){
				mogelijk.put(i, new ArrayList<Integer>());
			}
			
			// Mogelijkheden toevoegen aan de map.
			ArrayList<Integer> temp;
			for(int i = 0; i < 9; i++){
				// Mogelijke waardes van een specifiek hokje in deze kolom
				temp = this.hokPossible.get(new Coordinate(i, row));
				for(int val: temp){
					mogelijk.get((Object) val).add(i);
				}
			}
			
			// Controleren op waardes die maar op 1 plek mogelijk zijn.
			for(int i = 1; i < 10; i++){
				temp = mogelijk.get((Object) i);
				if(temp.size() == 1){
					// Slechts 1 mogelijkheid, lees de rij uit en vul de waarde in.
					int col = temp.get(0);
					this.setValue(col, row, i);
				}
			}
		}
	}
	
	/**
	 * Controleert of een waarde maar op 1 plek in een blok mogelijk is.
	 */
	private void checkBlok(){
		// Voor elk blok
		for(int blok = 0; blok < 9; blok++){
			// Mogelijkheden in een map met (getal), (kolom waar getal mogelijk is).
			TreeMap<Integer, ArrayList<Coordinate>> mogelijk = new TreeMap<Integer, ArrayList<Coordinate>>();
			// Vullen met lege lijsten
			for(int i = 1; i < 10; i++){
				mogelijk.put(i, new ArrayList<Coordinate>());
			}
			
			// Alle hokjes in het blok afgaan
			ArrayList<Integer> temp;
			int colStart = (blok%3)*3;
			int rowStart = (blok/3)*3;
			for(int i = colStart; i < colStart+3; i++){
				for(int j = rowStart; j < rowStart+3; j++){
					temp = this.hokPossible.get(new Coordinate(i, j));
					for(int val: temp){
						mogelijk.get((Object) val).add(new Coordinate(i, j));
					}
				}
			}
			
			// Controleren op waardes die maar op 1 plek mogelijk zijn
			ArrayList<Coordinate> temp2;
			for(int i = 1; i < 10; i++){
				temp2 = mogelijk.get((Object) i);
				if(temp2.size() == 1){
					// De waarde i kan slechts in 1 hokje in dit blok, dus vullen we i daar in.
					Coordinate c = temp2.get(0);
					this.setValue(c.getX(), c.getY(), i);
				}
			}
		}
	}
	
	/**
	 * Controleert of een getal maar op twee plaatsen in een kolom kan.
	 * Indien er twee getallen zijn die maar op twee plaatsen kunnen, en dit zijn dezelfde plaatsen kunnen in deze hokjes geen andere getallen meer staan.
	 */
	private void twinCols(){
		// Elke rij
		for(int col = 0; col < 9; col++){
			// Map waardes volgens (waarde), (Lijst met rijen)
			TreeMap<Integer, ArrayList<Integer>> waardes = new TreeMap<Integer, ArrayList<Integer>>();
			// Vullen met lege lijsten
			for(int val = 1; val <= 9; val++){
				waardes.put(val, new ArrayList<Integer>());
			}
			
			// Alle hokjes controleren
			ArrayList<Integer> temp;
			for(int row = 0; row < 9; row++){
				temp = this.hokPossible.get(new Coordinate(col, row));
				for(Integer val: temp){
					waardes.get((Object) val).add(row);
				}
			}
			
			// Controleren zijn of er getallen maar op twee plek kunnen staan.
			ArrayList<Integer> twoPossible = new ArrayList<Integer>();
			for(int val = 1; val <= 9; val++){
				temp = waardes.get((Object) val);
				if(temp.size() == 2){
					for(Integer waarde: twoPossible){
						if(waardes.get((Object) waarde).equals(temp)){
							// Er zijn twee waardes die enkel in dezelfde twee hokjes kunnen.
							ArrayList<Integer> newPossible = new ArrayList<Integer>();
							newPossible.add(waarde);
							newPossible.add(val);
							for(Integer row: temp){
								// Voor beide hokjes de mogelijkheden aanpassen naar enkel deze twee waardes
								this.hokPossible.put(new Coordinate(col, row), new ArrayList<Integer>(newPossible));
							}
						}
					}
					twoPossible.add(val);
				}
			}
		}
	}
	
	/**
	 * Controleert of een getal maar op twee plaatsen in een rij kan.
	 * Indien er twee getallen zijn die maar op twee plaatsen kunnen, en dit zijn dezelfde plaatsen kunnen in deze hokjes geen andere getallen meer staan.
	 */
	private void twinRows(){
		// Elk blok
		for(int row = 0; row < 9; row++){
			// Map waardes volgens (waarde), (Lijst met kolommen)
			TreeMap<Integer, ArrayList<Integer>> waardes = new TreeMap<Integer, ArrayList<Integer>>();
			// Vullen met lege lijsten
			for(int val = 1; val <= 9; val++){
				waardes.put(val, new ArrayList<Integer>());
			}
			
			// Alle hokjes controleren
			ArrayList<Integer> temp;
			for(int col = 0; col < 9; col++){
				temp = this.hokPossible.get(new Coordinate(col, row));
				for(Integer val: temp){
					waardes.get((Object) val).add(col);
				}
			}
			
			// Controleren zijn of er getallen maar op ��n plek kunnen staan.
			ArrayList<Integer> twoPossible = new ArrayList<Integer>();
			for(int val = 1; val <= 9; val++){
				temp = waardes.get((Object) val);
				if(temp.size() == 2){
					for(Integer waarde: twoPossible){
						if(waardes.get((Object) waarde).equals(temp)){
							// Er zijn twee waardes die enkel in dezelfde twee hokjes kunnen.
							ArrayList<Integer> newPossible = new ArrayList<Integer>();
							newPossible.add(waarde);
							newPossible.add(val);
							for(Integer col: temp){
								// Voor beide hokjes de mogelijkheden aanpassen naar enkel deze twee waardes
								this.hokPossible.put(new Coordinate(col, row), new ArrayList<Integer>(newPossible));
							}
						}
					}
					twoPossible.add(val);
				}
			}
		}
	}
	
	/**
	 * Controleert of een getal maar op twee plaatsen in een blok kan.
	 * Indien er twee getallen zijn die maar op twee plaatsen kunnen, en dit zijn dezelfde plaatsen kunnen in deze hokjes geen andere getallen meer staan.
	 */
	private void twinBlok(){
		// Elk blok
		for(int blok = 0; blok < 9; blok++){
			// Map waardes volgens (waarde), (Lijst met co�rdinaten)
			TreeMap<Integer, ArrayList<Coordinate>> waardes = new TreeMap<Integer, ArrayList<Coordinate>>();
			// Vullen met lege lijsten
			for(int val = 1; val <= 9; val++){
				waardes.put(val, new ArrayList<Coordinate>());
			}
			
			// Alle hokjes controleren
			ArrayList<Integer> temp;
			int startCol = (blok%3)*3;
			int startRow = (blok/3)*3;
			for(int col = startCol; col < startCol+3; col++){
				for(int row = startRow; row < startRow+3; row++){
					temp = this.hokPossible.get(new Coordinate(col, row));
					for(Integer val: temp){
						waardes.get((Object) val).add(new Coordinate(col, row));
					}
				}
			}
			
			// Controleren zijn of er getallen maar op twee plek kunnen staan.
			ArrayList<Coordinate> temp2;
			ArrayList<Integer> twoPossible = new ArrayList<Integer>();
			for(int val = 1; val <= 9; val++){
				temp2 = waardes.get((Object) val);
				if(temp2.size() == 2){
					for(Integer waarde: twoPossible){
						if(waardes.get((Object) waarde).equals(temp2)){
							// Er zijn twee waardes die enkel in dezelfde twee hokjes kunnen.
							ArrayList<Integer> newPossible = new ArrayList<Integer>();
							newPossible.add(waarde);
							newPossible.add(val);
							for(Coordinate coord: temp2){
								// Voor beide hokjes de mogelijkheden aanpassen naar enkel deze twee waardes
								this.hokPossible.put(new Coordinate(coord.getX(), coord.getY()), new ArrayList<Integer>(newPossible));
							}
						}
					}
					twoPossible.add(val);
				}
			}
		}
	}

	/***
	 * Checks if it can find a sling of fields which have to contain a certain series of numbers.
	 * Example: 4 fields, possibilities (8,3),(3,6),(6,8),(9,3). The first 3 columns must contain 3,6 and 8.
	 * 			So now we can remove 3 as a possibility from the 4th field, and therefore it has to be 9.
	 */
	private HashMap<Integer, HashSet<Integer>> sling(HashMap<Integer, HashSet<Integer>> poss){
		ArrayList<Integer> emptyFields = new ArrayList<>();
		// Find the fields which are still empty
		for(int k: poss.keySet()){
			if(poss.get(k).size() != 0)
				emptyFields.add(k);
		}

		// No we have to find x amount of fields, which can only contain x amount of values.
		// Than we can remove these x values from all the other cells.
		// TODO: Find a way to use a dynamic number of fields instead of the hardcoded i=2
		int i = 2;
		//for(int i = 2; i < emptyFields.size(); i++) {
			// Check each combination of fields.
			for(int j = 0; j <= emptyFields.size()-i+1; j++){
				if(poss.get(emptyFields.get(j)).size() <= i) {
					for (int k = j+1; k < emptyFields.size(); k++) {
						if (poss.get(emptyFields.get(k)).size() <= i) {
							// Use a hashSet because this will prevent duplicates
							HashSet<Integer> possibleValues = new HashSet<>();
							possibleValues.addAll(poss.get(emptyFields.get(j)));
							possibleValues.addAll(poss.get(emptyFields.get(k)));

							if (possibleValues.size() == i) {
								//System.out.println("Removing:  " + possibleValues.toString());
								// Remove these options from all other cols.
								for(int x = 0; x < emptyFields.size(); x++){
									if(x != j && x != k){
										HashSet<Integer> tmp = poss.get(emptyFields.get(x));
										tmp.removeAll(possibleValues);
										poss.put(emptyFields.get(x), tmp);
									}
								}
							}
						}
					}
				}
			}
		//}

		return poss;
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
}