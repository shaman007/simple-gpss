/**
 * Defines model block memory model. 
 * Block.java 14.12.2003
 *
 * @author Andrey Bondarenko 
 * @e-mail bondarenko007@aport2000.ru
 * @license GPL
 */
public class Block {
	/**
	 * Defines block type
	 * 
	 * Block Type description
	 * 
	 * 0 - Generate + 1 - Terminate + 2 - Queue + 3 - Depart + 4 - Seize + 5 -
	 * Split + 6 - Release + 7 - Tabulate 8 - Advance + 9 - Enter + 10 - Leave +
	 * 11 - Loop + 12 - Transfer + 13 - Test + 14 - Assign + 15 - Savevalue + 16 -
	 * Priority + 17 - Assemble + 18 - Queue-Storage + 19 - Buffer + 20 - Table + + -
	 * frontend avalible
	 */
	public int Type;
	/** Unic block ID */
	public int ID;
	/** Static global block counter */
	public static int Counter;
	/** Number of block parameteres */
	public int Count;
	/**
	 * Block parameteres Arguments shuld be parsed in block's business logig
	 */
	public String Args[];
	/**
	 * Default constructor type - block type. count - number of arguments args =
	 * arguments for the block
	 */
	public Block(int type, int count, String args[]) {
		try {
			Type = type;
			Count = count;
			Args = new String[count];
			for (int i = 0; i < count; i++) {
				Args[i] = args[i];
			}
			ID = Counter++;
		} catch (Exception e) {
			System.out
					.print("<EXCEPTION message=Block(int type,int count, String args[])></EXCEPTION>");
			System.out.println(e);
		}
	}
	/**
	 * Empty and dummy block constructor Defines empty block
	 */
	public Block() {
		ID = -1;
		Type = -1;
	}
}
