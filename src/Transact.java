import java.util.*;
/**
 * Defines transaction memory model. All calsses will work with Transact directly. 
 * No API except clone method.
 * Transact.java 14.12.2003
 *
 * @author  Andrey Bondarenko 
 * @e-mail  me@andreybondarenko.com
 * @license GPLv2
 */
class Transact extends java.lang.Object {
	/** This tine is planed for next event */
	public int time_next;
	/** Transact nunber */
	public int ID;
	/** Static counter. Counts all transacts in system */
	private static int counter;
	/** Transact _NOW_ in this block */
	public int block_current;
	/** Transact priority */
	public int priority;
	/** How many hopes left to go */
	public int hops_to_go;
	/** Does it in loop or not */
	public int in_loop;
	/** Does it in loop or not */
	public int param[];
	/** Label name */
	public String label_name;
	/** Old label name */
	public Stack old_label_name;
	/** Old hopes_to_go value */
	public Stack old_hopes;
	/** Old old_in_loop value */
	public Stack old_in_loop;
	/** default - all values are 0 except priority and ID */
	Transact(int pri) {
		try {
			System.out.println("<TRANSACT>");
			counter++;
			ID = counter;
			System.out.println("\t\t\t<VARIABLE id=" + ID + "></VARIABLE>");
			priority = pri;
			System.out.println("\t\t\t<VARIABLE priority=" + ID
					+ "></VARIABLE>");
			label_name = "";
			old_label_name = new Stack();
			old_in_loop = new Stack();
			old_hopes = new Stack();
			in_loop = 0;
			param = new int[128]; //Need to make it dynamic in future to save
								  // memory
			System.out.println("</TRANSACT>");
		} catch (Exception e) {
			System.out
					.println("<EXCEPTION function=Transact(int pri) exception="
							+ e + "></<EXCEPTION>");
		}
	}
	Transact() {
		/** Fake */
	}
	/** Clones everything except ID, usefull for Split */
	public void Clone_Transact(Transact clone) {
		try {
			clone.block_current = block_current;
			clone.hops_to_go = hops_to_go;
			clone.priority = priority;
			clone.time_next = time_next;
			clone.in_loop = in_loop;
			clone.label_name = label_name;
			clone.old_hopes = old_hopes;
			clone.old_label_name = old_label_name;
			clone.old_in_loop = old_in_loop;
		} catch (Exception e) {
			System.out
					.println("<EXCEPTION function=\"Clone_Transact(int pri)\">"
							+ e + "</EXCEPTION");
		}
	}
}
