package sudoku;

/**
 * Coordinaten om als key te gebruiken
 * @author Aeilko Bos
 */
public class Coordinate implements Comparable<Coordinate> {
	// Co�rdinaten
	private int x;
	private int y;
	
	
	// Constructor
	/**
	 * Maakt nieuwe co�rdinaten aan
	 * @param x De x co�rdinaat.
	 * @param y De y co�rdinaat.
	 */
	public Coordinate(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	
	// Queries
	/**
	 * @return De x co�rdinaat
	 */
	public int getX(){
		return this.x;
	}
	
	/**
	 * @return De y co�rdinaat
	 */
	public int getY(){
		return this.y;
	}
	
	
	// Override's
	@Override
	public String toString(){
		return this.x + ", " + this.y;
	}
	
	@Override
	public int compareTo(Coordinate c) {
		if(c.getX() == this.x){
			if(c.getY() == this.y){
				return 0;
			}
			else if(c.getY() < this.y){
				return -1;
			}
			else{
				return 1;
			}
		}
		else if(c.getX() < this.x){
			return -1;
		}
		else{
			return 1;
		}
	}
}