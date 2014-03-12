import java.util.Random;
/**
 * Defines "Generate" interface, bussiness logic and memory model
 * Generate.java 14.12.2003
 *
 * @author  Andrey Bondarenko 
 * @e-mail  bondarenko007@aport2000.ru
 * @license GPL
 */
class Generate {
	/** Temporary transact storage */
	private Transact temp;
	private Random rnd;
	/**
	 * Constructor with static timing a - static shitft b - random factor c -
	 * initial shift d - transats amount q - main queue storage current -
	 * current block number
	 */
	public Generate(int a, int b, int c, int d, Queue q, int current) {
		try {
			System.out.println("<GENERATE>");
			int i;
			rnd = new Random();
			for (i = 0; i < d; i++) {
				temp = new Transact(0);
				temp.block_current = current;
				System.out.println("\t\t\t<VARIABLE block_current="
						+ temp.block_current + "></VARIABLE>");
				temp.priority = 0;
				System.out.println("\t\t\t<VARIABLE priority=" + temp.priority
						+ "></VARIABLE>");
				temp.time_next = c + ((a - b / 2) * i) + rnd.nextInt(2 * b);
				System.out.println("\t\t\t<VARIABLE time_next="
						+ temp.time_next + "></VARIABLE>");
				int ercode = q.add_FE_gen(temp);
			}
			System.out.println("</GENERATE>");
		} catch (Exception e) {
			System.out
					.println("<EXCEPTION function=\"Generate(int a,int b,int c,int d, Queue q,int current ,int output)\">"
							+ e + "></EXCEPTION>");
		}
	}
}
