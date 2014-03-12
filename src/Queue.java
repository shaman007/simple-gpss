import java.util.ArrayList;
/**
 * Defines future and current events queues and API for them
 * Queue.java 14.12.2003
 *
 * @author  Andrey Bondarenko 
 * @e-mail  bondarenko007@aport2000.ru
 * @license GPL
 */
class Queue {
	/** Current Events Chain */
	private ArrayList Current_Events;
	/** Future Events Chain */
	private ArrayList Future_Events;
	/** Main queues */
	/** Current Events Chain lenght */
	private int CE_count;
	/** Future Events Chain lenght */
	private int FE_count;
	/** 1, if Futire_Events need to be sorted by time_next */
	public int Need_Sort;
	/** Default constructor. Allocates memory */
	public Queue() {
		try {
			Current_Events = new ArrayList();
			Future_Events = new ArrayList();
			CE_count = Current_Events.size();
			FE_count = Future_Events.size();
			Need_Sort = 1;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"Queue()\">" + e
					+ "</EXCEPTION>");
		}
	}
	/** Adds transact to Current_Events queue */
	public int add_CE(Transact CE) {
		try {
			Current_Events.add(CE);
			CE_count = Current_Events.size();
			return 0;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"add_CE(Transact CE)\">"
					+ e + "</EXCEPTION>");
			return -1;
		}
	}
	/** Adds transact to Future_Events queue */
	public int add_FE(Transact FE) {
		try {
			Transact tmp;
			Future_Events.add(0, FE);
			if (Future_Events.size() > 1) {
				int i = 0;
				while (i < (Future_Events.size() - 1)) {
					if (((Transact) Future_Events.get(i + 1)).time_next < ((Transact) Future_Events
							.get(i)).time_next) {
						tmp = (Transact) Future_Events.remove(i);
						Future_Events.add(i + 1, tmp);
					} else
						break;
					i++;
				}
			}
			FE_count = Future_Events.size();
			return 0;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"add_FE(Transact FE)\">"
					+ e + "</EXCEPTION>");
			return -1;
		}
	}
	public int add_FE_gen(Transact FE) {
		try {
			Need_Sort = 1;
			Future_Events.add(FE_count, FE);
			FE_count = Future_Events.size();
			return 0;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"add_FE(Transact FE)\">"
					+ e + "</EXCEPTION>");
			return -1;
		}
	}
	/** Returns transact from Current_Events */
	public Transact get_CE(int Pos) {
		try {
			CE_count = Current_Events.size() - 1;
			Transact CE;
			CE = (Transact) Current_Events.get(Pos);
			return CE;
		} catch (Exception e) {
			System.out
					.println("<EXCEPTION function=\"get_CE(int Pos,int Priority)\">"
							+ e + "</EXCEPTION>");
			return null;
		}
	}
	/** Returns transact from Future_Events */
	public Transact get_FE(int Pos) {
		try {
			FE_count = Future_Events.size() - 1;
			Transact FE;
			FE = (Transact) Future_Events.get(Pos);
			return FE;
		} catch (Exception e) {
			System.out
					.println("<EXCEPTION function=\"get_FE(int Pos, int Priority)\">"
							+ e + "</EXCEPTION>");
			return null;
		}
	}
	/** Returns and removes transact from Current_Events top */
	public Transact remove_CE() {
		try {
			CE_count = Current_Events.size() - 1;
			Transact CE;
			CE = (Transact) Current_Events.remove(0);
			return CE;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"remove_CE()\">" + e
					+ "</EXCEPTION>");
			return null;
		}
	}
	/** Returns and removes event from Future_Events top */
	public Transact remove_FE() {
		try {
			FE_count = Future_Events.size() - 1;
			Transact FE;
			FE = (Transact) Future_Events.remove(0);
			return FE;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"remove_FE()\">" + e
					+ "</EXCEPTION>");
			return null;
		}
	}
	/** Returns CE_count */
	public int get_CEC() {
		try {
			Current_Events.trimToSize();
			return CE_count;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"get_CEC()\">" + e
					+ "</EXCEPTION>");
			return -1;
		}
	}
	/** Returns FE_count */
	public int get_FEC() {
		try {
			Future_Events.trimToSize();
			FE_count = Future_Events.size();
			return FE_count;
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"get_FEC()\">" + e
					+ "</EXCEPTION>");
			return -1;
		}
	}
	public void Print() {
		try {
			ArrayList fCurrent_Events;
			ArrayList fFuture_Events;
			fCurrent_Events = new ArrayList();
			fFuture_Events = new ArrayList();
			fCurrent_Events.addAll(Current_Events);
			fFuture_Events.addAll(Future_Events);
			/*for (int i = 0; i < get_CEC(); i++) {
				System.out.println("\t\t\t<ENVIROMENT current_event_id[" + i
						+ "]=" + ((Transact) fCurrent_Events.get(i)).ID
						+ "></ENVIROMENT>");
			}
			for (int i = 0; i < get_FEC(); i++) {
				System.out.println("\t\t\t<ENVIROMENT future_event_id[" + i
						+ "]=" + ((Transact) fFuture_Events.get(i)).ID
						+ "></ENVIROMENT>");
			}*/
		} catch (Exception e) {
			System.out.println("<EXCEPTION function=\"Print()\">" + e
					+ "</EXCEPTION>");
		}
	}
}
