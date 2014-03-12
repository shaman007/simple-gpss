/**
 * Implements timer corrction algorythm.
 * Time.java 14.12.2003
 *
 * @author  Andrey Bondarenko 
 * @e-mail  bondarenko007@aport2000.ru
 * @license GPL
 */
public class Time {
	/** Global Timer for all events. */
	private int Timer;
	/** Dafault constructor. Sarting with 0 time */
	public Time() {
		/** Starting with zero - time. */
		Timer = 0;
	}
	/** Returns current timer state */
	public int Get_Timer() {
		return Timer;
	}
	/** Main procedure - corrects global timer acording to the Future Event content */
	public void Timer_Correct(Queue que, Schema sch) {
		try {
			System.out.println("<TIMER>");
			int st;
			int i;
			int tt;
			/**
			 * Sort, because something in Future Evern was changed No need in
			 * sorting empty or one-element queue
			 */
			System.out.println("\t\t\t<ENVIROMENT Future_events_count="
					+ que.get_FEC() + "></ENVIROMENT>");
			if (que.get_FEC() > 1 && que.Need_Sort == 1) {
				System.out.println("<INFO message=Sorting></INFO>");
				sort(que);
				que.Need_Sort = 0;
			}
			tt = que.get_FEC();
			if (tt > 0) {
				Transact tmp;
				/**
				 * Fetching transact from FE
				 */
				tmp = que.remove_FE();
				/**
				 * Modifying timer
				 */
				Timer = tmp.time_next;
				/**
				 * Saving transact in CE
				 */
				st = que.add_CE(tmp);
				/**
				 * Fetching next transact from FE
				 */
				tt = que.get_FEC();
				int j = 0;
				for (i = 0; i < tt; i++) {
					tmp = que.get_FE(j);
					/**
					 * While there are transacts to move now.
					 *  
					 */
					if (tmp.time_next == Timer) {
						/**
						 * If time_nexet for us - saving it in CE
						 */
						tmp = que.remove_FE();
						st = que.add_CE(tmp);
						j--;
					}
					j++;
				}
				/**
				 * Timer modofication has been finished Launching Shema
				 * Processor
				 */
				System.out.println("\t\t\t<ENVIROMENT Time=" + Timer
						+ "></ENVIROMENT>\n</TIMER>");
				return;
			} else {
				return;
			}
		} catch (Exception e) {
			System.out
					.println("<EXCEPTION function=Timer_Correct(Queue que,Schema sch) exception="
							+ e + "></EXCEPTION>");
		}
	}
	/**
	 * Here we have the slowest part of the modelling itaration
	 */
	/*
	 * @(#)SortAlgorithm.java 1.6f 95/01/31 James Gosling
	 * 
	 * Copyright (c) 1994-1995 Sun Microsystems, Inc. All Rights Reserved.
	 * 
	 * Permission to use, copy, modify, and distribute this software and its
	 * documentation for NON-COMMERCIAL or COMMERCIAL purposes and without fee
	 * is hereby granted. Please refer to the file
	 * http://java.sun.com/copy_trademarks.html for further important copyright
	 * and trademark information and to http://java.sun.com/licensing.html for
	 * further important licensing information for the Java (tm) Technology.
	 * 
	 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
	 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
	 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
	 * OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
	 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
	 * ITS DERIVATIVES.
	 * 
	 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
	 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
	 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
	 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
	 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
	 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
	 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). SUN
	 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
	 * HIGH RISK ACTIVITIES.
	 */
	/**
	 * A generic sort demonstration algorithm SortAlgorithm.java, Thu Oct 27
	 * 10:32:35 1994
	 * 
	 * @author James Gosling with Andrey Bondarenko types modifications
	 * @version 1.6f, 31 Jan 1995
	 */
	private void QuickSort(Transact a[], int l, int r) throws Exception {
		int M = 4;
		int i = 0;
		int j = 0;
		Transact v;
		if ((r - l) > M) {
			i = (r + l) / 2;
			if (a[l].time_next > a[i].time_next)
				swap(a, l, i); // Tri-Median Methode!
			if (a[l].time_next > a[r].time_next)
				swap(a, l, r);
			if (a[i].time_next > a[r].time_next)
				swap(a, i, r);
			j = r - 1;
			swap(a, i, j);
			i = l;
			v = a[j];
			for (;;) {
				while (a[++i].time_next < v.time_next);
				while (a[--j].time_next > v.time_next);
				if (j < i)
					break;
				swap(a, i, j);
			}
			swap(a, i, r - 1);
			QuickSort(a, l, j);
			QuickSort(a, i + 1, r);
		}
	}
	private void swap(Transact a[], int i, int j) {
		Transact T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;
	}
	private void InsertionSort(Transact a[], int lo0, int hi0) throws Exception {
		int i;
		int j;
		Transact v;
		for (i = lo0 + 1; i <= hi0; i++) {
			v = a[i];
			j = i;
			while ((j > lo0) && (a[j - 1].time_next > v.time_next)) {
				a[j] = a[j - 1];
				j--;
			}
			a[j] = v;
		}
	}
	private void sort(Queue arr) throws Exception {
		/**
		 * This ugly function saves Future Events in temporary storage and
		 * returns transacts in Future Events after processing
		 */
		Transact a[];
		int c = arr.get_FEC();
		a = new Transact[c];
		for (int i = 0; i < c; i++) {
			a[i] = (Transact) arr.remove_FE();
		}
		QuickSort(a, 0, c - 1);
		InsertionSort(a, 0, c - 1);
		for (int i = 0; i < c; i++) {
			arr.add_FE_gen(a[i]);
		}
	}
}
